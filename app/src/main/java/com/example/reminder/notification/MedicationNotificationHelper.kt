package com.example.reminder.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.reminder.model.MedicationOccurrence
import com.example.reminder.model.ReminderStage
import com.example.reminder.receiver.MedicationActionReceiver

/**
 * Handles creation of the Medication Reminders notification channel, building rich Android
 * notifications with "Mark as taken" background actions, and dispatching deterministic notifications.
 */
object MedicationNotificationHelper {

    const val CHANNEL_ID = "medication_reminders_channel"
    private const val CHANNEL_NAME = "Medication Reminders"
    private const val CHANNEL_DESC = "Scheduled medication alerts, missed-dose reminders, and family escalations."

    const val ACTION_MARK_TAKEN = "com.example.reminder.ACTION_MARK_TAKEN"
    const val EXTRA_HUB_ID = "extra_hub_id"
    const val EXTRA_MEDICATION_ID = "extra_medication_id"
    const val EXTRA_OCCURRENCE_ID = "extra_occurrence_id"
    const val EXTRA_DATE_KEY = "extra_date_key"
    const val EXTRA_NOTIFICATION_ID = "extra_notification_id"

    const val EXTRA_NAV_HUB_ID = "nav_hub_id"
    const val EXTRA_NAV_MEDICATION_ID = "nav_medication_id"

    /**
     * Creates and registers the dedicated Notification Channel on Android 8.0 (API 26) and above.
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

            val existing = notificationManager.getNotificationChannel(CHANNEL_ID)
            if (existing == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = CHANNEL_DESC
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 350, 200, 350, 200, 450)
                    enableLights(true)
                    setShowBadge(true)
                    lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                }
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    /**
     * Generates a deterministic notification ID based on occurrence and stage.
     */
    fun getNotificationId(occurrence: MedicationOccurrence, stage: ReminderStage): Int {
        var result = occurrence.hubId.hashCode()
        result = 31 * result + occurrence.medicationId.hashCode()
        result = 31 * result + occurrence.dateKey.hashCode()
        result = 31 * result + stage.ordinal
        return Math.abs(result)
    }

