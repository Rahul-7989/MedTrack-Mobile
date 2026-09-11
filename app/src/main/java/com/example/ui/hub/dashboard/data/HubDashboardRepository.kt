package com.example.ui.hub.dashboard.data

import com.example.data.auth.FirebaseAuthService
import com.example.ui.hub.dashboard.model.HubJoinRequest
import com.example.ui.hub.dashboard.model.HubMember
import com.example.ui.hub.dashboard.model.MedicationItem
import com.example.ui.hub.dashboard.model.ReminderCycle
import com.example.ui.hub.data.FamilyHubRepository
import com.example.ui.profilesetup.data.UserProfileRepository
import com.example.ui.profilesetup.model.ProfileAvatarType
import com.example.ui.profilesetup.model.ProfileGender
import com.example.ui.profilesetup.model.toAvatarType
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

/**
 * Isolated repository managing state for the MedTrack Hub Dashboard with Firebase Firestore persistence.
 *
 * Handles:
 * - Real-time medication management (Add, Edit, Delete, Mark as Taken)
 * - Approved Hub Members list with live synchronization
 * - Creator-only Pending Join Requests (Accept, Reject)
 * - Live Internet/Local synchronized clock display
 */
object HubDashboardRepository {

    private val MONTH_CODES = arrayOf(
        "JAN", "FEB", "MAR", "APR", "MAY", "JUN",
        "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"
    )

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    // Current synchronized formatted time state (e.g. "9:42 AM")
    private val _formattedCurrentTime = MutableStateFlow(getCurrentFormattedDeviceTime())
    val formattedCurrentTime: StateFlow<String> = _formattedCurrentTime.asStateFlow()

    // Current formatted date header (format: <DATE> <MONTH>, <YEAR> e.g. "11 SEP, 2026")
    private val _formattedCurrentDateLabel = MutableStateFlow(getCurrentFormattedDeviceDate())
    val formattedCurrentDateLabel: StateFlow<String> = _formattedCurrentDateLabel.asStateFlow()

    // Approved hub members
    private val _hubMembers = MutableStateFlow<List<HubMember>>(emptyList())
    val hubMembers: StateFlow<List<HubMember>> = _hubMembers.asStateFlow()

    // Pending join requests (only displayed to hub creator)
    private val _pendingJoinRequests = MutableStateFlow<List<HubJoinRequest>>(emptyList())
    val pendingJoinRequests: StateFlow<List<HubJoinRequest>> = _pendingJoinRequests.asStateFlow()

    // Scheduled medications for the hub
    private val _medications = MutableStateFlow<List<MedicationItem>>(emptyList())
    val medications: StateFlow<List<MedicationItem>> = _medications.asStateFlow()

    // Firestore listener registrations
    private var medicationsListener: ListenerRegistration? = null
    private var membersListener: ListenerRegistration? = null
    private var childrenListener: ListenerRegistration? = null
    private var joinRequestsListener: ListenerRegistration? = null
    private var activeHubId: String? = null

    private var rawAdultMembers = listOf<HubMember>()
    private var rawChildMembers = listOf<HubMember>()

    private fun updateCombinedMembers() {
        val combined = (rawAdultMembers + rawChildMembers).distinctBy { it.id }
        if (combined.isNotEmpty()) {
            _hubMembers.value = combined
        }
    }

    // Offset in milliseconds between authoritative server time and device clock
    private var serverTimeOffsetMs: Long = 0L

    init {
        initializeHubData()
        startLiveClockTicker()
        fetchInternetTimeOffset()
    }

