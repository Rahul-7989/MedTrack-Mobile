package com.example.reminder.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.MainActivity
import com.example.reminder.data.ReminderStorage
import com.example.reminder.model.MedicationOccurrence
import com.example.reminder.model.ReminderStage
import com.example.reminder.receiver.MedicationReminderReceiver
import com.example.ui.hub.dashboard.model.MedicationItem
import com.example.ui.hub.dashboard.model.ReminderCycle
import com.example.reminder.util.ReminderCycleUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Robust AlarmManager-backed scheduler that manages exact medication alarms
 * for Initial Reminders, Missed Dosage alerts, and Family Escalations.
 */
object MedicationReminderScheduler {

    private const val TAG = "MedReminderScheduler"

    const val ACTION_TRIGGER_REMINDER = "com.example.reminder.ACTION_TRIGGER_REMINDER"
    const val EXTRA_HUB_ID = "extra_hub_id"
    const val EXTRA_MEDICATION_ID = "extra_medication_id"
    const val EXTRA_OCCURRENCE_ID = "extra_occurrence_id"
    const val EXTRA_DATE_KEY = "extra_date_key"
    const val EXTRA_STAGE = "extra_stage"
    const val EXTRA_MED_NAME = "extra_med_name"
    const val EXTRA_DOSAGE = "extra_dosage"
    const val EXTRA_RECIPIENT_ID = "extra_recipient_id"
    const val EXTRA_RECIPIENT_NAME = "extra_recipient_name"
    const val EXTRA_RESPONSIBLE_UID = "extra_responsible_uid"
    const val EXTRA_IS_CHILD = "extra_is_child"

    fun getTodayDateKey(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return formatter.format(Date())
    }

    /**
     * Checks if a medication is scheduled to be taken on today's calendar date according to its reminder cycle.
     */
    fun isMedicationScheduledForDate(medication: MedicationItem, calendar: Calendar): Boolean {
        when (medication.reminderCycle) {
            ReminderCycle.EVERY_24_HOURS -> return true
            ReminderCycle.EVERY_48_HOURS -> {
                // Every 48h based on creation timestamp day parity
                val creationCal = Calendar.getInstance().apply { timeInMillis = medication.createdAt }
                val dayDiff = Math.abs(calendar.get(Calendar.DAY_OF_YEAR) - creationCal.get(Calendar.DAY_OF_YEAR))
                return dayDiff % 2 == 0
            }
            ReminderCycle.CUSTOM -> {
                if (medication.customDaysOfWeek.isNotEmpty()) {
                    val dayName = SimpleDateFormat("EEE", Locale.US).format(calendar.time).uppercase()
                    return medication.customDaysOfWeek.any { it.startsWith(dayName, ignoreCase = true) }
                }
                if (medication.customIntervalDays > 1) {
                    val creationCal = Calendar.getInstance().apply { timeInMillis = medication.createdAt }
                    val dayDiff = Math.abs(calendar.get(Calendar.DAY_OF_YEAR) - creationCal.get(Calendar.DAY_OF_YEAR))
                    return dayDiff % medication.customIntervalDays == 0
                }
                return true
            }
        }
    }

    /**
     * Creates a unique request code for the AlarmManager PendingIntent.
     */
    fun getAlarmRequestCode(medicationId: String, dateKey: String, stage: ReminderStage): Int {
        var code = medicationId.hashCode() * 31 + dateKey.hashCode()
        code = code * 31 + stage.ordinal
        return Math.abs(code)
    }