    /**
     * Builds a PendingIntent to open the app on the relevant Hub Dashboard when the notification is tapped.
     */
    private fun createContentIntent(context: Context, occurrence: MedicationOccurrence): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_NAV_HUB_ID, occurrence.hubId)
            putExtra(EXTRA_NAV_MEDICATION_ID, occurrence.medicationId)
        }
        val requestCode = (occurrence.medicationId.hashCode() xor occurrence.dateKey.hashCode()).let { Math.abs(it) }
        return PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Builds a PendingIntent for the direct "Mark as taken" notification action button.
     */
    private fun createMarkTakenActionPendingIntent(
        context: Context,
        occurrence: MedicationOccurrence,
        notificationId: Int
    ): PendingIntent {
        val intent = Intent(context, MedicationActionReceiver::class.java).apply {
            action = ACTION_MARK_TAKEN
            putExtra(EXTRA_HUB_ID, occurrence.hubId)
            putExtra(EXTRA_MEDICATION_ID, occurrence.medicationId)
            putExtra(EXTRA_OCCURRENCE_ID, occurrence.occurrenceId)
            putExtra(EXTRA_DATE_KEY, occurrence.dateKey)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        val requestCode = notificationId + 100000
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Displays the initial scheduled reminder notification at the medication's exact time.
     */
    fun showInitialReminder(context: Context, occurrence: MedicationOccurrence) {
        createNotificationChannel(context)
        val notificationId = getNotificationId(occurrence, ReminderStage.INITIAL)

        val title = if (occurrence.isChildRecipient) {
            "💊 ${occurrence.recipientName}’s medicine time"
        } else {
            "💊 Time for your medicine"
        }

        val body = if (occurrence.isChildRecipient) {
            "It’s time for ${occurrence.recipientName} to take their ${occurrence.medicationName}.\nGive them ${occurrence.dosage} and mark it as taken when they’re done."
        } else {
            "It’s time to take your ${occurrence.medicationName}.\nTake ${occurrence.dosage} and mark it as taken when you’re done."
        }

        val markTakenAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_agenda,
            "Mark as taken",
            createMarkTakenActionPendingIntent(context, occurrence, notificationId)
        ).build()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(createContentIntent(context, occurrence))
            .addAction(markTakenAction)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setVibrate(longArrayOf(0, 350, 200, 350, 200, 450))
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: SecurityException) {}
    }

    /**
     * Displays the missed dosage reminder notification after the hub's missed interval.
     */
    fun showMissedDosageReminder(context: Context, occurrence: MedicationOccurrence) {
        createNotificationChannel(context)
        val notificationId = getNotificationId(occurrence, ReminderStage.MISSED)

        val title = if (occurrence.isChildRecipient) {
            "⏰ ${occurrence.recipientName}’s medicine reminder"
        } else {
            "⏰ Medicine reminder"
        }

        val body = if (occurrence.isChildRecipient) {
            "${occurrence.recipientName}’s ${occurrence.medicationName} hasn’t been marked as taken yet.\nPlease check on them, give them their medicine, and mark it as taken."
        } else {
            "Have you taken your ${occurrence.medicationName}?\nIt hasn’t been marked as taken yet. Please take it and remember to mark it."
        }

        val markTakenAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_agenda,
            "Mark as taken",
            createMarkTakenActionPendingIntent(context, occurrence, notificationId)
        ).build()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(createContentIntent(context, occurrence))
            .addAction(markTakenAction)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
            .setVibrate(longArrayOf(0, 500, 250, 500))
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: SecurityException) {}
    }

    /**
     * Displays family escalation notification to the original recipient / responsible member.
     */
    fun showFamilyEscalationToOriginalUser(context: Context, occurrence: MedicationOccurrence) {
        createNotificationChannel(context)
        val notificationId = getNotificationId(occurrence, ReminderStage.FAMILY_ESCALATION)

        val title = if (occurrence.isChildRecipient) {
            "🔔 Family has been notified"
        } else {
            "🔔 Your family has been notified"
        }

        val body = if (occurrence.isChildRecipient) {
            "We’ve let your family know about ${occurrence.recipientName}’s ${occurrence.medicationName}.\nPlease make sure ${occurrence.recipientName} takes their medicine and mark it when they’re done."
        } else {
            "Your ${occurrence.medicationName} hasn’t been marked as taken yet.\nWe’ve let your family know so they can remind you. Please take your medicine and mark it when you’re done."
        }

        val markTakenAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_agenda,
            "Mark as taken",
            createMarkTakenActionPendingIntent(context, occurrence, notificationId)
        ).build()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(createContentIntent(context, occurrence))
            .addAction(markTakenAction)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: SecurityException) {}
    }

    /**
     * Displays family escalation notification to approved hub members.
     */
    fun showFamilyEscalationToMembers(context: Context, occurrence: MedicationOccurrence) {
        createNotificationChannel(context)
        // Offset ID so it doesn't collide with recipient notification
        val notificationId = getNotificationId(occurrence, ReminderStage.FAMILY_ESCALATION) + 50000

        val title = "👨👩👧 Family reminder"

        val body = if (occurrence.isChildRecipient) {
            "${occurrence.recipientName} hasn’t taken their ${occurrence.medicationName} yet.\nPlease check in and make sure they take their medicine."
        } else {
            "${occurrence.recipientName} hasn’t marked their ${occurrence.medicationName} as taken yet.\nPlease check in with them and remind them about their medicine."
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(createContentIntent(context, occurrence))
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: SecurityException) {}
    }

    /**
     * Cancels all notifications (Initial, Missed, Family) for an occurrence when marked as taken.
     */
    fun cancelOccurrenceNotifications(context: Context, occurrence: MedicationOccurrence) {
        val manager = NotificationManagerCompat.from(context)
        manager.cancel(getNotificationId(occurrence, ReminderStage.INITIAL))
        manager.cancel(getNotificationId(occurrence, ReminderStage.MISSED))
        manager.cancel(getNotificationId(occurrence, ReminderStage.FAMILY_ESCALATION))
        manager.cancel(getNotificationId(occurrence, ReminderStage.FAMILY_ESCALATION) + 50000)
    }

    /**
     * Cancels a specific notification by ID.
     */
    fun cancelNotification(context: Context, notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }
}
