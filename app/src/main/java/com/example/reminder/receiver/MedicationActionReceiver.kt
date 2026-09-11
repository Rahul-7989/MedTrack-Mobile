package com.example.reminder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.reminder.data.ReminderStorage
import com.example.reminder.notification.MedicationNotificationHelper
import com.example.reminder.scheduler.MedicationReminderScheduler
import com.example.ui.hub.dashboard.data.HubDashboardRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * BroadcastReceiver triggered when the user taps "Mark as taken" directly on an Android Notification.
 *
 * Fully functions in the background even when the MedTrack app is minimized, closed, or killed.
 * Updates Firestore, cancels pending escalation alarms, dismisses active notifications, and updates local state.
 */
class MedicationActionReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "MedActionReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != MedicationNotificationHelper.ACTION_MARK_TAKEN) return

        val hubId = intent.getStringExtra(MedicationNotificationHelper.EXTRA_HUB_ID) ?: return
        val medicationId = intent.getStringExtra(MedicationNotificationHelper.EXTRA_MEDICATION_ID) ?: return
        val occurrenceId = intent.getStringExtra(MedicationNotificationHelper.EXTRA_OCCURRENCE_ID) ?: "${medicationId}_${MedicationReminderScheduler.getTodayDateKey()}"
        val dateKey = intent.getStringExtra(MedicationNotificationHelper.EXTRA_DATE_KEY) ?: MedicationReminderScheduler.getTodayDateKey()
        val notificationId = intent.getIntExtra(MedicationNotificationHelper.EXTRA_NOTIFICATION_ID, -1)

        val pendingResult = goAsync()

        val timeFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())
        val nowFormatted = timeFormatter.format(Date())
        val nowMillis = System.currentTimeMillis()

        // 1. Immediately dismiss active notifications for this occurrence
        if (notificationId != -1) {
            MedicationNotificationHelper.cancelNotification(context, notificationId)
        }
        val occurrence = ReminderStorage.getOccurrence(context, medicationId, dateKey)
        if (occurrence != null) {
            MedicationNotificationHelper.cancelOccurrenceNotifications(context, occurrence)
        }

        // 2. Immediately cancel pending Missed Dosage & Family Escalation alarms
        MedicationReminderScheduler.cancelOccurrenceAlarms(context, medicationId, dateKey)

        // 3. Update local storage
        ReminderStorage.markOccurrenceAsTaken(context, medicationId, dateKey, nowFormatted)

        // 4. Update in-memory dashboard repository if present
        try {
            HubDashboardRepository.toggleMedicationTaken(medicationId)
        } catch (_: Exception) {}

        // 5. Update Firebase Firestore in background coroutine
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val firestore = FirebaseFirestore.getInstance()

                // Update parent medication document
                firestore.collection("family_hubs")
                    .document(hubId)
                    .collection("medications")
                    .document(medicationId)
                    .update(
                        mapOf(
                            "isTakenToday" to true,
                            "takenAtTime" to nowFormatted,
                            "updatedAt" to nowMillis
                        )
                    )
                    .await()

                // Record occurrence document for persistent history
                val occMap = hashMapOf(
                    "occurrenceId" to occurrenceId,
                    "medicationId" to medicationId,
                    "hubId" to hubId,
                    "dateKey" to dateKey,
                    "taken" to true,
                    "takenAt" to nowFormatted,
                    "takenAtTimestamp" to nowMillis
                )
                firestore.collection("family_hubs")
                    .document(hubId)
                    .collection("medications")
                    .document(medicationId)
                    .collection("occurrences")
                    .document(occurrenceId)
                    .set(occMap, SetOptions.merge())
                    .await()

                Log.d(TAG, "Successfully marked medication $medicationId ($occurrenceId) as taken at $nowFormatted in Firestore.")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to persist Mark as Taken to Firestore", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
