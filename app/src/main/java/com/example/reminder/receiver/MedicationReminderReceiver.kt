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
import com.example.ui.hub.dashboard.model.HubMember
import com.example.ui.profilesetup.model.ProfileAvatarType
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * BroadcastReceiver triggered by AlarmManager when a medication reminder alarm fires.
 *
 * Responsibilities:
 * - Authoritatively queries Firebase Firestore to check if medication was already marked as taken.
 * - Suppresses missed dosage and family escalation if the medication is taken.
 * - Renders dynamic Android notifications for adults and child dependents with "Mark as taken" actions.
 * - Dispatches family escalation alerts strictly to approved members of the specific hub.
 */
class MedicationReminderReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "MedReminderReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != MedicationReminderScheduler.ACTION_TRIGGER_REMINDER) return

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

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. Check idempotency: avoid duplicate notification dispatch
                if (ReminderStorage.hasStageBeenNotified(context, occurrenceId, stage)) {
                    Log.d(TAG, "Stage $stage already notified for $occurrenceId, skipping.")
                    return@launch
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
                    return@launch
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

                // 4. Dispatch Android Notification based on Stage
                when (stage) {
                    ReminderStage.INITIAL -> {
                        MedicationNotificationHelper.showInitialReminder(context, occurrence)
                        ReminderStorage.markStageAsNotified(context, occurrenceId, ReminderStage.INITIAL)
                    }

                    ReminderStage.MISSED -> {
                        MedicationNotificationHelper.showMissedDosageReminder(context, occurrence)
                        ReminderStorage.markStageAsNotified(context, occurrenceId, ReminderStage.MISSED)
                    }

                    ReminderStage.FAMILY_ESCALATION -> {
                        // Notify original recipient or responsible adult
                        MedicationNotificationHelper.showFamilyEscalationToOriginalUser(context, occurrence)

                        // Also notify other approved family members in this hub
                        MedicationNotificationHelper.showFamilyEscalationToMembers(context, occurrence)

                        ReminderStorage.markStageAsNotified(context, occurrenceId, ReminderStage.FAMILY_ESCALATION)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in MedicationReminderReceiver", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
