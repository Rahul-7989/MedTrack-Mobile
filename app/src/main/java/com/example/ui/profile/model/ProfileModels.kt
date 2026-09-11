package com.example.ui.profile.model

import com.example.ui.profilesetup.model.ProfileAvatarType
import com.example.ui.profilesetup.model.ProfileGender

/**
 * User's role inside a specific Family Hub.
 */
enum class HubUserRole(val label: String) {
    CREATOR("Creator"),
    MEMBER("Member")
}

/**
 * Summary information for a Family Hub displayed in the Profile page.
 */
data class UserHubSummary(
    val hubId: String,
    val name: String,
    val hiveCode: String,
    val role: HubUserRole,
    val createdByUid: String?,
    val missedDosageReminderMinutes: Int = 5,
    val familyNotificationReminderMinutes: Int = 10
)

/**
 * Child / Dependent Profile associated with a specific Family Hub.
 */
data class ChildProfileData(
    val childId: String = "",
    val hubId: String = "",
    val name: String = "",
    val gender: ProfileGender = ProfileGender.MALE,
    val avatarType: ProfileAvatarType = ProfileAvatarType.CHILD_MALE,
    val reminderResponsibleMemberId: String = "",
    val reminderResponsibleMemberName: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Standard minute options available for Hub Reminder settings.
 */
val REMINDER_MINUTE_OPTIONS = listOf(1, 5, 10, 15, 20, 30, 45, 60)

