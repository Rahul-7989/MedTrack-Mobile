package com.example.reminder.model

import com.example.ui.profilesetup.model.ProfileAvatarType

/**
 * Represents a single daily or scheduled occurrence of a medication dose.
 *
 * Each occurrence has a deterministic identity: `${medicationId}_${dateKey}`.
 * This guarantees marking today's occurrence as taken does not affect tomorrow's or future occurrences.
 */
data class MedicationOccurrence(
    val occurrenceId: String,
    val medicationId: String,
    val hubId: String,
    val dateKey: String, // Format: YYYY-MM-DD
    val medicationName: String,
    val dosage: String,
    val recipientId: String,
    val recipientName: String,
    val recipientAvatarType: ProfileAvatarType = ProfileAvatarType.MALE,
    val reminderTimeFormatted: String = "9:00 AM",
    val reminderHour: Int = 9,
    val reminderMinute: Int = 0,
    val scheduledTimeMillis: Long,
    val missedReminderMillis: Long,
    val familyEscalationMillis: Long,
    val reminderResponsibleUid: String? = null,
    val isChildRecipient: Boolean = false,
    val isTaken: Boolean = false,
    val takenAtFormatted: String? = null,
    val takenAtMillis: Long? = null,
    val lastNotifiedStage: ReminderStage? = null
)