    private fun createAlarmPendingIntent(
        context: Context,
        occurrence: MedicationOccurrence,
        stage: ReminderStage
    ): PendingIntent {
        val intent = Intent(context, MedicationReminderReceiver::class.java).apply {
            action = ACTION_TRIGGER_REMINDER
            putExtra(EXTRA_HUB_ID, occurrence.hubId)
            putExtra(EXTRA_MEDICATION_ID, occurrence.medicationId)
            putExtra(EXTRA_OCCURRENCE_ID, occurrence.occurrenceId)
            putExtra(EXTRA_DATE_KEY, occurrence.dateKey)
            putExtra(EXTRA_STAGE, stage.name)
            putExtra(EXTRA_MED_NAME, occurrence.medicationName)
            putExtra(EXTRA_DOSAGE, occurrence.dosage)
            putExtra(EXTRA_RECIPIENT_ID, occurrence.recipientId)
            putExtra(EXTRA_RECIPIENT_NAME, occurrence.recipientName)
            putExtra(EXTRA_RESPONSIBLE_UID, occurrence.reminderResponsibleUid)
            putExtra(EXTRA_IS_CHILD, occurrence.isChildRecipient)
        }
        val requestCode = getAlarmRequestCode(occurrence.medicationId, occurrence.dateKey, stage)
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Schedules an exact alarm with the system AlarmManager using setAlarmClock for highest reliability.
     */
    private fun setExactAlarm(context: Context, triggerAtMillis: Long, pendingIntent: PendingIntent) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val now = System.currentTimeMillis()
        if (triggerAtMillis <= now) return

        try {
            val showIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val showPendingIntent = PendingIntent.getActivity(
                context,
                0,
                showIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerAtMillis, showPendingIntent)
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            Log.d(TAG, "Scheduled exact alarm at $triggerAtMillis (in ${(triggerAtMillis - now) / 1000}s)")
        } catch (e: SecurityException) {
            // Fallback if exact alarms permission is strictly restricted
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                }
            } catch (err: Exception) {
                Log.e(TAG, "Failed to schedule alarm", err)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule alarm", e)
        }
    }

    /**
     * Schedules alarms for a medication occurrence based on audience eligibility:
     * - Assigned recipient / responsible adult: Initial, Missed, and Personal Family Escalation.
     * - Family hub members: Family Escalation reminder only.
     */
    fun scheduleOccurrenceAlarms(context: Context, occurrence: MedicationOccurrence) {
        if (occurrence.isTaken) {
            cancelOccurrenceAlarms(context, occurrence.medicationId, occurrence.dateKey)
            return
        }

        val now = System.currentTimeMillis()
        val currentUserId = MedicationReminderReceiver.resolveCurrentUserId()
        val targetRecipientId = MedicationReminderReceiver.resolveTargetPersonalRecipientId(occurrence)
        val isTargetRecipient = (currentUserId.isNotBlank() && currentUserId == targetRecipientId)

        // 1. Initial Reminder (strictly for target recipient)
        if (isTargetRecipient && occurrence.scheduledTimeMillis > now && !ReminderStorage.hasStageBeenNotified(context, occurrence.occurrenceId, ReminderStage.INITIAL)) {
            val initialIntent = createAlarmPendingIntent(context, occurrence, ReminderStage.INITIAL)
            setExactAlarm(context, occurrence.scheduledTimeMillis, initialIntent)
        }

        // 2. Missed Dosage Reminder (strictly for target recipient)
        if (isTargetRecipient && occurrence.missedReminderMillis > now && !ReminderStorage.hasStageBeenNotified(context, occurrence.occurrenceId, ReminderStage.MISSED)) {
            val missedIntent = createAlarmPendingIntent(context, occurrence, ReminderStage.MISSED)
            setExactAlarm(context, occurrence.missedReminderMillis, missedIntent)
        }

        // 3. Family Escalation (target recipient receives personal alert, family members receive broad hub alert)
        if (occurrence.familyEscalationMillis > now && !ReminderStorage.hasStageBeenNotified(context, occurrence.occurrenceId, ReminderStage.FAMILY_ESCALATION)) {
            val familyIntent = createAlarmPendingIntent(context, occurrence, ReminderStage.FAMILY_ESCALATION)
            setExactAlarm(context, occurrence.familyEscalationMillis, familyIntent)
        }
    }

    /**
     * Cancels all pending AlarmManager intents for an occurrence.
     */
    fun cancelOccurrenceAlarms(context: Context, medicationId: String, dateKey: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        for (stage in ReminderStage.values()) {
            val requestCode = getAlarmRequestCode(medicationId, dateKey, stage)
            val intent = Intent(context, MedicationReminderReceiver::class.java).apply {
                action = ACTION_TRIGGER_REMINDER
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
    }

    /**
     * Builds and schedules daily occurrences for all medications belonging to an active hub.
     */
    fun syncMedications(
        context: Context,
        hubId: String,
        medications: List<MedicationItem>,
        missedDosageMinutes: Int = 5,
        familyNotificationMinutes: Int = 15
    ) {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        val occurrences = mutableListOf<MedicationOccurrence>()

        for (med in medications) {
            if (!isMedicationScheduledForDate(med, calendar)) continue

            val (cycleStartMs, cycleEndMs) = ReminderCycleUtils.getCycleStartAndEnd(
                createdAt = med.createdAt,
                cycle = med.reminderCycle,
                customIntervalDays = med.customIntervalDays,
                now = now
            )
            val cycleDateKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(cycleStartMs))
            val isTakenInCycle = med.isTakenToday && med.takenAtMillis > 0L && med.takenAtMillis >= cycleStartMs
            val takenTimeFormatted = if (isTakenInCycle) med.takenAtTime else null

            val schedCal = Calendar.getInstance(TimeZone.getDefault()).apply {
                timeInMillis = cycleStartMs
                set(Calendar.HOUR_OF_DAY, med.reminderHour)
                set(Calendar.MINUTE, med.reminderMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val scheduledMillis = schedCal.timeInMillis
            val missedMillis = scheduledMillis + (missedDosageMinutes * 60_000L)
            val familyMillis = scheduledMillis + (familyNotificationMinutes * 60_000L)

            val occurrenceId = "${med.id}_$cycleDateKey"
            val occurrence = MedicationOccurrence(
                occurrenceId = occurrenceId,
                medicationId = med.id,
                hubId = hubId,
                dateKey = cycleDateKey,
                medicationName = med.name,
                dosage = med.dosage,
                recipientId = med.recipientId,
                recipientName = med.recipientName,
                recipientAvatarType = med.recipientAvatarType,
                reminderTimeFormatted = med.reminderTime,
                reminderHour = med.reminderHour,
                reminderMinute = med.reminderMinute,
                scheduledTimeMillis = scheduledMillis,
                missedReminderMillis = missedMillis,
                familyEscalationMillis = familyMillis,
                reminderResponsibleUid = med.reminderResponsibleUid,
                isChildRecipient = med.isChildRecipient,
                isTaken = isTakenInCycle,
                takenAtFormatted = takenTimeFormatted,
                takenAtMillis = if (isTakenInCycle) med.takenAtMillis else 0L
            )

            occurrences.add(occurrence)
            scheduleOccurrenceAlarms(context, occurrence)
        }

        ReminderStorage.saveOccurrences(context, occurrences)
    }
}