    /**
     * Attaches live real-time Firestore listeners to the specified family hub.
     */
    fun attachHubListeners(hubId: String) {
        if (activeHubId == hubId && medicationsListener != null) return
        detachHubListeners()
        activeHubId = hubId

        val firestore = try {
            FirebaseFirestore.getInstance()
        } catch (_: Exception) {
            null
        } ?: return

        // 1. Listen to medications subcollection: family_hubs/{hubId}/medications
        try {
            medicationsListener = firestore.collection("family_hubs")
                .document(hubId)
                .collection("medications")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null) {
                        val list = snapshot.documents.mapNotNull { doc ->
                            try {
                                val recipientAvatarStr = doc.getString("recipientAvatarType") ?: "MALE"
                                val cycleStr = doc.getString("reminderCycle") ?: ReminderCycle.EVERY_24_HOURS.name
                                MedicationItem(
                                    id = doc.getString("id") ?: doc.id,
                                    hubId = doc.getString("hubId") ?: hubId,
                                    name = doc.getString("name") ?: "",
                                    dosage = doc.getString("dosage") ?: "",
                                    recipientId = doc.getString("recipientId") ?: "",
                                    recipientName = doc.getString("recipientName") ?: "",
                                    recipientAvatarType = try {
                                        ProfileAvatarType.valueOf(recipientAvatarStr)
                                    } catch (_: Exception) { ProfileAvatarType.MALE },
                                    reminderTime = doc.getString("reminderTime") ?: "9:00 AM",
                                    reminderHour = (doc.getLong("reminderHour") ?: 9L).toInt(),
                                    reminderMinute = (doc.getLong("reminderMinute") ?: 0L).toInt(),
                                    reminderCycle = try {
                                        ReminderCycle.valueOf(cycleStr)
                                    } catch (_: Exception) { ReminderCycle.EVERY_24_HOURS },
                                    customIntervalDays = (doc.getLong("customIntervalDays") ?: 1L).toInt(),
                                    customDaysOfWeek = (doc.get("customDaysOfWeek") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                                    notes = doc.getString("notes"),
                                    imageUri = doc.getString("imageUri"),
                                    createdByUid = doc.getString("createdByUid") ?: "",
                                    isTakenToday = doc.getBoolean("isTakenToday") ?: false,
                                    takenAtTime = doc.getString("takenAtTime"),
                                    reminderResponsibleUid = doc.getString("reminderResponsibleUid"),
                                    isChildRecipient = doc.getBoolean("isChildRecipient") ?: false,
                                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                                )
                            } catch (_: Exception) {
                                null
                            }
                        }.sortedByDescending { it.createdAt }

                        _medications.value = list

                        // Synchronize system alarms for today's active occurrences
                        try {
                            val context = com.example.MedTrackApplication.appContext
                            val currentHub = FamilyHubRepository.currentHub.value
                            val missed = currentHub?.missedDosageReminderMinutes ?: 5
                            val family = currentHub?.familyNotificationReminderMinutes ?: 15
                            com.example.reminder.scheduler.MedicationReminderScheduler.syncMedications(
                                context = context,
                                hubId = hubId,
                                medications = list,
                                missedDosageMinutes = missed,
                                familyNotificationMinutes = family
                            )
                        } catch (_: Exception) {}
                    }
                }
        } catch (_: Exception) {}

