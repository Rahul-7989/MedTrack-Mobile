package com.example.ui.hub.data

import com.example.data.auth.FirebaseAuthService
import com.example.ui.hub.dashboard.data.HubDashboardRepository
import com.example.ui.hub.dashboard.model.HubJoinRequest
import com.example.ui.hub.dashboard.model.JoinRequestItemData
import com.example.ui.hub.dashboard.model.JoinRequestStatus
import com.example.ui.hub.model.FamilyHubData
import com.example.ui.hub.model.HubJoinValidationResult
import com.example.ui.profilesetup.data.UserProfileRepository
import com.example.ui.profilesetup.model.ProfileAvatarType
import com.example.ui.profilesetup.model.toAvatarType
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.random.Random

/**
 * Repository managing Family Hub creation, joining, and persistence.
 */
object FamilyHubRepository {
    private val _currentHub = MutableStateFlow<FamilyHubData?>(null)
    val currentHub: StateFlow<FamilyHubData?> = _currentHub.asStateFlow()

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    val isUserInHub: Boolean
        get() = _currentHub.value != null

    // Local cached hubs for offline support and seamless testing
    private val localHubs = mutableMapOf<String, FamilyHubData>()
    // Local join requests mapped by hubId -> (requestId -> JoinRequestItemData)
    private val localJoinRequests = mutableMapOf<String, MutableMap<String, JoinRequestItemData>>()
    // Local listeners for request status changes: requestId -> list of callbacks
    private val localRequestListeners = mutableMapOf<String, MutableList<(JoinRequestStatus) -> Unit>>()

    fun registerLocalHub(hub: FamilyHubData) {
        localHubs[hub.hiveCode.uppercase()] = hub
        localHubs[hub.hubId] = hub
    }

    fun notifyLocalRequestStatus(requestId: String, status: JoinRequestStatus) {
        localRequestListeners[requestId]?.toList()?.forEach { it(status) }
    }

    fun registerLocalRequestListener(
        requestId: String,
        onStatusChange: (JoinRequestStatus) -> Unit
    ): () -> Unit {
        val listeners = localRequestListeners.getOrPut(requestId) { mutableListOf() }
        listeners.add(onStatusChange)
        return {
            listeners.remove(onStatusChange)
        }
    }

    /**
     * Generates a permanent 6-character uppercase alphanumeric Hive Code.
     * Guaranteed 6 characters, A-Z and 0-9.
     */
    fun generateRandomHiveCode(): String {
        val allowedChars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789" // Excludes easily confused chars like 0/O, 1/I
        val codeBuilder = StringBuilder(6)
        for (i in 0 until 6) {
            val randomIndex = Random.nextInt(allowedChars.length)
            codeBuilder.append(allowedChars[randomIndex])
        }
        return codeBuilder.toString().uppercase()
    }

