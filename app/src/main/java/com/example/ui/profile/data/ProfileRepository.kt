package com.example.ui.profile.data

import com.example.data.auth.FirebaseAuthService
import com.example.ui.hub.dashboard.data.HubDashboardRepository
import com.example.ui.hub.dashboard.model.HubMember
import com.example.ui.hub.data.FamilyHubRepository
import com.example.ui.hub.model.FamilyHubData
import com.example.ui.profile.model.ChildProfileData
import com.example.ui.profile.model.HubUserRole
import com.example.ui.profile.model.UserHubSummary
import com.example.ui.profilesetup.data.UserProfileRepository
import com.example.ui.profilesetup.model.ProfileAvatarType
import com.example.ui.profilesetup.model.ProfileGender
import com.example.ui.profilesetup.model.UserProfileData
import com.example.ui.profilesetup.model.toAvatarType
import com.example.ui.profilesetup.model.toChildAvatarType
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Repository providing data services specifically for the Profile page.
 * Strictly maintains data boundaries:
 * - Profile data belongs to USER
 * - Hub membership belongs to USER <-> HUB
 * - Reminder settings belong to HUB
 */
object ProfileRepository {

    suspend fun loadCurrentUserProfile(): UserProfileData? = withContext(Dispatchers.IO) {
        UserProfileRepository.loadProfile()
    }

    suspend fun saveUserProfile(
        name: String,
        gender: ProfileGender,
        age: Int,
        aboutMe: String?
    ): Result<UserProfileData> = withContext(Dispatchers.IO) {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Name is required"))
        }

        val clampedAge = age.coerceIn(18, 70)
        val cleanedAboutMe = aboutMe?.trim()?.take(200)?.ifBlank { null }
        val avatarType = gender.toAvatarType()

        val updatedProfile = UserProfileData(
            name = trimmedName,
            gender = gender,
            age = clampedAge,
            aboutMe = cleanedAboutMe,
            avatarType = avatarType,
            isCompleted = true
        )

