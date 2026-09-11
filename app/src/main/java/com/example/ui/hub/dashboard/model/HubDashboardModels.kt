package com.example.ui.hub.dashboard.model

import com.example.ui.profilesetup.model.ProfileAvatarType
import com.example.ui.profilesetup.model.ProfileGender

/**
 * Reminder frequency cycle for medication scheduling.
 */
enum class ReminderCycle(val label: String) {
    EVERY_24_HOURS("Every 24 hrs"),
    EVERY_48_HOURS("Every 48 hrs"),
    CUSTOM("Custom")
}

/**
 * Approved member belonging to the current Family Hub.
 * Can represent an adult authenticated member or a dependent child profile.
 */
data class HubMember(
    val id: String,
    val name: String,
    val avatarType: ProfileAvatarType,
    val gender: ProfileGender? = null,
    val isCreator: Boolean = false,
    val isChild: Boolean = false,
    val reminderResponsibleMemberId: String? = null,
    val reminderResponsibleMemberName: String? = null
)

/**
 * States supported for Family Hub Join Requests.
 */
enum class JoinRequestStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    CANCELLED
}

/**
 * Detailed Join Request record with status and hub reference.
 */
data class JoinRequestItemData(
    val id: String,
    val hubId: String,
    val hubName: String,
    val userId: String,
    val userName: String,
    val avatarType: ProfileAvatarType,
    val status: JoinRequestStatus = JoinRequestStatus.PENDING,
    val requestedAt: Long = System.currentTimeMillis()
)

/**
 * Pending join request waiting for approval by the Hub Creator.
 */
data class HubJoinRequest(
    val id: String,
    val userId: String,
    val userName: String,
    val avatarType: ProfileAvatarType,
    val requestedAt: Long = System.currentTimeMillis(),
    val status: JoinRequestStatus = JoinRequestStatus.PENDING
)

/**
 * Individual medication scheduled within the family hub.
 */
data class MedicationItem(
    val id: String,
    val hubId: String,
    val name: String,
    val dosage: String,
    val recipientId: String,
    val recipientName: String,
    val recipientAvatarType: ProfileAvatarType,
    val reminderTime: String,
    val reminderHour: Int = 9,
    val reminderMinute: Int = 0,
    val reminderCycle: ReminderCycle = ReminderCycle.EVERY_24_HOURS,
    val customIntervalDays: Int = 1,
    val customDaysOfWeek: List<String> = emptyList(),
    val notes: String? = null,
    val imageUri: String? = null,
    val createdByUid: String,
    val isTakenToday: Boolean = false,
    val takenAtTime: String? = null,
    val reminderResponsibleUid: String? = null,
    val isChildRecipient: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