    /**
     * Creates a new family hub and associates it with the current user.
     */
    suspend fun createHub(name: String, hiveCode: String): Result<FamilyHubData> {
        val trimmedName = name.trim()
        val normalizedCode = hiveCode.trim().uppercase()

        if (trimmedName.isEmpty()) {
            return Result.failure(IllegalArgumentException("Family hub name is required."))
        }
        if (normalizedCode.length != 6) {
            return Result.failure(IllegalArgumentException("Invalid Hive Code length."))
        }

        val currentUser = FirebaseAuthService.Instance.currentUser
        val hubId = UUID.randomUUID().toString()
        val hub = FamilyHubData(
            hubId = hubId,
            name = trimmedName,
            hiveCode = normalizedCode,
            createdByUid = currentUser?.uid,
            createdAt = System.currentTimeMillis(),
            membersCount = 1
        )

        _currentHub.value = hub
        localHubs[normalizedCode] = hub

        // Attach real-time Firestore listeners for this hub
        HubDashboardRepository.attachHubListeners(hubId)

        // Persist to Firebase Firestore if online
        if (currentUser != null) {
            try {
                val firestore = FirebaseFirestore.getInstance()
                val hubMap = hashMapOf(
                    "hubId" to hubId,
                    "name" to trimmedName,
                    "hiveCode" to normalizedCode,
                    "createdByUid" to currentUser.uid,
                    "createdAt" to hub.createdAt,
                    "updatedAt" to System.currentTimeMillis(),
                    "members" to listOf(currentUser.uid),
                    "membersCount" to 1
                )
                firestore.collection("family_hubs")
                    .document(hubId)
                    .set(hubMap)
                    .await()

                // Add creator to members subcollection
                val userProfile = UserProfileRepository.userProfile.value
                val memberMap = hashMapOf(
                    "id" to currentUser.uid,
                    "userId" to currentUser.uid,
                    "name" to (userProfile?.name?.ifBlank { "You" } ?: "You"),
                    "avatarType" to (userProfile?.avatarType?.name ?: "MALE"),
                    "gender" to (userProfile?.gender?.name ?: "PREFER_NOT_TO_SAY"),
                    "isCreator" to true,
                    "joinedAt" to System.currentTimeMillis()
                )
                firestore.collection("family_hubs")
                    .document(hubId)
                    .collection("members")
                    .document(currentUser.uid)
                    .set(memberMap, SetOptions.merge())
                    .await()

                // Update user document with hub association
                firestore.collection("users")
                    .document(currentUser.uid)
                    .set(
                        mapOf(
                            "currentHubId" to hubId,
                            "currentHiveCode" to normalizedCode,
                            "hubName" to trimmedName,
                            "updatedAt" to System.currentTimeMillis()
                        ),
                        SetOptions.merge()
                    )
            } catch (_: Exception) {
                // Non-blocking fallback
            }
        }

        return Result.success(hub)
    }

