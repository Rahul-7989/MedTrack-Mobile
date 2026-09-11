package com.example.ui.hub.model

/**
 * Data model representing a Family Hub ("Family Hive") in MedTrack.
 */
data class FamilyHubData(
    val hubId: String,
    val name: String,
    val hiveCode: String,
    val createdByUid: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val membersCount: Int = 1,
    val missedDosageReminderMinutes: Int = 5,
    val familyNotificationReminderMinutes: Int = 10
)

/**
 * Result returned from Hub Code validation and join request submission.
 */
sealed class HubJoinValidationResult {
    data class SuccessPending(
        val hub: FamilyHubData,
        val requestId: String
    ) : HubJoinValidationResult()

    data class SuccessAlreadyMember(
        val hub: FamilyHubData
    ) : HubJoinValidationResult()

    data class Error(
        val title: String,
        val message: String
    ) : HubJoinValidationResult()
}