        try {
            UserProfileRepository.saveProfile(updatedProfile)
            Result.success(updatedProfile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Loads all Family Hubs that the current user belongs to (as Creator or Member).
     */
    suspend fun loadUserHubs(): List<UserHubSummary> = withContext(Dispatchers.IO) {
        val currentUser = FirebaseAuthService.Instance.currentUser
        val hubsMap = mutableMapOf<String, UserHubSummary>()

        // 1. Check local/in-memory hubs first (for instant display and offline fallback)
        val currentActiveHub = FamilyHubRepository.currentHub.value
        if (currentActiveHub != null) {
            val isCreator = currentActiveHub.createdByUid == currentUser?.uid || currentActiveHub.createdByUid == "current_user_local"
            hubsMap[currentActiveHub.hubId] = UserHubSummary(
                hubId = currentActiveHub.hubId,
                name = currentActiveHub.name,
                hiveCode = currentActiveHub.hiveCode,
                role = if (isCreator) HubUserRole.CREATOR else HubUserRole.MEMBER,
                createdByUid = currentActiveHub.createdByUid,
                missedDosageReminderMinutes = currentActiveHub.missedDosageReminderMinutes,
                familyNotificationReminderMinutes = currentActiveHub.familyNotificationReminderMinutes
            )
        }

        if (currentUser == null) {
            return@withContext hubsMap.values.toList()
        }

        try {
            val firestore = FirebaseFirestore.getInstance()

            // 2. Query hubs created by the user
            val createdQuery = firestore.collection("family_hubs")
                .whereEqualTo("createdByUid", currentUser.uid)
                .get()
                .await()

            for (doc in createdQuery.documents) {
                val hubId = doc.getString("hubId") ?: doc.id
                val name = doc.getString("name") ?: "Family Hub"
                val hiveCode = doc.getString("hiveCode") ?: "AGKJNZ"
                val createdByUid = doc.getString("createdByUid") ?: currentUser.uid
                val missedDosage = (doc.getLong("missedDosageReminderMinutes") ?: 5L).toInt()
                val familyNotify = (doc.getLong("familyNotificationReminderMinutes") ?: 10L).toInt()
                val approvedAdults = loadHubApprovedMembers(hubId).filter { it.id != currentUser.uid }
                val membersCount = (approvedAdults.size + 1).coerceAtLeast(1)

                hubsMap[hubId] = UserHubSummary(
                    hubId = hubId,
                    name = name,
                    hiveCode = hiveCode,
                    role = HubUserRole.CREATOR,
                    createdByUid = createdByUid,
                    missedDosageReminderMinutes = missedDosage,
                    familyNotificationReminderMinutes = familyNotify,
                    membersCount = membersCount,
                    approvedAdultMembers = approvedAdults
                )
            }

            // 3. Query hubs where user is in members array
            val memberQuery = firestore.collection("family_hubs")
                .whereArrayContains("members", currentUser.uid)
                .get()
                .await()

            for (doc in memberQuery.documents) {
                val hubId = doc.getString("hubId") ?: doc.id
                val name = doc.getString("name") ?: "Family Hub"
                val hiveCode = doc.getString("hiveCode") ?: "AGKJNZ"
                val createdByUid = doc.getString("createdByUid")
                val isCreator = createdByUid == currentUser.uid
                val missedDosage = (doc.getLong("missedDosageReminderMinutes") ?: 5L).toInt()
                val familyNotify = (doc.getLong("familyNotificationReminderMinutes") ?: 10L).toInt()
                val approvedAdults = loadHubApprovedMembers(hubId).filter { it.id != currentUser.uid }
                val membersCount = (approvedAdults.size + 1).coerceAtLeast(1)

                hubsMap[hubId] = UserHubSummary(
                    hubId = hubId,
                    name = name,
                    hiveCode = hiveCode,
                    role = if (isCreator) HubUserRole.CREATOR else HubUserRole.MEMBER,
                    createdByUid = createdByUid,
                    missedDosageReminderMinutes = missedDosage,
                    familyNotificationReminderMinutes = familyNotify,
                    membersCount = membersCount,
                    approvedAdultMembers = approvedAdults
                )
            }
        } catch (_: Exception) {
            // Offline fallback keeps in-memory hubs
        }

        hubsMap.values.sortedWith(compareBy({ it.role != HubUserRole.CREATOR }, { it.name }))
    }

    /**
     * Updates reminder settings for a hub. Strictly enforces that only the Creator can update.
     */
    suspend fun updateHubReminderSettings(
        hubId: String,
        missedDosageMinutes: Int,
        familyNotificationMinutes: Int
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val currentUser = FirebaseAuthService.Instance.currentUser

        try {
            val firestore = FirebaseFirestore.getInstance()
            val hubDoc = firestore.collection("family_hubs").document(hubId).get().await()

            if (hubDoc != null && hubDoc.exists()) {
                val createdByUid = hubDoc.getString("createdByUid")
                // Enforce creator permission at backend/repository level
                if (currentUser != null && createdByUid != null && createdByUid != currentUser.uid) {
                    return@withContext Result.failure(IllegalStateException("Only the hub creator can modify reminder settings."))
                }

                firestore.collection("family_hubs").document(hubId).set(
                    mapOf(
                        "missedDosageReminderMinutes" to missedDosageMinutes,
                        "familyNotificationReminderMinutes" to familyNotificationMinutes,
                        "updatedAt" to System.currentTimeMillis()
                    ),
                    SetOptions.merge()
                ).await()
            }

            // Update in-memory active hub if currently selected
            val current = FamilyHubRepository.currentHub.value
            if (current != null && current.hubId == hubId) {
                val updated = current.copy(
                    missedDosageReminderMinutes = missedDosageMinutes,
                    familyNotificationReminderMinutes = familyNotificationMinutes
                )
                FamilyHubRepository.setCurrentHub(updated)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Selects and opens a specific hub, setting it as active for the Hub Dashboard.
     */
    suspend fun selectAndOpenHub(hub: UserHubSummary) = withContext(Dispatchers.IO) {
        val currentUser = FirebaseAuthService.Instance.currentUser

        val hubData = FamilyHubData(
            hubId = hub.hubId,
            name = hub.name,
            hiveCode = hub.hiveCode,
            createdByUid = hub.createdByUid,
            missedDosageReminderMinutes = hub.missedDosageReminderMinutes,
            familyNotificationReminderMinutes = hub.familyNotificationReminderMinutes
        )

        FamilyHubRepository.setCurrentHub(hubData)
        HubDashboardRepository.attachHubListeners(hub.hubId)

        if (currentUser != null) {
            try {
                val firestore = FirebaseFirestore.getInstance()
                firestore.collection("users").document(currentUser.uid).set(
                    mapOf(
                        "currentHubId" to hub.hubId,
                        "currentHiveCode" to hub.hiveCode,
                        "hubName" to hub.name,
                        "updatedAt" to System.currentTimeMillis()
                    ),
                    SetOptions.merge()
                )
            } catch (_: Exception) {}
        }
    }

    /**
     * Loads approved adult members for a specific family hub.
     */
    suspend fun loadHubApprovedMembers(hubId: String): List<HubMember> = withContext(Dispatchers.IO) {
        try {
            val firestore = FirebaseFirestore.getInstance()
            val snapshot = firestore.collection("family_hubs")
                .document(hubId)
                .collection("members")
                .get()
                .await()

            if (snapshot != null && !snapshot.isEmpty) {
                snapshot.documents.mapNotNull { doc ->
                    try {
                        val avatarStr = doc.getString("avatarType") ?: "MALE"
                        val genderStr = doc.getString("gender")
                        HubMember(
                            id = doc.getString("id") ?: doc.getString("userId") ?: doc.id,
                            name = doc.getString("name") ?: "Family Member",
                            avatarType = try {
                                ProfileAvatarType.valueOf(avatarStr)
                            } catch (_: Exception) { ProfileAvatarType.MALE },
                            gender = try {
                                genderStr?.let { ProfileGender.valueOf(it) }
                            } catch (_: Exception) { null },
                            isCreator = doc.getBoolean("isCreator") ?: false,
                            isChild = false
                        )
                    } catch (_: Exception) { null }
                }
            } else {
                // Fallback to active members in memory if offline
                HubDashboardRepository.hubMembers.value.filter { !it.isChild }
            }
        } catch (_: Exception) {
            HubDashboardRepository.hubMembers.value.filter { !it.isChild }
        }
    }

    /**
     * Loads child profiles for a specific family hub.
     */
    suspend fun loadChildProfiles(hubId: String): List<ChildProfileData> = withContext(Dispatchers.IO) {
        try {
            val firestore = FirebaseFirestore.getInstance()
            val snapshot = firestore.collection("family_hubs")
                .document(hubId)
                .collection("children")
                .get()
                .await()

            if (snapshot != null && !snapshot.isEmpty) {
                snapshot.documents.mapNotNull { doc ->
                    try {
                        val genderStr = doc.getString("gender") ?: ProfileGender.MALE.name
                        val gender = try { ProfileGender.valueOf(genderStr) } catch (_: Exception) { ProfileGender.MALE }
                        val avatarStr = doc.getString("avatarType") ?: gender.toChildAvatarType().name
                        ChildProfileData(
                            childId = doc.getString("childId") ?: doc.id,
                            hubId = doc.getString("hubId") ?: hubId,
                            name = doc.getString("name") ?: "",
                            gender = gender,
                            avatarType = try {
                                ProfileAvatarType.valueOf(avatarStr)
                            } catch (_: Exception) { gender.toChildAvatarType() },
                            reminderResponsibleMemberId = doc.getString("reminderResponsibleMemberId") ?: "",
                            reminderResponsibleMemberName = doc.getString("reminderResponsibleMemberName") ?: "",
                            createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                        )
                    } catch (_: Exception) { null }
                }.sortedBy { it.createdAt }
            } else {
                emptyList()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Creates a new persistent child profile under family_hubs/{hubId}/children/{childId}.
     * Children are dependent profiles without independent authentication credentials.
     */
    suspend fun createChildProfile(
        hubId: String,
        name: String,
        gender: ProfileGender,
        reminderResponsibleMemberId: String,
        reminderResponsibleMemberName: String
    ): Result<ChildProfileData> = withContext(Dispatchers.IO) {
        if (name.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Child name cannot be empty."))
        }
        if (reminderResponsibleMemberId.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Please select who should be reminded for this child."))
        }

        try {
            val childId = "child_" + UUID.randomUUID().toString().take(12)
            val avatarType = gender.toChildAvatarType()
            val createdAt = System.currentTimeMillis()

            val currentUser = FirebaseAuthService.Instance.currentUser
            val currentUserId = currentUser?.uid ?: ""

            val childData = ChildProfileData(
                childId = childId,
                hubId = hubId,
                name = name.trim(),
                gender = gender,
                avatarType = avatarType,
                reminderResponsibleMemberId = reminderResponsibleMemberId,
                reminderResponsibleMemberName = reminderResponsibleMemberName,
                createdByUid = currentUserId,
                createdAt = createdAt
            )

            val firestore = FirebaseFirestore.getInstance()
            val childMap = hashMapOf(
                "childId" to childData.childId,
                "hubId" to childData.hubId,
                "name" to childData.name,
                "gender" to childData.gender.name,
                "avatarType" to childData.avatarType.name,
                "reminderResponsibleMemberId" to childData.reminderResponsibleMemberId,
                "reminderResponsibleMemberName" to childData.reminderResponsibleMemberName,
                "createdByUid" to currentUserId,
                "createdAt" to childData.createdAt,
                "updatedAt" to createdAt
            )

            firestore.collection("family_hubs")
                .document(hubId)
                .collection("children")
                .document(childId)
                .set(childMap, SetOptions.merge())
                .await()

            val profile = UserProfileRepository.userProfile.value
            com.example.ui.hub.activity.data.HubActivityRepository.recordEvent(
                hubId = hubId,
                actorUserId = currentUser?.uid ?: "current_user_local",
                actorName = profile?.name ?: "Family Member",
                actorAvatarType = profile?.avatarType?.name ?: "MALE",
                eventType = "CHILD_CREATED",
                targetType = "CHILD",
                targetId = childId,
                metadata = mapOf(
                    "childName" to childData.name,
                    "remindName" to childData.reminderResponsibleMemberName
                )
            )

            Result.success(childData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteHub(hubId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val currentUser = FirebaseAuthService.Instance.currentUser
            ?: return@withContext Result.failure(IllegalStateException("Not authenticated"))
        try {
            val firestore = FirebaseFirestore.getInstance()
            val hubDocRef = firestore.collection("family_hubs").document(hubId)
            val hubDoc = hubDocRef.get().await()
            if (!hubDoc.exists()) {
                return@withContext Result.failure(IllegalStateException("Hub does not exist"))
            }
            val createdByUid = hubDoc.getString("createdByUid")
            if (createdByUid != currentUser.uid) {
                return@withContext Result.failure(IllegalStateException("Only the hub creator can delete this hub."))
            }

            val profile = UserProfileRepository.userProfile.value
            com.example.ui.hub.activity.data.HubActivityRepository.recordEvent(
                hubId = hubId,
                actorUserId = currentUser.uid,
                actorName = profile?.name ?: "Family Member",
                actorAvatarType = profile?.avatarType?.name ?: "MALE",
                eventType = "HUB_DELETED",
                targetType = "HUB",
                targetId = hubId
            )

            val subcollections = listOf("members", "children", "join_requests", "medications", "hub_activity")
            for (sub in subcollections) {
                try {
                    val subSnap = hubDocRef.collection(sub).get().await()
                    for (doc in subSnap.documents) {
                        doc.reference.delete().await()
                    }
                } catch (_: Exception) {}
            }

            hubDocRef.delete().await()

            val usersSnap = firestore.collection("users").whereEqualTo("currentHubId", hubId).get().await()
            for (doc in usersSnap.documents) {
                try {
                    doc.reference.update(
                        mapOf(
                            "currentHubId" to null,
                            "currentHiveCode" to null,
                            "hubName" to null
                        )
                    ).await()
                } catch (_: Exception) {}
            }

            val activeHub = FamilyHubRepository.currentHub.value
            if (activeHub != null && activeHub.hubId == hubId) {
                FamilyHubRepository.clearHub()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun leaveHub(hubId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val currentUser = FirebaseAuthService.Instance.currentUser
            ?: return@withContext Result.failure(IllegalStateException("Not authenticated"))
        try {
            val firestore = FirebaseFirestore.getInstance()
            val hubDocRef = firestore.collection("family_hubs").document(hubId)
            val hubDoc = hubDocRef.get().await()
            if (!hubDoc.exists()) {
                return@withContext Result.failure(IllegalStateException("Hub does not exist"))
            }

            val createdByUid = hubDoc.getString("createdByUid")
            if (createdByUid == currentUser.uid) {
                return@withContext Result.failure(IllegalStateException("Creator cannot leave without transferring ownership."))
            }

            val profile = UserProfileRepository.userProfile.value
            com.example.ui.hub.activity.data.HubActivityRepository.recordEvent(
                hubId = hubId,
                actorUserId = currentUser.uid,
                actorName = profile?.name ?: "Family Member",
                actorAvatarType = profile?.avatarType?.name ?: "MALE",
                eventType = "MEMBER_LEFT",
                targetType = "USER",
                targetId = currentUser.uid
            )

            hubDocRef.update(
                "members", com.google.firebase.firestore.FieldValue.arrayRemove(currentUser.uid)
            ).await()

            try {
                hubDocRef.collection("members").document(currentUser.uid).delete().await()
            } catch (_: Exception) {}

            val userDocRef = firestore.collection("users").document(currentUser.uid)
            val userDoc = userDocRef.get().await()
            if (userDoc.getString("currentHubId") == hubId) {
                userDocRef.update(
                    mapOf(
                        "currentHubId" to null,
                        "currentHiveCode" to null,
                        "hubName" to null
                    )
                ).await()
            }

            val activeHub = FamilyHubRepository.currentHub.value
            if (activeHub != null && activeHub.hubId == hubId) {
                FamilyHubRepository.clearHub()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun transferCreatorAndLeave(hubId: String, newCreatorUserId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val currentUser = FirebaseAuthService.Instance.currentUser
            ?: return@withContext Result.failure(IllegalStateException("Not authenticated"))
        try {
            val firestore = FirebaseFirestore.getInstance()
            val hubDocRef = firestore.collection("family_hubs").document(hubId)
            val hubDoc = hubDocRef.get().await()
            if (!hubDoc.exists()) {
                return@withContext Result.failure(IllegalStateException("Hub does not exist"))
            }

            val createdByUid = hubDoc.getString("createdByUid")
            if (createdByUid != currentUser.uid) {
                return@withContext Result.failure(IllegalStateException("Only the creator can transfer ownership."))
            }

            val profile = UserProfileRepository.userProfile.value
            val actorName = profile?.name ?: "Family Member"
            val actorAvatar = profile?.avatarType?.name ?: "MALE"

            hubDocRef.update("createdByUid", newCreatorUserId).await()

            try {
                hubDocRef.collection("members").document(newCreatorUserId).set(
                    mapOf("isCreator" to true), SetOptions.merge()
                ).await()
            } catch (_: Exception) {}

            com.example.ui.hub.activity.data.HubActivityRepository.recordEvent(
                hubId = hubId,
                actorUserId = currentUser.uid,
                actorName = actorName,
                actorAvatarType = actorAvatar,
                eventType = "CREATOR_ROLE_TRANSFERRED",
                targetType = "USER",
                targetId = newCreatorUserId
            )

            hubDocRef.update(
                "members", com.google.firebase.firestore.FieldValue.arrayRemove(currentUser.uid)
            ).await()

            try {
                hubDocRef.collection("members").document(currentUser.uid).delete().await()
            } catch (_: Exception) {}

            com.example.ui.hub.activity.data.HubActivityRepository.recordEvent(
                hubId = hubId,
                actorUserId = currentUser.uid,
                actorName = actorName,
                actorAvatarType = actorAvatar,
                eventType = "MEMBER_LEFT",
                targetType = "USER",
                targetId = currentUser.uid
            )

            val userDocRef = firestore.collection("users").document(currentUser.uid)
            val userDoc = userDocRef.get().await()
            if (userDoc.getString("currentHubId") == hubId) {
                userDocRef.update(
                    mapOf(
                        "currentHubId" to null,
                        "currentHiveCode" to null,
                        "hubName" to null
                    )
                ).await()
            }

            val activeHub = FamilyHubRepository.currentHub.value
            if (activeHub != null && activeHub.hubId == hubId) {
                FamilyHubRepository.clearHub()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
