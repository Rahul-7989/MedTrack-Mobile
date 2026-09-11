package com.example.reminder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.auth.FirebaseAuthService
import com.example.reminder.data.ReminderStorage
import com.example.reminder.scheduler.MedicationReminderScheduler
import com.example.ui.hub.dashboard.model.MedicationItem
import com.example.ui.hub.dashboard.model.ReminderCycle
import com.example.ui.profilesetup.model.ProfileAvatarType
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Restores and resynchronizes scheduled medication alarms when the Android device reboots
 * or the application is updated/replaced.
 */
class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == "android.intent.action.QUICKBOOT_POWERON" ||
            action == "com.htc.intent.action.QUICKBOOT_POWERON"
        ) {
            Log.d(TAG, "Device rebooted or app updated. Restoring medication reminder alarms...")

            val pendingResult = goAsync()

            // 1. Immediately restore cached occurrences from local storage
            val localOccurrences = ReminderStorage.loadOccurrences(context)
            for (occ in localOccurrences) {
                if (!occ.isTaken) {
                    MedicationReminderScheduler.scheduleOccurrenceAlarms(context, occ)
                }
            }

            // 2. Fetch latest active hub from Firestore and re-synchronize
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val currentUser = FirebaseAuthService.Instance.currentUser
                    if (currentUser != null) {
                        val firestore = FirebaseFirestore.getInstance()
                        val userDoc = firestore.collection("users").document(currentUser.uid).get().await()
                        val hubId = userDoc?.getString("currentHubId")

                        if (hubId != null) {
                            val hubDoc = firestore.collection("family_hubs").document(hubId).get().await()
                            val missedMins = (hubDoc?.getLong("missedDosageReminderMinutes") ?: 5L).toInt()
                            val familyMins = (hubDoc?.getLong("familyNotificationReminderMinutes") ?: 15L).toInt()

                            val medSnapshot = firestore.collection("family_hubs")
                                .document(hubId)
                                .collection("medications")
                                .get()
                                .await()

                            val meds = medSnapshot.documents.mapNotNull { doc ->
                                try {
                                    val cycleStr = doc.getString("reminderCycle") ?: ReminderCycle.EVERY_24_HOURS.name
                                    val avatarStr = doc.getString("recipientAvatarType") ?: "MALE"
                                    MedicationItem(
                                        id = doc.getString("id") ?: doc.id,
                                        hubId = hubId,
                                        name = doc.getString("name") ?: "",
                                        dosage = doc.getString("dosage") ?: "",
                                        recipientId = doc.getString("recipientId") ?: "",
                                        recipientName = doc.getString("recipientName") ?: "",
                                        recipientAvatarType = try { ProfileAvatarType.valueOf(avatarStr) } catch (_: Exception) { ProfileAvatarType.MALE },
                                        reminderTime = doc.getString("reminderTime") ?: "9:00 AM",
                                        reminderHour = (doc.getLong("reminderHour") ?: 9L).toInt(),
                                        reminderMinute = (doc.getLong("reminderMinute") ?: 0L).toInt(),
                                        reminderCycle = try { ReminderCycle.valueOf(cycleStr) } catch (_: Exception) { ReminderCycle.EVERY_24_HOURS },
                                        customIntervalDays = (doc.getLong("customIntervalDays") ?: 1L).toInt(),
                                        customDaysOfWeek = (doc.get("customDaysOfWeek") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                                        createdByUid = doc.getString("createdByUid") ?: "",
                                        isTakenToday = doc.getBoolean("isTakenToday") ?: false,
                                        takenAtTime = doc.getString("takenAtTime"),
                                        reminderResponsibleUid = doc.getString("reminderResponsibleUid"),
                                        isChildRecipient = doc.getBoolean("isChildRecipient") ?: false,
                                        createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                                    )
                                } catch (_: Exception) {
                                    null
                                }
                            }

                            MedicationReminderScheduler.syncMedications(
                                context = context,
                                hubId = hubId,
                                medications = meds,
                                missedDosageMinutes = missedMins,
                                familyNotificationMinutes = familyMins
                            )
                            Log.d(TAG, "Successfully restored ${meds.size} medication alarms from Firestore.")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error restoring alarms from Firestore on boot", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
