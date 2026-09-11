package com.example.reminder.model

/**
 * Stages in the medication reminder escalation timeline.
 */
enum class ReminderStage {
    /** First scheduled reminder at exact medication time (e.g., 9:00 AM). */
    INITIAL,

    /** Second reminder after missed dose interval (e.g., 9:05 AM for a 5-min setting). */
    MISSED,

    /** Final escalation to family members & original recipient after family interval (e.g., 9:15 AM). */
    FAMILY_ESCALATION
}