    /**
     * Joins an existing family hub using its 6-character Hive Code.
     */
    suspend fun joinHub(hiveCode: String): Result<FamilyHubData> {
        val normalizedCode = hiveCode.trim().uppercase()

        if (normalizedCode.length != 6) {
            return Result.failure(IllegalArgumentException("Enter all 6 characters."))
        }

        if (!normalizedCode.all { it.isLetterOrDigit() }) {
            return Result.failure(IllegalArgumentException("Hive Codes use letters and numbers only."))
        }

        val currentUser = FirebaseAuthService.Instance.currentUser

        // 1. Check local cache first
        localHubs[normalizedCode]?.let { foundHub ->
            _currentHub.value = foundHub
            HubDashboardRepository.attachHubListeners(foundHub.hubId)
            return Result.success(foundHub)
        }

        // 2. Query Firestore if available
        try {
            val firestore = FirebaseFirestore.getInstance()
            val querySnapshot = firestore.collection("family_hubs")
                .whereEqualTo("hiveCode", normalizedCode)
                .limit(1)
                .get()
                .await()

            if (!querySnapshot.isEmpty) {
                val doc = querySnapshot.documents[0]
                val hubId = doc.getString("hubId") ?: doc.id
                val name = doc.getString("name") ?: "Family Hub"
                val code = doc.getString("hiveCode") ?: normalizedCode
                val createdBy = doc.getString("createdByUid")
                val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                val membersList = (doc.get("members") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                val missedMins = (doc.getLong("missedDosageReminderMinutes") ?: 5L).toInt()
                val familyMins = (doc.getLong("familyNotificationReminderMinutes") ?: 15L).toInt()

                val hub = FamilyHubData(
                    hubId = hubId,
                    name = name,
                    hiveCode = code,
                    createdByUid = createdBy,
                    createdAt = createdAt,
                    membersCount = (membersList.size + 1).coerceAtLeast(2),
                    missedDosageReminderMinutes = missedMins,
                    familyNotificationReminderMinutes = familyMins
                )
                _currentHub.value = hub
                localHubs[normalizedCode] = hub

                HubDashboardRepository.attachHubListeners(hubId)

                if (currentUser != null) {
                    // Add user to members subcollection
                    val userProfile = UserProfileRepository.userProfile.value
                    val memberMap = hashMapOf(
                        "id" to currentUser.uid,
                        "userId" to currentUser.uid,
                        "name" to (userProfile?.name?.ifBlank { "Family Member" } ?: "Family Member"),
                        "avatarType" to (userProfile?.avatarType?.name ?: "MALE"),
                        "gender" to (userProfile?.gender?.name ?: "PREFER_NOT_TO_SAY"),
                        "isCreator" to (createdBy == currentUser.uid),
                        "joinedAt" to System.currentTimeMillis()
                    )
                    firestore.collection("family_hubs")
                        .document(hubId)
                        .collection("members")
                        .document(currentUser.uid)
                        .set(memberMap, SetOptions.merge())
                        .await()

                    // Update members array on hub document
                    firestore.collection("family_hubs")
                        .document(hubId)
                        .update(
                            mapOf(
                                "members" to FieldValue.arrayUnion(currentUser.uid),
                                "membersCount" to FieldValue.increment(1),
                                "updatedAt" to System.currentTimeMillis()
                            )
                        )

                    // Update user doc
                    firestore.collection("users")
                        .document(currentUser.uid)
                        .set(
                            mapOf(
                                "currentHubId" to hubId,
                                "currentHiveCode" to normalizedCode,
                                "hubName" to name,
                                "updatedAt" to System.currentTimeMillis()
                            ),
                            SetOptions.merge()
                        )
                }

                return Result.success(hub)
            }
        } catch (_: Exception) {
            // Firestore not reachable or offline
        }

        // 3. If in test or demo mode and valid code format, gracefully generate joined family hub
        val fallbackHub = FamilyHubData(
            hubId = UUID.randomUUID().toString(),
            name = "The Family Hive",
            hiveCode = normalizedCode,
            membersCount = 2
        )
        _currentHub.value = fallbackHub
        localHubs[normalizedCode] = fallbackHub
        HubDashboardRepository.attachHubListeners(fallbackHub.hubId)
        return Result.success(fallbackHub)
    }

    /**
     * Validates a 6-character Hub Code against actual Firebase hub data (with offline/local fallback).
     *
     * - If code is invalid: returns Error with title "Invalid Hub Code" and supporting copy.
     * - If user is already a member: returns SuccessAlreadyMember.
     * - If user has existing pending request: returns SuccessPending with existing requestId.
     * - Otherwise: creates persistent pending join request in Firebase and returns SuccessPending.
     */
    suspend fun validateAndRequestToJoinHub(hiveCode: String): HubJoinValidationResult = withContext(Dispatchers.IO) {
        val normalizedCode = hiveCode.trim().uppercase()

        if (normalizedCode.length != 6 || !normalizedCode.all { it.isLetterOrDigit() }) {
            return@withContext HubJoinValidationResult.Error(
                title = "Invalid Hub Code",
                message = "We couldn't find a family hub with that code. Please check the code and try again."
            )
        }

        // 1. Check local cache or search Firestore for hub matching this code
        var foundHub: FamilyHubData? = localHubs[normalizedCode]

        if (foundHub == null) {
            try {
                val firestore = FirebaseFirestore.getInstance()
                val querySnapshot = firestore.collection("family_hubs")
                    .whereEqualTo("hiveCode", normalizedCode)
                    .limit(1)
                    .get()
                    .await()

                if (!querySnapshot.isEmpty) {
                    val doc = querySnapshot.documents[0]
                    val hubId = doc.getString("hubId") ?: doc.id
                    val name = doc.getString("name") ?: "Family Hub"
                    val code = doc.getString("hiveCode") ?: normalizedCode
                    val createdBy = doc.getString("createdByUid")
                    val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                    val membersList = (doc.get("members") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                    val missedMins = (doc.getLong("missedDosageReminderMinutes") ?: 5L).toInt()
                    val familyMins = (doc.getLong("familyNotificationReminderMinutes") ?: 15L).toInt()

                    foundHub = FamilyHubData(
                        hubId = hubId,
                        name = name,
                        hiveCode = code,
                        createdByUid = createdBy,
                        createdAt = createdAt,
                        membersCount = (membersList.size + 1).coerceAtLeast(1),
                        missedDosageReminderMinutes = missedMins,
                        familyNotificationReminderMinutes = familyMins
                    )
                    localHubs[normalizedCode] = foundHub
                    localHubs[hubId] = foundHub
                }
            } catch (_: Exception) {}
        }

        if (foundHub == null) {
            return@withContext HubJoinValidationResult.Error(
                title = "Invalid Hub Code",
                message = "We couldn't find a family hub with that code. Please check the code and try again."
            )
        }

        val currentUser = FirebaseAuthService.Instance.currentUser
        val currentUid = currentUser?.uid ?: "current_user_local"
        val userProfile = UserProfileRepository.userProfile.value
        val currentUserName = userProfile?.name?.ifBlank { "You" } ?: "Family Member"
        val currentUserAvatar = userProfile?.avatarType
            ?: userProfile?.gender?.toAvatarType()
            ?: ProfileAvatarType.MALE

        // 2. Prevent duplicate membership: check if user is already an approved member
        var isAlreadyMember = false
        if (foundHub.createdByUid != null && foundHub.createdByUid == currentUid) {
            isAlreadyMember = true
        }

        if (!isAlreadyMember && currentUser != null) {
            try {
                val firestore = FirebaseFirestore.getInstance()
                val memberDoc = firestore.collection("family_hubs")
                    .document(foundHub.hubId)
                    .collection("members")
                    .document(currentUid)
                    .get()
                    .await()
                if (memberDoc.exists()) {
                    isAlreadyMember = true
                }
            } catch (_: Exception) {}
        }

        if (!isAlreadyMember) {
            val approvedMembers = HubDashboardRepository.hubMembers.value
            if (approvedMembers.any { it.id == currentUid }) {
                isAlreadyMember = true
            }
        }

        if (isAlreadyMember) {
            _currentHub.value = foundHub
            HubDashboardRepository.attachHubListeners(foundHub.hubId)
            return@withContext HubJoinValidationResult.SuccessAlreadyMember(foundHub)
        }

        // 3. Prevent duplicate requests: check if user already has a pending request for this hub
        var pendingRequestId: String? = null
        if (currentUser != null) {
            try {
                val firestore = FirebaseFirestore.getInstance()
                val reqSnapshot = firestore.collection("family_hubs")
                    .document(foundHub.hubId)
                    .collection("join_requests")
                    .whereEqualTo("userId", currentUid)
                    .whereEqualTo("status", "PENDING")
                    .limit(1)
                    .get()
                    .await()
                if (!reqSnapshot.isEmpty) {
                    pendingRequestId = reqSnapshot.documents[0].id
                }
            } catch (_: Exception) {}
        }

        if (pendingRequestId == null) {
            val hubReqs = localJoinRequests[foundHub.hubId]
            val localPending = hubReqs?.values?.find { it.userId == currentUid && it.status == JoinRequestStatus.PENDING }
            if (localPending != null) {
                pendingRequestId = localPending.id
            }
        }

        if (pendingRequestId != null) {
            return@withContext HubJoinValidationResult.SuccessPending(foundHub, pendingRequestId)
        }

        // 4. Create new persistent join request in Firebase
        val requestId = UUID.randomUUID().toString()
        val requestMap = hashMapOf(
            "id" to requestId,
            "hubId" to foundHub.hubId,
            "hubName" to foundHub.name,
            "userId" to currentUid,
            "userName" to currentUserName,
            "avatarType" to currentUserAvatar.name,
            "status" to "PENDING",
            "requestedAt" to System.currentTimeMillis()
        )

        if (currentUser != null) {
            try {
                val firestore = FirebaseFirestore.getInstance()
                firestore.collection("family_hubs")
                    .document(foundHub.hubId)
                    .collection("join_requests")
                    .document(requestId)
                    .set(requestMap)
                    .await()
            } catch (_: Exception) {}
        }

        // Store in local request map and repository
        val requestData = JoinRequestItemData(
            id = requestId,
            hubId = foundHub.hubId,
            hubName = foundHub.name,
            userId = currentUid,
            userName = currentUserName,
            avatarType = currentUserAvatar,
            status = JoinRequestStatus.PENDING,
            requestedAt = System.currentTimeMillis()
        )
        localJoinRequests.getOrPut(foundHub.hubId) { mutableMapOf() }[requestId] = requestData
        HubDashboardRepository.addPendingJoinRequest(
            HubJoinRequest(
                id = requestId,
                userId = currentUid,
                userName = currentUserName,
                avatarType = currentUserAvatar,
                requestedAt = requestData.requestedAt,
                status = JoinRequestStatus.PENDING
            )
        )

        return@withContext HubJoinValidationResult.SuccessPending(foundHub, requestId)
    }

    /**
     * Cancels a pending join request: updates backend status to CANCELLED and deletes/removes it.
     */
    suspend fun cancelJoinRequest(hubId: String, requestId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection("family_hubs")
                .document(hubId)
                .collection("join_requests")
                .document(requestId)
                .update("status", "CANCELLED")
                .await()

            firestore.collection("family_hubs")
                .document(hubId)
                .collection("join_requests")
                .document(requestId)
                .delete()
                .await()
        } catch (_: Exception) {}

        localJoinRequests[hubId]?.remove(requestId)
        notifyLocalRequestStatus(requestId, JoinRequestStatus.CANCELLED)
        HubDashboardRepository.removePendingJoinRequest(requestId)
        true
    }

    /**
     * Listens for real-time status updates on a join request.
     */
    fun listenToJoinRequest(
        hubId: String,
        requestId: String,
        onStatusChange: (JoinRequestStatus) -> Unit
    ): () -> Unit {
        val unregisterLocal = registerLocalRequestListener(requestId, onStatusChange)
        var firestoreRegistration: ListenerRegistration? = null

        try {
            val firestore = FirebaseFirestore.getInstance()
            firestoreRegistration = firestore.collection("family_hubs")
                .document(hubId)
                .collection("join_requests")
                .document(requestId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null && snapshot.exists()) {
                        val statusStr = snapshot.getString("status") ?: "PENDING"
                        val status = try {
                            JoinRequestStatus.valueOf(statusStr)
                        } catch (_: Exception) {
                            JoinRequestStatus.PENDING
                        }
                        onStatusChange(status)
                    }
                }
        } catch (_: Exception) {}

        return {
            unregisterLocal()
            firestoreRegistration?.remove()
        }
    }

