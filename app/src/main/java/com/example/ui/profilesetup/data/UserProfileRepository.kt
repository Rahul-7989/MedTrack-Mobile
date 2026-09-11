package com.example.ui.profilesetup.data

import com.example.data.auth.FirebaseAuthService
import com.example.ui.hub.data.FamilyHubRepository
import com.example.ui.profilesetup.model.ProfileAvatarType
import com.example.ui.profilesetup.model.ProfileGender
import com.example.ui.profilesetup.model.UserProfileData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Repository managing the user's basic family profile state with Firestore database persistence.
 *
 * Ensures that incomplete profiles are never treated as completed, and synchronizes
 * the profile data with Firebase Firestore when an authenticated user is present.
 */
object UserProfileRepository {
    private val _userProfile = MutableStateFlow<UserProfileData?>(null)
    val userProfile: StateFlow<UserProfileData?> = _userProfile.asStateFlow()

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    val isProfileComplete: Boolean
        get() = _userProfile.value?.isCompleted == true

    fun saveProfile(profile: UserProfileData) {
        _userProfile.value = profile

        // Sync with Firebase Firestore
        val currentUser = FirebaseAuthService.Instance.currentUser
        if (currentUser != null) {
            repositoryScope.launch {
                try {
                    val firestore = FirebaseFirestore.getInstance()
                    val profileMap = hashMapOf(
                        "uid" to currentUser.uid,
                        "email" to (currentUser.email ?: ""),
                        "name" to profile.name,
                        "gender" to profile.gender.name,
                        "age" to profile.age,
                        "aboutMe" to (profile.aboutMe ?: ""),
                        "avatarType" to profile.avatarType.name,
                        "isCompleted" to profile.isCompleted,
                        "updatedAt" to System.currentTimeMillis()
                    )
                    firestore.collection("users")
                        .document(currentUser.uid)
                        .set(profileMap, SetOptions.merge())

                    // If user is currently in a hub, update their member profile record in the hub subcollection
                    val currentHub = FamilyHubRepository.currentHub.value
                    if (currentHub != null) {
                        val memberUpdate = hashMapOf(
                            "id" to currentUser.uid,
                            "userId" to currentUser.uid,
                            "name" to profile.name,
                            "avatarType" to profile.avatarType.name,
                            "gender" to profile.gender.name,
                            "updatedAt" to System.currentTimeMillis()
                        )
                        firestore.collection("family_hubs")
                            .document(currentHub.hubId)
                            .collection("members")
                            .document(currentUser.uid)
                            .set(memberUpdate, SetOptions.merge())
                    }
                } catch (_: Exception) {
                    // Non-blocking fallback
                }
            }
        }
    }

    suspend fun loadProfile(): UserProfileData? = withContext(Dispatchers.IO) {
        val currentUser = FirebaseAuthService.Instance.currentUser ?: return@withContext _userProfile.value
        try {
            val firestore = FirebaseFirestore.getInstance()
            val snapshot = firestore.collection("users")
                .document(currentUser.uid)
                .get()
                .await()

            if (snapshot != null && snapshot.exists()) {
                val name = snapshot.getString("name") ?: ""
                val genderStr = snapshot.getString("gender") ?: ProfileGender.PREFER_NOT_TO_SAY.name
                val gender = try {
                    ProfileGender.valueOf(genderStr)
                } catch (_: Exception) {
                    ProfileGender.PREFER_NOT_TO_SAY
                }
                val age = (snapshot.getLong("age") ?: 30L).toInt()
                val aboutMe = snapshot.getString("aboutMe")
                val isCompleted = snapshot.getBoolean("isCompleted") ?: false
                val avatarStr = snapshot.getString("avatarType") ?: ProfileAvatarType.MALE.name
                val avatarType = try {
                    ProfileAvatarType.valueOf(avatarStr)
                } catch (_: Exception) {
                    ProfileAvatarType.MALE
                }

                val profile = UserProfileData(
                    name = name,
                    gender = gender,
                    age = age,
                    aboutMe = aboutMe,
                    avatarType = avatarType,
                    isCompleted = isCompleted
                )
                _userProfile.value = profile
                return@withContext profile
            }
        } catch (_: Exception) {
            // Offline fallback
        }
        return@withContext _userProfile.value
    }

    fun loadProfileFromFirestore(onComplete: (UserProfileData?) -> Unit = {}) {
        val currentUser = FirebaseAuthService.Instance.currentUser ?: run {
            onComplete(_userProfile.value)
            return
        }
        repositoryScope.launch {
            val profile = loadProfile()
            FamilyHubRepository.loadUserHubFromFirestore()
            onComplete(profile)
        }
    }

    fun clearProfile() {
        _userProfile.value = null
    }
}
