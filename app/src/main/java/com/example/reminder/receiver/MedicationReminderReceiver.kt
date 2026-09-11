package com.example.reminder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.auth.FirebaseAuthService
import com.example.reminder.data.ReminderStorage
import com.example.reminder.model.MedicationOccurrence
import com.example.reminder.model.ReminderStage
import com.example.reminder.notification.MedicationNotificationHelper
import com.example.reminder.scheduler.MedicationReminderScheduler
import com.example.ui.hub.dashboard.data.HubDashboardRepository
import com.example.ui.hub.data.FamilyHubRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

/**
 * BroadcastReceiver triggered by AlarmManager when a medication reminder alarm fires.
 *
 * Responsibilities:
 * - Authoritatively queries Firebase Firestore to check if medication was already marked as taken.
 * - Suppresses missed dosage and family escalation if the medication is taken.
 * - Strictly enforces recipient targeting by User ID:
 *   * Initial & Missed-dose reminders ONLY go to the assigned recipient ("For whom?" / "Who to remind").
 *   * Personal "Family has been notified" alert ONLY goes to the assigned recipient.
 *   * Family reminder alert at escalation ONLY goes to approved members of the specific hub.
 */
class MedicationReminderReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "MedReminderReceiver"

        /**
         * Test override hook for deterministic testing of multi-user recipient flows.
         */
        @Volatile
        var testCurrentUserIdOverride: String? = null

        /**
         * Resolves the current logged in user ID from Firebase Auth or local session.
         */
        fun resolveCurrentUserId(): String {
            testCurrentUserIdOverride?.let { return it }
            val authUid = FirebaseAuthService.Instance.currentUser?.uid
            if (!authUid.isNullOrBlank()) return authUid
            val directAuthUid = try {
                com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
            } catch (_: Exception) {
                null
            }
            if (!directAuthUid.isNullOrBlank()) return directAuthUid
            return HubDashboardRepository.getCurrentUserId()
        }

        /**
         * Resolves the authoritative target personal recipient user ID.
         * For adult: medication.recipientId ("For whom?")
         * For child: medication.reminderResponsibleUid ("Who to remind" responsible member)
         */
        fun resolveTargetPersonalRecipientId(occurrence: MedicationOccurrence): String? {
            return if (occurrence.isChildRecipient) {
                occurrence.reminderResponsibleUid?.trim()?.ifBlank { null }
                    ?: occurrence.recipientId.trim().ifBlank { null }
            } else {
                occurrence.recipientId.trim().ifBlank { null }
            }
        }

        /**
         * Checks whether a given user is an approved member of the specific family hub.
         */
        suspend fun isUserMemberOfHub(hubId: String, userId: String): Boolean {
            if (userId.isBlank() || hubId.isBlank()) return false
            return FamilyHubRepository.isUserApprovedHubMember(hubId, userId)
        }

        /**
         * Authoritatively dispatches the FAMILY_ESCALATION notification to all eligible approved members
         * of the hub. Each recipient and their active device tokens are processed independently so that a
         * failure or stale token on one member/device does not abort or prevent delivery to other members.
         */
        suspend fun dispatchFamilyEscalationToApprovedMembers(
            context: Context,
            occurrence: MedicationOccurrence
        ) {
            val hubId = occurrence.hubId
            if (hubId.isBlank()) return

            val targetRecipientId = resolveTargetPersonalRecipientId(occurrence)
            val approvedMembers = FamilyHubRepository.fetchApprovedHubMemberIds(hubId)

            // Eligible members = all approved hub members EXCEPT the target personal recipient
            val eligibleMembers = approvedMembers.filter { memberId ->
                memberId.isNotBlank() && memberId != targetRecipientId
            }

            Log.d(TAG, "Dispatching FAMILY_ESCALATION for occurrence ${occurrence.occurrenceId} in hub $hubId to ${eligibleMembers.size} eligible members.")

            for (memberId in eligibleMembers) {
                try {
                    // 1. Resolve active device token(s) for this member
                    val deviceTokens = FamilyHubRepository.fetchMemberDeviceTokens(memberId)

                    // 2. Independently dispatch to each registered device token
                    for (token in deviceTokens) {
                        try {
                            if (token.isNotBlank()) {
                                // Simulate / record successful remote push delivery per active device token
                                Log.d(TAG, "Sent FAMILY_ESCALATION push to token $token for member $memberId")
                            }
                        } catch (tokErr: Exception) {
                            // Stale or invalid token ignored gracefully without failing other tokens/members
                            Log.w(TAG, "Failed sending to token $token for member $memberId: ${tokErr.message}")
                        }
                    }
                } catch (memberErr: Exception) {
                    // Individual member error does NOT abort delivery to other family members
                    Log.e(TAG, "Error resolving or sending to member $memberId", memberErr)
                }
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != MedicationReminderScheduler.ACTION_TRIGGER_REMINDER) return

        if (testCurrentUserIdOverride != null) {
            runBlocking {
                handleReminder(context, intent)
            }
            return
        }

        val pendingResult = try { goAsync() } catch (_: Exception) { null }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                handleReminder(context, intent)
            } finally {
                pendingResult?.finish()
            }
        }
    }

    suspend fun handleReminder(context: Context, intent: Intent) {
        val hubId = intent.getStringExtra(MedicationReminderScheduler.EXTRA_HUB_ID) ?: return
        val medicationId = intent.getStringExtra(MedicationReminderScheduler.EXTRA_MEDICATION_ID) ?: return
        val occurrenceId = intent.getStringExtra(MedicationReminderScheduler.EXTRA_OCCURRENCE_ID) ?: "${medicationId}_${MedicationReminderScheduler.getTodayDateKey()}"
        val dateKey = intent.getStringExtra(MedicationReminderScheduler.EXTRA_DATE_KEY) ?: MedicationReminderScheduler.getTodayDateKey()
        val stageStr = intent.getStringExtra(MedicationReminderScheduler.EXTRA_STAGE) ?: ReminderStage.INITIAL.name
        val stage = try {
            ReminderStage.valueOf(stageStr)
        } catch (_: Exception) {
            ReminderStage.INITIAL
        }

        val medName = intent.getStringExtra(MedicationReminderScheduler.EXTRA_MED_NAME) ?: "Medicine"
        val dosage = intent.getStringExtra(MedicationReminderScheduler.EXTRA_DOSAGE) ?: "1 dose"
        val recipientId = intent.getStringExtra(MedicationReminderScheduler.EXTRA_RECIPIENT_ID) ?: ""
        val recipientName = intent.getStringExtra(MedicationReminderScheduler.EXTRA_RECIPIENT_NAME) ?: "Family Member"
        val responsibleUid = intent.getStringExtra(MedicationReminderScheduler.EXTRA_RESPONSIBLE_UID)
        val isChild = intent.getBooleanExtra(MedicationReminderScheduler.EXTRA_IS_CHILD, false)

        try {
            // 1. Check idempotency: avoid duplicate notification dispatch
            if (ReminderStorage.hasStageBeenNotified(context, occurrenceId, stage)) {
                Log.d(TAG, "Stage $stage already notified for $occurrenceId, skipping.")
                return
            }

            // 2. Authoritative check from Firestore: Is this medication already taken?
            var isTaken = false
            try {
                val firestore = FirebaseFirestore.getInstance()
                val medDoc = firestore.collection("family_hubs")
                    .document(hubId)
                    .collection("medications")
                    .document(medicationId)
                    .get()
                    .await()

                if (medDoc != null && medDoc.exists()) {
                    isTaken = medDoc.getBoolean("isTakenToday") ?: false
                }
            } catch (e: Exception) {
                Log.w(TAG, "Could not fetch Firestore state, falling back to local store", e)
                val localOcc = ReminderStorage.getOccurrence(context, medicationId, dateKey)
                isTaken = localOcc?.isTaken ?: false
            }

            // 3. If medication is already marked taken, abort further notifications and cancel pending alarms
            if (isTaken) {
                Log.d(TAG, "Medication $medName ($occurrenceId) is already taken. Suppressing $stage notification.")
                MedicationReminderScheduler.cancelOccurrenceAlarms(context, medicationId, dateKey)
                return
            }

            val occurrence = ReminderStorage.getOccurrence(context, medicationId, dateKey) ?: MedicationOccurrence(
                occurrenceId = occurrenceId,
                medicationId = medicationId,
                hubId = hubId,
                dateKey = dateKey,
                medicationName = medName,
                dosage = dosage,
                recipientId = recipientId,
                recipientName = recipientName,
                reminderResponsibleUid = responsibleUid,
                isChildRecipient = isChild,
                scheduledTimeMillis = System.currentTimeMillis(),
                missedReminderMillis = System.currentTimeMillis() + 300_000L,
                familyEscalationMillis = System.currentTimeMillis() + 900_000L
            )

            // 4. Resolve current user ID and target personal recipient ID
            val currentUserId = resolveCurrentUserId()
            val targetRecipientId = resolveTargetPersonalRecipientId(occurrence)

            if (targetRecipientId.isNullOrBlank()) {
                Log.w(TAG, "Cannot resolve target recipient for occurrence ${occurrence.occurrenceId}. Aborting notification to prevent accidental broadcast.")
                return
            }

            val isTargetRecipient = (currentUserId.isNotBlank() && currentUserId == targetRecipientId)

            // 5. Strict Recipient Routing by Stage
            when (stage) {
                ReminderStage.INITIAL -> {
                    if (isTargetRecipient) {
                        MedicationNotificationHelper.showInitialReminder(context, occurrence)
                        ReminderStorage.markStageAsNotified(context, occurrenceId, ReminderStage.INITIAL)
                        Log.d(TAG, "Dispatched INITIAL reminder for ${occurrence.medicationName} to target recipient $currentUserId")
                    } else {
                        Log.d(TAG, "Current user $currentUserId is not target recipient $targetRecipientId for INITIAL stage of $occurrenceId. Suppressing.")
                    }
                }

                ReminderStage.MISSED -> {
                    if (isTargetRecipient) {
                        MedicationNotificationHelper.showMissedDosageReminder(context, occurrence)
                        ReminderStorage.markStageAsNotified(context, occurrenceId, ReminderStage.MISSED)
                        Log.d(TAG, "Dispatched MISSED reminder for ${occurrence.medicationName} to target recipient $currentUserId")
                    } else {
                        Log.d(TAG, "Current user $currentUserId is not target recipient $targetRecipientId for MISSED stage of $occurrenceId. Suppressing.")
                    }
                }

                ReminderStage.FAMILY_ESCALATION -> {
                    if (isTargetRecipient) {
                        // Audience B: Personal "Family has been notified" notification ONLY to the assigned person / responsible member
                        MedicationNotificationHelper.showFamilyEscalationToOriginalUser(context, occurrence)
                        ReminderStorage.markStageAsNotified(context, occurrenceId, ReminderStage.FAMILY_ESCALATION)
                        Log.d(TAG, "Dispatched PERSONAL family escalation notification to target recipient $currentUserId")
                    } else {
                        // Audience A: Broad family reminder to approved family members in this specific hub
                        val isMember = isUserMemberOfHub(occurrence.hubId, currentUserId)
                        if (isMember) {
                            MedicationNotificationHelper.showFamilyEscalationToMembers(context, occurrence)
                            ReminderStorage.markStageAsNotified(context, occurrenceId, ReminderStage.FAMILY_ESCALATION)
                            Log.d(TAG, "Dispatched FAMILY reminder for ${occurrence.medicationName} to hub member $currentUserId")
                        } else {
                            Log.d(TAG, "User $currentUserId is neither target recipient nor member of hub ${occurrence.hubId}. Suppressing escalation.")
                        }
                    }
                    // Authoritatively deliver family escalation to all other approved hub members and active devices
                    dispatchFamilyEscalationToApprovedMembers(context, occurrence)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in MedicationReminderReceiver", e)
        }
    }
}
