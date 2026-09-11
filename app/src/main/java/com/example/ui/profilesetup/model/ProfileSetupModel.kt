package com.example.ui.profilesetup.model

/**
 * Gender options for MedTrack basic family profile.
 * Strictly limited to: Male, Female, and Prefer not to say.
 */
enum class ProfileGender(val label: String) {
    MALE("Male"),
    FEMALE("Female"),
    PREFER_NOT_TO_SAY("Prefer not to say")
}

/**
 * Exactly three dynamic avatar states derived from selected gender.
 */
enum class ProfileAvatarType {
    MALE,
    FEMALE,
    ANONYMOUS,
    CHILD_MALE,
    CHILD_FEMALE,
    CHILD_ANONYMOUS
}

/**
 * Maps gender selection to its corresponding dynamic adult avatar.
 */
fun ProfileGender?.toAvatarType(): ProfileAvatarType = when (this) {
    ProfileGender.MALE -> ProfileAvatarType.MALE
    ProfileGender.FEMALE -> ProfileAvatarType.FEMALE
    ProfileGender.PREFER_NOT_TO_SAY -> ProfileAvatarType.ANONYMOUS
    null -> ProfileAvatarType.ANONYMOUS
}

/**
 * Maps gender selection to its corresponding dynamic child avatar.
 */
fun ProfileGender?.toChildAvatarType(): ProfileAvatarType = when (this) {
    ProfileGender.MALE -> ProfileAvatarType.CHILD_MALE
    ProfileGender.FEMALE -> ProfileAvatarType.CHILD_FEMALE
    ProfileGender.PREFER_NOT_TO_SAY, null -> ProfileAvatarType.CHILD_ANONYMOUS
}

/**
 * User Profile Data model representing completed profile setup.
 */
data class UserProfileData(
    val name: String,
    val gender: ProfileGender,
    val age: Int,
    val aboutMe: String? = null,
    val avatarType: ProfileAvatarType = gender.toAvatarType(),
    val isCompleted: Boolean = true
)