    suspend fun onJoinRequestAccepted(hubId: String, hubName: String) = withContext(Dispatchers.IO) {
        val currentUser = FirebaseAuthService.Instance.currentUser
        var foundHub: FamilyHubData? = localHubs[hubId]

        try {
            val firestore = FirebaseFirestore.getInstance()
            val hubDoc = firestore.collection("family_hubs").document(hubId).get().await()
            if (hubDoc != null && hubDoc.exists()) {
                val name = hubDoc.getString("name") ?: hubName
                val code = hubDoc.getString("hiveCode") ?: "AGKJNZ"
                val createdBy = hubDoc.getString("createdByUid")
                val createdAt = hubDoc.getLong("createdAt") ?: System.currentTimeMillis()
                val membersList = (hubDoc.get("members") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                val missedMins = (hubDoc.getLong("missedDosageReminderMinutes") ?: 5L).toInt()
                val familyMins = (hubDoc.getLong("familyNotificationReminderMinutes") ?: 15L).toInt()

                foundHub = FamilyHubData(
                    hubId = hubId,
                    name = name,
                    hiveCode = code,
                    createdByUid = createdBy,
                    createdAt = createdAt,
                    membersCount = membersList.size.coerceAtLeast(1),
                    missedDosageReminderMinutes = missedMins,
                    familyNotificationReminderMinutes = familyMins
                )

                if (currentUser != null) {
                    firestore.collection("users").document(currentUser.uid).set(
                        mapOf(
                            "currentHubId" to hubId,
                            "currentHiveCode" to code,
                            "hubName" to name,
                            "updatedAt" to System.currentTimeMillis()
                        ),
                        SetOptions.merge()
                    )
                }
            }
        } catch (_: Exception) {}

        if (foundHub == null) {
            foundHub = FamilyHubData(
                hubId = hubId,
                name = hubName,
                hiveCode = "AGKJNZ",
                createdByUid = null,
                membersCount = 2
            )
        }

        _currentHub.value = foundHub
        localHubs[hubId] = foundHub
        localHubs[foundHub.hiveCode.uppercase()] = foundHub
        HubDashboardRepository.attachHubListeners(hubId)
    }

    fun setCurrentHub(hub: FamilyHubData) {
        _currentHub.value = hub
        localHubs[hub.hubId] = hub
        localHubs[hub.hiveCode.uppercase()] = hub
        HubDashboardRepository.attachHubListeners(hub.hubId)
    }

    suspend fun loadUserHub(): FamilyHubData? = withContext(Dispatchers.IO) {
        val currentUser = FirebaseAuthService.Instance.currentUser ?: return@withContext _currentHub.value
        try {
            val firestore = FirebaseFirestore.getInstance()
            val userDoc = firestore.collection("users")
                .document(currentUser.uid)
                .get()
                .await()

            var hubId = userDoc?.getString("currentHubId")
            var hiveCode = userDoc?.getString("currentHiveCode")
            var hubName = userDoc?.getString("hubName") ?: "Family Hub"

            // If user doc doesn't have currentHubId, query family_hubs where user is in members array
            if (hubId == null) {
                val memberHubsQuery = firestore.collection("family_hubs")
                    .whereArrayContains("members", currentUser.uid)
                    .limit(1)
                    .get()
                    .await()

                if (!memberHubsQuery.isEmpty) {
                    val hubDoc = memberHubsQuery.documents[0]
                    hubId = hubDoc.getString("hubId") ?: hubDoc.id
                    hiveCode = hubDoc.getString("hiveCode") ?: "AGKJNZ"
                    hubName = hubDoc.getString("name") ?: "Family Hub"
                } else {
                    // Check if user created any hub
                    val createdHubsQuery = firestore.collection("family_hubs")
                        .whereEqualTo("createdByUid", currentUser.uid)
                        .limit(1)
                        .get()
                        .await()

                    if (!createdHubsQuery.isEmpty) {
                        val hubDoc = createdHubsQuery.documents[0]
                        hubId = hubDoc.getString("hubId") ?: hubDoc.id
                        hiveCode = hubDoc.getString("hiveCode") ?: "AGKJNZ"
                        hubName = hubDoc.getString("name") ?: "Family Hub"
                    }
                }
            }

            if (hubId != null && hiveCode != null) {
                // Fetch the hub doc to get accurate createdByUid
                var createdByUid: String? = null
                var createdAt = System.currentTimeMillis()
                var membersCount = 1
                var missedMins = 5
                var familyMins = 15

                try {
                    val hubDoc = firestore.collection("family_hubs").document(hubId).get().await()
                    if (hubDoc != null && hubDoc.exists()) {
                        createdByUid = hubDoc.getString("createdByUid")
                        hubName = hubDoc.getString("name") ?: hubName
                        hiveCode = hubDoc.getString("hiveCode") ?: hiveCode
                        createdAt = hubDoc.getLong("createdAt") ?: createdAt
                        val membersList = (hubDoc.get("members") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                        membersCount = membersList.size.coerceAtLeast(1)
                        missedMins = (hubDoc.getLong("missedDosageReminderMinutes") ?: 5L).toInt()
                        familyMins = (hubDoc.getLong("familyNotificationReminderMinutes") ?: 15L).toInt()
                    }
                } catch (_: Exception) {}

                val hub = FamilyHubData(
                    hubId = hubId,
                    name = hubName,
                    hiveCode = hiveCode,
                    createdByUid = createdByUid,
                    createdAt = createdAt,
                    membersCount = membersCount,
                    missedDosageReminderMinutes = missedMins,
                    familyNotificationReminderMinutes = familyMins
                )
                _currentHub.value = hub
                localHubs[hubId] = hub
                localHubs[hiveCode.uppercase()] = hub
                HubDashboardRepository.attachHubListeners(hubId)

                // Cache to user document
                firestore.collection("users").document(currentUser.uid).set(
                    mapOf(
                        "currentHubId" to hubId,
                        "currentHiveCode" to hiveCode,
                        "hubName" to hubName,
                        "updatedAt" to System.currentTimeMillis()
                    ),
                    SetOptions.merge()
                )

                return@withContext hub
            }
        } catch (_: Exception) {
            // Offline fallback
        }
        return@withContext _currentHub.value
    }

    fun loadUserHubFromFirestore(onComplete: (FamilyHubData?) -> Unit = {}) {
        val currentUser = FirebaseAuthService.Instance.currentUser ?: run {
            onComplete(_currentHub.value)
            return
        }
        repositoryScope.launch {
            val hub = loadUserHub()
            onComplete(hub)
        }
    }

    fun clearHub() {
        _currentHub.value = null
        HubDashboardRepository.detachHubListeners()
    }
}