        // 2. Listen to members subcollection: family_hubs/{hubId}/members
        try {
            membersListener = firestore.collection("family_hubs")
                .document(hubId)
                .collection("members")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null) {
                        val membersList = snapshot.documents.mapNotNull { doc ->
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
                            } catch (_: Exception) {
                                null
                            }
                        }
                        rawAdultMembers = membersList
                        updateCombinedMembers()
                    }
                }
        } catch (_: Exception) {}

        // 3. Listen to children subcollection: family_hubs/{hubId}/children
        try {
            childrenListener = firestore.collection("family_hubs")
                .document(hubId)
                .collection("children")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null) {
                        val childList = snapshot.documents.mapNotNull { doc ->
                            try {
                                val avatarStr = doc.getString("avatarType") ?: "CHILD_MALE"
                                val genderStr = doc.getString("gender")
                                HubMember(
                                    id = doc.getString("childId") ?: doc.getString("id") ?: doc.id,
                                    name = doc.getString("name") ?: "Child",
                                    avatarType = try {
                                        ProfileAvatarType.valueOf(avatarStr)
                                    } catch (_: Exception) { ProfileAvatarType.CHILD_MALE },
                                    gender = try {
                                        genderStr?.let { ProfileGender.valueOf(it) }
                                    } catch (_: Exception) { null },
                                    isCreator = false,
                                    isChild = true,
                                    reminderResponsibleMemberId = doc.getString("reminderResponsibleMemberId"),
                                    reminderResponsibleMemberName = doc.getString("reminderResponsibleMemberName")
                                )
                            } catch (_: Exception) {
                                null
                            }
                        }
                        rawChildMembers = childList
                        updateCombinedMembers()
                    }
                }
        } catch (_: Exception) {}

        // 4. Listen to pending join requests: family_hubs/{hubId}/join_requests
        try {
            joinRequestsListener = firestore.collection("family_hubs")
                .document(hubId)
                .collection("join_requests")
                .whereEqualTo("status", "PENDING")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null) {
                        val requests = snapshot.documents.mapNotNull { doc ->
                            try {
                                val avatarStr = doc.getString("avatarType") ?: "MALE"
                                HubJoinRequest(
                                    id = doc.getString("id") ?: doc.id,
                                    userId = doc.getString("userId") ?: "",
                                    userName = doc.getString("userName") ?: "New Member",
                                    avatarType = try {
                                        ProfileAvatarType.valueOf(avatarStr)
                                    } catch (_: Exception) { ProfileAvatarType.MALE },
                                    requestedAt = doc.getLong("requestedAt") ?: System.currentTimeMillis()
                                )
                            } catch (_: Exception) {
                                null
                            }
                        }
                        _pendingJoinRequests.value = requests
                    }
                }
        } catch (_: Exception) {}
    }

    /**
     * Detaches all active Firestore snapshot listeners.
     */
    fun detachHubListeners() {
        try { medicationsListener?.remove() } catch (_: Exception) {}
        try { membersListener?.remove() } catch (_: Exception) {}
        try { childrenListener?.remove() } catch (_: Exception) {}
        try { joinRequestsListener?.remove() } catch (_: Exception) {}
        medicationsListener = null
        membersListener = null
        childrenListener = null
        joinRequestsListener = null
        activeHubId = null
    }

    /**
     * Initializes default approved members, sample medications, and pending requests.
     */
    fun initializeHubData() {
        val currentProfile = UserProfileRepository.userProfile.value
        val currentUid = FirebaseAuthService.Instance.currentUser?.uid ?: "current_user_local"
        val currentUserName = currentProfile?.name?.ifBlank { "You" } ?: "Rahul"
        val currentUserAvatar = currentProfile?.avatarType
            ?: currentProfile?.gender?.toAvatarType()
            ?: ProfileAvatarType.MALE

        val defaultCreator = HubMember(
            id = currentUid,
            name = currentUserName,
            avatarType = currentUserAvatar,
            gender = currentProfile?.gender,
            isCreator = true
        )

        val partnerMember = HubMember(
            id = "member_partner_1",
            name = "Maya",
            avatarType = ProfileAvatarType.FEMALE,
            isCreator = false
        )

        if (_hubMembers.value.isEmpty()) {
            _hubMembers.value = listOf(defaultCreator, partnerMember)
        }

        // Seed initial items for graceful display
        val currentHub = FamilyHubRepository.currentHub.value
        val hubId = currentHub?.hubId ?: "hub_demo_id"

        if (_medications.value.isEmpty()) {
            _medications.value = listOf(
                MedicationItem(
                    id = "med_vitamin_d",
                    hubId = hubId,
                    name = "Vitamin D3",
                    dosage = "1 tablet (2000 IU)",
                    recipientId = currentUid,
                    recipientName = currentUserName,
                    recipientAvatarType = currentUserAvatar,
                    reminderTime = "9:00 AM",
                    reminderHour = 9,
                    reminderMinute = 0,
                    reminderCycle = ReminderCycle.EVERY_24_HOURS,
                    notes = "Take with morning meal and water.",
                    createdByUid = currentUid,
                    isTakenToday = false,
                    takenAtTime = null
                ),
                MedicationItem(
                    id = "med_omega_3",
                    hubId = hubId,
                    name = "Omega-3 Fish Oil",
                    dosage = "2 softgels",
                    recipientId = "member_partner_1",
                    recipientName = "Maya",
                    recipientAvatarType = ProfileAvatarType.FEMALE,
                    reminderTime = "1:00 PM",
                    reminderHour = 13,
                    reminderMinute = 0,
                    reminderCycle = ReminderCycle.EVERY_24_HOURS,
                    notes = null,
                    createdByUid = currentUid,
                    isTakenToday = true,
                    takenAtTime = "1:05 PM"
                )
            )
        }

        // Attach listeners if hub already exists
        if (currentHub != null) {
            attachHubListeners(currentHub.hubId)
        }
    }

    /**
     * Checks if the active user is the creator of the current hub.
     * The join requests screen and banner are strictly visible ONLY to the creator of the hub.
     */
    fun isCurrentUserHubCreator(): Boolean {
        val currentUid = FirebaseAuthService.Instance.currentUser?.uid ?: "current_user_local"
        val currentHub = FamilyHubRepository.currentHub.value ?: return false

        // 1. Direct createdByUid check if populated on hub
        if (!currentHub.createdByUid.isNullOrBlank()) {
            return currentHub.createdByUid == currentUid || (currentUid == "current_user_local" && currentHub.createdByUid == "current_user_local")
        }

        // 2. Check approved members list for isCreator flag
        val currentMember = _hubMembers.value.find { it.id == currentUid }
        if (currentMember != null) {
            return currentMember.isCreator
        }

        // 3. Fallback for offline/local standalone mode
        return currentUid == "current_user_local"
    }

    /**
     * Gets the active user's ID for ownership checks.
     */
    fun getCurrentUserId(): String {
        return FirebaseAuthService.Instance.currentUser?.uid ?: "current_user_local"
    }

    /**
     * Marks a medication as taken or un-taken with timestamp and syncs to Firestore.
     */
    fun toggleMedicationTaken(medicationId: String) {
        val nowFormatted = getCurrentFormattedTime()
        var updatedItem: MedicationItem? = null

        _medications.value = _medications.value.map { item ->
            if (item.id == medicationId) {
                val toggled = if (item.isTakenToday) {
                    item.copy(isTakenToday = false, takenAtTime = null)
                } else {
                    item.copy(isTakenToday = true, takenAtTime = nowFormatted)
                }
                updatedItem = toggled
                toggled
            } else {
                item
            }
        }

        // Persist change to Firestore and manage system alarms
        val item = updatedItem ?: return
        val currentHub = FamilyHubRepository.currentHub.value ?: return

        try {
            val context = com.example.MedTrackApplication.appContext
            val todayKey = com.example.reminder.scheduler.MedicationReminderScheduler.getTodayDateKey()
            if (item.isTakenToday) {
                com.example.reminder.scheduler.MedicationReminderScheduler.cancelOccurrenceAlarms(context, medicationId, todayKey)
                val occ = com.example.reminder.data.ReminderStorage.getOccurrence(context, medicationId, todayKey)
                if (occ != null) {
                    com.example.reminder.notification.MedicationNotificationHelper.cancelOccurrenceNotifications(context, occ)
                }
                com.example.reminder.data.ReminderStorage.markOccurrenceAsTaken(context, medicationId, todayKey, nowFormatted)
            } else {
                com.example.reminder.scheduler.MedicationReminderScheduler.syncMedications(
                    context = context,
                    hubId = currentHub.hubId,
                    medications = _medications.value,
                    missedDosageMinutes = currentHub.missedDosageReminderMinutes,
                    familyNotificationMinutes = currentHub.familyNotificationReminderMinutes
                )
            }
        } catch (_: Exception) {}

        repositoryScope.launch {
            try {
                val firestore = FirebaseFirestore.getInstance()
                firestore.collection("family_hubs")
                    .document(currentHub.hubId)
                    .collection("medications")
                    .document(medicationId)
                    .update(
                        mapOf(
                            "isTakenToday" to item.isTakenToday,
                            "takenAtTime" to item.takenAtTime,
                            "updatedAt" to System.currentTimeMillis()
                        )
                    )
            } catch (_: Exception) {}
        }
    }

    /**
     * Creates a new medication item on the shared hub and saves to Firestore.
     */
    fun addMedication(
        name: String,
        dosage: String,
        recipient: HubMember,
        reminderTime: String,
        reminderHour: Int,
        reminderMinute: Int,
        reminderCycle: ReminderCycle,
        customIntervalDays: Int = 1,
        customDaysOfWeek: List<String> = emptyList(),
        notes: String? = null,
        imageUri: String? = null
    ): MedicationItem {
        val currentUid = getCurrentUserId()
        val currentHub = FamilyHubRepository.currentHub.value
        val hubId = currentHub?.hubId ?: "hub_demo_id"
        val medId = UUID.randomUUID().toString()

        val newItem = MedicationItem(
            id = medId,
            hubId = hubId,
            name = name.trim(),
            dosage = dosage.trim(),
            recipientId = recipient.id,
            recipientName = recipient.name,
            recipientAvatarType = recipient.avatarType,
            reminderTime = reminderTime,
            reminderHour = reminderHour,
            reminderMinute = reminderMinute,
            reminderCycle = reminderCycle,
            customIntervalDays = customIntervalDays,
            customDaysOfWeek = customDaysOfWeek,
            notes = notes?.trim()?.ifBlank { null },
            imageUri = imageUri,
            createdByUid = currentUid,
            isTakenToday = false,
            takenAtTime = null,
            reminderResponsibleUid = recipient.reminderResponsibleMemberId,
            isChildRecipient = recipient.isChild,
            createdAt = System.currentTimeMillis()
        )

        _medications.value = listOf(newItem) + _medications.value.filter { it.id != medId }

        // Synchronize alarms for the new medication
        try {
            val context = com.example.MedTrackApplication.appContext
            com.example.reminder.scheduler.MedicationReminderScheduler.syncMedications(
                context = context,
                hubId = hubId,
                medications = _medications.value,
                missedDosageMinutes = currentHub?.missedDosageReminderMinutes ?: 5,
                familyNotificationMinutes = currentHub?.familyNotificationReminderMinutes ?: 15
            )
        } catch (_: Exception) {}

        // Persist to Firestore: family_hubs/{hubId}/medications/{medId}
        repositoryScope.launch {
            try {
                val firestore = FirebaseFirestore.getInstance()
                val medMap = hashMapOf(
                    "id" to newItem.id,
                    "hubId" to hubId,
                    "name" to newItem.name,
                    "dosage" to newItem.dosage,
                    "recipientId" to newItem.recipientId,
                    "recipientName" to newItem.recipientName,
                    "recipientAvatarType" to newItem.recipientAvatarType.name,
                    "reminderTime" to newItem.reminderTime,
                    "reminderHour" to newItem.reminderHour,
                    "reminderMinute" to newItem.reminderMinute,
                    "reminderCycle" to newItem.reminderCycle.name,
                    "customIntervalDays" to newItem.customIntervalDays,
                    "customDaysOfWeek" to newItem.customDaysOfWeek,
                    "notes" to newItem.notes,
                    "imageUri" to newItem.imageUri,
                    "createdByUid" to newItem.createdByUid,
                    "isTakenToday" to false,
                    "takenAtTime" to null,
                    "reminderResponsibleUid" to newItem.reminderResponsibleUid,
                    "isChildRecipient" to newItem.isChildRecipient,
                    "createdAt" to newItem.createdAt,
                    "updatedAt" to System.currentTimeMillis()
                )

                firestore.collection("family_hubs")
                    .document(hubId)
                    .collection("medications")
                    .document(newItem.id)
                    .set(medMap, SetOptions.merge())
            } catch (_: Exception) {}
        }

        return newItem
    }

    /**
     * Edits an existing medication and updates Firestore.
     */
    fun editMedication(
        medicationId: String,
        name: String,
        dosage: String,
        recipient: HubMember,
        reminderTime: String,
        reminderHour: Int,
        reminderMinute: Int,
        reminderCycle: ReminderCycle,
        customIntervalDays: Int = 1,
        customDaysOfWeek: List<String> = emptyList(),
        notes: String? = null,
        imageUri: String? = null
    ): Boolean {
        val currentUid = getCurrentUserId()
        val target = _medications.value.find { it.id == medicationId } ?: return false

        // Security / ownership enforcement
        if (target.createdByUid != currentUid && target.createdByUid != "current_user_local") {
            return false
        }

        val updated = target.copy(
            name = name.trim(),
            dosage = dosage.trim(),
            recipientId = recipient.id,
            recipientName = recipient.name,
            recipientAvatarType = recipient.avatarType,
            reminderTime = reminderTime,
            reminderHour = reminderHour,
            reminderMinute = reminderMinute,
            reminderCycle = reminderCycle,
            customIntervalDays = customIntervalDays,
            customDaysOfWeek = customDaysOfWeek,
            notes = notes?.trim()?.ifBlank { null },
            imageUri = imageUri ?: target.imageUri,
            reminderResponsibleUid = recipient.reminderResponsibleMemberId,
            isChildRecipient = recipient.isChild
        )

        _medications.value = _medications.value.map { item ->
            if (item.id == medicationId) updated else item
        }

        val currentHub = FamilyHubRepository.currentHub.value
        val hubId = currentHub?.hubId ?: target.hubId

        // Synchronize updated alarms
        try {
            val context = com.example.MedTrackApplication.appContext
            com.example.reminder.scheduler.MedicationReminderScheduler.syncMedications(
                context = context,
                hubId = hubId,
                medications = _medications.value,
                missedDosageMinutes = currentHub?.missedDosageReminderMinutes ?: 5,
                familyNotificationMinutes = currentHub?.familyNotificationReminderMinutes ?: 15
            )
        } catch (_: Exception) {}

        repositoryScope.launch {
            try {
                val firestore = FirebaseFirestore.getInstance()
                val updateMap = hashMapOf<String, Any?>(
                    "name" to updated.name,
                    "dosage" to updated.dosage,
                    "recipientId" to updated.recipientId,
                    "recipientName" to updated.recipientName,
                    "recipientAvatarType" to updated.recipientAvatarType.name,
                    "reminderTime" to updated.reminderTime,
                    "reminderHour" to updated.reminderHour,
                    "reminderMinute" to updated.reminderMinute,
                    "reminderCycle" to updated.reminderCycle.name,
                    "customIntervalDays" to updated.customIntervalDays,
                    "customDaysOfWeek" to updated.customDaysOfWeek,
                    "notes" to updated.notes,
                    "imageUri" to updated.imageUri,
                    "reminderResponsibleUid" to updated.reminderResponsibleUid,
                    "isChildRecipient" to updated.isChildRecipient,
                    "updatedAt" to System.currentTimeMillis()
                )

                firestore.collection("family_hubs")
                    .document(hubId)
                    .collection("medications")
                    .document(medicationId)
                    .set(updateMap, SetOptions.merge())
            } catch (_: Exception) {}
        }

        return true
    }

    /**
     * Deletes a medication and removes document from Firestore.
     */
    fun deleteMedication(medicationId: String): Boolean {
        val currentUid = getCurrentUserId()
        val target = _medications.value.find { it.id == medicationId } ?: return false

        if (target.createdByUid != currentUid && target.createdByUid != "current_user_local") {
            return false
        }

        _medications.value = _medications.value.filter { it.id != medicationId }

        val currentHub = FamilyHubRepository.currentHub.value
        val hubId = currentHub?.hubId ?: target.hubId

        // Cancel alarms and notifications for deleted medication
        try {
            val context = com.example.MedTrackApplication.appContext
            val todayKey = com.example.reminder.scheduler.MedicationReminderScheduler.getTodayDateKey()
            com.example.reminder.scheduler.MedicationReminderScheduler.cancelOccurrenceAlarms(context, medicationId, todayKey)
            val occ = com.example.reminder.data.ReminderStorage.getOccurrence(context, medicationId, todayKey)
            if (occ != null) {
                com.example.reminder.notification.MedicationNotificationHelper.cancelOccurrenceNotifications(context, occ)
            }
        } catch (_: Exception) {}

        repositoryScope.launch {
            try {
                val firestore = FirebaseFirestore.getInstance()
                firestore.collection("family_hubs")
                    .document(hubId)
                    .collection("medications")
                    .document(medicationId)
                    .delete()
            } catch (_: Exception) {}
        }

        return true
    }

    fun addPendingJoinRequest(request: HubJoinRequest) {
        if (_pendingJoinRequests.value.none { it.id == request.id }) {
            _pendingJoinRequests.value = _pendingJoinRequests.value + request
        }
    }

    fun removePendingJoinRequest(requestId: String) {
        _pendingJoinRequests.value = _pendingJoinRequests.value.filter { it.id != requestId }
    }

    /**
     * Approves a pending join request: adds the member to the hub and updates request status to ACCEPTED.
     * Enforces creator permission: only creator of this hub may accept.
     */
    fun acceptJoinRequest(requestId: String) {
        val currentHub = FamilyHubRepository.currentHub.value ?: return
        val currentUid = getCurrentUserId()

        // Enforce creator permission: only creator of that hub can accept
        if (currentHub.createdByUid != null && currentHub.createdByUid != currentUid && currentUid != "current_user_local") {
            return
        }

        val request = _pendingJoinRequests.value.find { it.id == requestId } ?: return
        val newMember = HubMember(
            id = request.userId,
            name = request.userName,
            avatarType = request.avatarType,
            isCreator = false
        )
        _hubMembers.value = _hubMembers.value + newMember
        _pendingJoinRequests.value = _pendingJoinRequests.value.filter { it.id != requestId }

        // Notify local listener if Waiting Room is listening in same process
        FamilyHubRepository.notifyLocalRequestStatus(requestId, com.example.ui.hub.dashboard.model.JoinRequestStatus.ACCEPTED)

        repositoryScope.launch {
            try {
                val firestore = FirebaseFirestore.getInstance()
                // Update join request status
                firestore.collection("family_hubs")
                    .document(currentHub.hubId)
                    .collection("join_requests")
                    .document(requestId)
                    .update(
                        mapOf(
                            "status" to "ACCEPTED",
                            "approvedAt" to System.currentTimeMillis()
                        )
                    )
                    .await()

                // Add to hub members
                val memberMap = hashMapOf(
                    "id" to request.userId,
                    "userId" to request.userId,
                    "name" to request.userName,
                    "avatarType" to request.avatarType.name,
                    "isCreator" to false,
                    "joinedAt" to System.currentTimeMillis()
                )
                firestore.collection("family_hubs")
                    .document(currentHub.hubId)
                    .collection("members")
                    .document(request.userId)
                    .set(memberMap, SetOptions.merge())
                    .await()

                // Update hub members list
                firestore.collection("family_hubs")
                    .document(currentHub.hubId)
                    .update(
                        mapOf(
                            "members" to FieldValue.arrayUnion(request.userId),
                            "membersCount" to FieldValue.increment(1),
                            "updatedAt" to System.currentTimeMillis()
                        )
                    )
                    .await()

                // Update approved user doc so their app knows their active hub
                firestore.collection("users")
                    .document(request.userId)
                    .set(
                        mapOf(
                            "currentHubId" to currentHub.hubId,
                            "currentHiveCode" to currentHub.hiveCode,
                            "hubName" to currentHub.name,
                            "updatedAt" to System.currentTimeMillis()
                        ),
                        SetOptions.merge()
                    )
                    .await()
            } catch (_: Exception) {}
        }
    }

    /**
     * Rejects a pending join request: updates status to REJECTED in Firestore without adding member.
     * Enforces creator permission: only creator of this hub may reject.
     */
    fun rejectJoinRequest(requestId: String) {
        val currentHub = FamilyHubRepository.currentHub.value ?: return
        val currentUid = getCurrentUserId()

        // Enforce creator permission: only creator of that hub can reject
        if (currentHub.createdByUid != null && currentHub.createdByUid != currentUid && currentUid != "current_user_local") {
            return
        }

        _pendingJoinRequests.value = _pendingJoinRequests.value.filter { it.id != requestId }

        // Notify local listener so Waiting Room transitions into Rejected state
        FamilyHubRepository.notifyLocalRequestStatus(requestId, com.example.ui.hub.dashboard.model.JoinRequestStatus.REJECTED)

        repositoryScope.launch {
            try {
                val firestore = FirebaseFirestore.getInstance()
                firestore.collection("family_hubs")
                    .document(currentHub.hubId)
                    .collection("join_requests")
                    .document(requestId)
                    .update(
                        mapOf(
                            "status" to "REJECTED",
                            "rejectedAt" to System.currentTimeMillis()
                        )
                    )
                    .await()
            } catch (_: Exception) {}
        }
    }

    /**
     * Coroutine clock ticker that updates time and date live every 15 seconds.
     */
     private fun startLiveClockTicker() {
         repositoryScope.launch {
             while (isActive) {
                 _formattedCurrentTime.value = getCurrentFormattedTime()
                 _formattedCurrentDateLabel.value = getCurrentFormattedDate()
                 delay(15_000) // update every 15s for responsiveness
             }
         }
     }

    /**
     * Attempts to query internet time offset via lightweight HTTP HEAD header,
     * seamlessly falling back to device clock.
     */
    private fun fetchInternetTimeOffset() {
        repositoryScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val url = URL("https://www.google.com")
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "HEAD"
                    connection.connectTimeout = 3000
                    connection.readTimeout = 3000
                    val serverDate = connection.date
                    if (serverDate > 0) {
                        val localNow = System.currentTimeMillis()
                        serverTimeOffsetMs = serverDate - localNow
                        _formattedCurrentTime.value = getCurrentFormattedTime()
                        _formattedCurrentDateLabel.value = getCurrentFormattedDate()
                    }
                    connection.disconnect()
                }
            } catch (_: Exception) {
                // Gracefully fallback to device clock
                serverTimeOffsetMs = 0L
                _formattedCurrentDateLabel.value = getCurrentFormattedDate()
            }
        }
    }

    private fun getCurrentFormattedTime(): String {
        val currentEffectiveTime = System.currentTimeMillis() + serverTimeOffsetMs
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        calendar.timeInMillis = currentEffectiveTime
        val formatter = SimpleDateFormat("h:mm a", Locale.getDefault())
        return formatter.format(calendar.time)
    }

    private fun getCurrentFormattedDeviceTime(): String {
        val formatter = SimpleDateFormat("h:mm a", Locale.getDefault())
        return formatter.format(Date())
    }

    /**
     * Returns the dynamic current calendar date formatted EXACTLY as:
     * <DATE> <MONTH>, <YEAR>
     * Example: "11 SEP, 2026"
     * Uses server-authoritative offset when available, otherwise device local calendar.
     */
    fun getCurrentFormattedDate(): String {
        val currentEffectiveTime = System.currentTimeMillis() + serverTimeOffsetMs
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        calendar.timeInMillis = currentEffectiveTime
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val monthCode = MONTH_CODES[calendar.get(Calendar.MONTH)]
        val year = calendar.get(Calendar.YEAR)
        return "$day $monthCode, $year"
    }

    private fun getCurrentFormattedDeviceDate(): String {
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val monthCode = MONTH_CODES[calendar.get(Calendar.MONTH)]
        val year = calendar.get(Calendar.YEAR)
        return "$day $monthCode, $year"
    }
}
