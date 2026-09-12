package com.example.ui.hub.dashboard

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.auth.FirebaseAuthService
import com.example.ui.hub.components.MyProfileDialog
import com.example.ui.hub.dashboard.components.AddEditMedicationModal
import com.example.ui.hub.dashboard.components.ChildDeleteModal
import com.example.ui.hub.dashboard.components.ChildDetailsModal
import com.example.ui.hub.dashboard.components.DeleteMedicationDialog
import com.example.ui.hub.dashboard.components.HubDashboardActions
import com.example.ui.hub.dashboard.components.HubDashboardBackgroundShapes
import com.example.ui.hub.dashboard.components.HubDashboardHeader
import com.example.ui.hub.dashboard.components.HubDashboardTimeBar
import com.example.ui.hub.dashboard.components.HubJoinRequestsBanner
import com.example.ui.hub.dashboard.components.HubMembersSection
import com.example.ui.hub.dashboard.components.MedicationBoard
import com.example.ui.hub.dashboard.components.MedicationDetailsDialog
import com.example.ui.hub.dashboard.components.MedicationHistoryButton
import com.example.ui.hub.dashboard.components.MemberDetailsModal
import com.example.ui.hub.dashboard.components.RemoveMemberModal
import com.example.ui.hub.dashboard.data.HubDashboardRepository
import com.example.ui.hub.dashboard.model.MedicationItem
import com.example.ui.hub.dashboard.voice.SmartVoiceMemoModal
import com.example.ui.hub.data.FamilyHubRepository
import com.example.ui.profilesetup.data.UserProfileRepository
import com.example.ui.profilesetup.model.ProfileAvatarType
import com.example.ui.profilesetup.model.toAvatarType
import kotlinx.coroutines.launch

// MedTrack Visual Language Palette tokens
private val ColorWarmIvory = Color(0xFFFAF4EC)

/**
 * MedTrack Hub Dashboard.
 *
 * Isolated, modular dashboard acting as the living family board:
 * - Hub Name & Copyable Hive Code (Warm Cream surface, IBM Plex Mono)
 * - Profile avatar icon (top-right, no text)
 * - Current local/synchronized time
 * - Main actions: + Add Medication & Smart Voice Memo placeholder
 * - Creator-only Pending Join Requests banner
 * - Shared Medication Board with cards, recipient avatars, mark-as-taken state, and creator ownership
 */
@Composable
fun FamilyHiveDashboardScreen(
    onNavigateToProfile: () -> Unit = {},
    onNavigateToMedicationHistory: () -> Unit = {},
    onSignOut: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    // Subscribed state from repositories
    val currentHub by FamilyHubRepository.currentHub.collectAsState()
    val userProfile by UserProfileRepository.userProfile.collectAsState()
    val currentTime by HubDashboardRepository.formattedCurrentTime.collectAsState()
    val currentDateLabel by HubDashboardRepository.formattedCurrentDateLabel.collectAsState()
    val approvedMembers by HubDashboardRepository.hubMembers.collectAsState()
    val pendingRequests by HubDashboardRepository.pendingJoinRequests.collectAsState()
    val medications by HubDashboardRepository.medications.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    androidx.compose.runtime.LaunchedEffect(currentHub?.hubId) {
        currentHub?.hubId?.let { hubId ->
            HubDashboardRepository.attachHubListeners(hubId)
        }
    }

    val currentUserId = HubDashboardRepository.getCurrentUserId()
    val isHubCreator = HubDashboardRepository.isCurrentUserHubCreator()

    val hubName = currentHub?.name?.ifBlank { "Family Hub" } ?: "Rahul's Fam"
    val hiveCode = currentHub?.hiveCode?.ifBlank { "AGKJNZ" } ?: "AGKJNZ"
    val avatarType = userProfile?.avatarType
        ?: userProfile?.gender?.toAvatarType()
        ?: ProfileAvatarType.MALE

    // Modal & Dialog states
    var isAddModalOpen by remember { mutableStateOf(false) }
    var isVoiceMemoModalOpen by remember { mutableStateOf(false) }
    var isProfileDialogOpen by remember { mutableStateOf(false) }
    var medicationToEdit by remember { mutableStateOf<MedicationItem?>(null) }
    var medicationToDelete by remember { mutableStateOf<MedicationItem?>(null) }
    var selectedMedicationForDetails by remember { mutableStateOf<MedicationItem?>(null) }
    var selectedMemberForDetails by remember { mutableStateOf<com.example.ui.hub.dashboard.model.HubMember?>(null) }
    var memberToRemove by remember { mutableStateOf<com.example.ui.hub.dashboard.model.HubMember?>(null) }
    var isRemovingMember by remember { mutableStateOf(false) }
    var removeMemberError by remember { mutableStateOf<String?>(null) }
    var selectedChildForDetails by remember { mutableStateOf<com.example.ui.hub.dashboard.model.HubMember?>(null) }
    var childToDelete by remember { mutableStateOf<com.example.ui.hub.dashboard.model.HubMember?>(null) }
    var isDeletingChild by remember { mutableStateOf(false) }
    var deleteChildError by remember { mutableStateOf<String?>(null) }

    fun handleLogout() {
        FirebaseAuthService.Instance.signOut()
        UserProfileRepository.clearProfile()
        FamilyHubRepository.clearHub()
        onSignOut()
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(ColorWarmIvory),
        containerColor = ColorWarmIvory,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Background organic geometric shapes
            HubDashboardBackgroundShapes()

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .testTag("family_hive_dashboard_scroll_container"),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Hub Header: Hub Name, Hub Code + Copy, Top-Right Profile Icon with Account Menu
                item(key = "hub_header") {
                    HubDashboardHeader(
                        hubName = hubName,
                        hubCode = hiveCode,
                        avatarType = avatarType,
                        onMyProfileClick = {
                            onNavigateToProfile()
                        },
                        onLogoutClick = {
                            handleLogout()
                        }
                    )
                }

                // 2. Current Time Bar & Quick Actions: Dynamic Date & Time on left, (+) (🎙) on right
                item(key = "time_bar") {
                    HubDashboardTimeBar(
                        dateLabel = currentDateLabel,
                        formattedTime = currentTime,
                        onAddMedicationClick = {
                            medicationToEdit = null
                            isAddModalOpen = true
                        },
                        onSmartVoiceMemoClick = {
                            isVoiceMemoModalOpen = true
                        }
                    )
                }

                // 3. Pending Family Join Requests (Creator only)
                if (isHubCreator && pendingRequests.isNotEmpty()) {
                    item(key = "join_requests_banner") {
                        HubJoinRequestsBanner(
                            requests = pendingRequests,
                            onAccept = { requestId ->
                                HubDashboardRepository.acceptJoinRequest(requestId)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Family member accepted into hub.")
                                }
                            },
                            onReject = { requestId ->
                                HubDashboardRepository.rejectJoinRequest(requestId)
                            }
                        )
                    }
                }

                // 5. Shared Family Medication Board
                item(key = "medication_board") {
                    MedicationBoard(
                        medications = medications,
                        currentUserId = currentUserId,
                        onToggleTaken = { medId ->
                            HubDashboardRepository.toggleMedicationTaken(medId)
                        },
                        onCardClick = { med ->
                            selectedMedicationForDetails = med
                        },
                        onEditMedication = { med ->
                            medicationToEdit = med
                            isAddModalOpen = true
                        },
                        onDeleteMedication = { med ->
                            medicationToDelete = med
                        },
                        onAddMedicationClick = {
                            medicationToEdit = null
                            isAddModalOpen = true
                        }
                    )
                }

                // 6. MEMBERS Section directly under Medication Board
                item(key = "members_section") {
                    HubMembersSection(
                        members = approvedMembers,
                        onMemberClick = { member ->
                            if (member.isChild) {
                                selectedChildForDetails = member
                            } else if (isHubCreator && member.id != currentUserId) {
                                selectedMemberForDetails = member
                            }
                        }
                    )
                }

                // 7. Medication History Button
                item(key = "medication_history_button") {
                    MedicationHistoryButton(
                        onClick = onNavigateToMedicationHistory
                    )
                }

                // Extra bottom spacing for navigation and scroll comfort
                item(key = "bottom_spacer") {
                    Spacer(modifier = Modifier.height(28.dp))
                }
            }
        }
    }

    // Add / Edit Medication Modal Sheet
    if (isAddModalOpen) {
        AddEditMedicationModal(
            existingMedication = medicationToEdit,
            approvedMembers = approvedMembers,
            currentUserId = currentUserId,
            onSaveMedication = { name, dosage, recipient, reminderTime, reminderHour, reminderMinute, cycle, intervalDays, notes, imageUri ->
                if (medicationToEdit != null) {
                    HubDashboardRepository.editMedication(
                        medicationId = medicationToEdit!!.id,
                        name = name,
                        dosage = dosage,
                        recipient = recipient,
                        reminderTime = reminderTime,
                        reminderHour = reminderHour,
                        reminderMinute = reminderMinute,
                        reminderCycle = cycle,
                        customIntervalDays = intervalDays,
                        notes = notes,
                        imageUri = imageUri
                    )
                } else {
                    HubDashboardRepository.addMedication(
                        name = name,
                        dosage = dosage,
                        recipient = recipient,
                        reminderTime = reminderTime,
                        reminderHour = reminderHour,
                        reminderMinute = reminderMinute,
                        reminderCycle = cycle,
                        customIntervalDays = intervalDays,
                        notes = notes,
                        imageUri = imageUri
                    )
                }
                isAddModalOpen = false
                medicationToEdit = null
            },
            onDismiss = {
                isAddModalOpen = false
                medicationToEdit = null
            }
        )
    }

    // Delete Medication Confirmation Dialog
    if (medicationToDelete != null) {
        DeleteMedicationDialog(
            medication = medicationToDelete!!,
            onConfirmDelete = {
                HubDashboardRepository.deleteMedication(medicationToDelete!!.id)
                medicationToDelete = null
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Medication removed from family board.")
                }
            },
            onDismiss = {
                medicationToDelete = null
            }
        )
    }

    // My Profile Dialog
    if (isProfileDialogOpen) {
        MyProfileDialog(
            userProfile = userProfile,
            onDismiss = { isProfileDialogOpen = false },
            onLogout = {
                isProfileDialogOpen = false
                handleLogout()
            }
        )
    }

    // Read-only Medication Details Dialog (No Edit or Delete)
    if (selectedMedicationForDetails != null) {
        MedicationDetailsDialog(
            medication = selectedMedicationForDetails!!,
            onDismiss = {
                selectedMedicationForDetails = null
            }
        )
    }

    // Smart Voice Memo Modal
    if (isVoiceMemoModalOpen) {
        SmartVoiceMemoModal(
            approvedMembers = approvedMembers,
            currentUserId = currentUserId,
            currentUserName = userProfile?.name ?: "You",
            onSaveMedication = { name, dosage, recipient, reminderTime, reminderHour, reminderMinute, reminderCycle, customIntervalDays, notes, imageUri ->
                HubDashboardRepository.addMedication(
                    name = name,
                    dosage = dosage,
                    recipient = recipient,
                    reminderTime = reminderTime,
                    reminderHour = reminderHour,
                    reminderMinute = reminderMinute,
                    reminderCycle = reminderCycle,
                    customIntervalDays = customIntervalDays,
                    customDaysOfWeek = emptyList(),
                    notes = notes,
                    imageUri = imageUri
                )
                isVoiceMemoModalOpen = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Medication created from Smart Voice Memo.")
                }
            },
            onDismiss = {
                isVoiceMemoModalOpen = false
            }
        )
    }

    // Member Details Modal (Creator Action)
    if (selectedMemberForDetails != null) {
        MemberDetailsModal(
            member = selectedMemberForDetails!!,
            onDismiss = { selectedMemberForDetails = null },
            onRemoveClick = {
                val mem = selectedMemberForDetails!!
                selectedMemberForDetails = null
                memberToRemove = mem
            }
        )
    }

    // Remove Member Confirmation Modal
    if (memberToRemove != null) {
        RemoveMemberModal(
            member = memberToRemove!!,
            isLoading = isRemovingMember,
            errorMessage = removeMemberError,
            onDismiss = {
                if (!isRemovingMember) {
                    memberToRemove = null
                    removeMemberError = null
                }
            },
            onConfirmRemove = {
                val hubId = currentHub?.hubId ?: return@RemoveMemberModal
                val targetMember = memberToRemove!!
                isRemovingMember = true
                removeMemberError = null
                coroutineScope.launch {
                    val result = HubDashboardRepository.removeHubMember(hubId, targetMember.id, targetMember.name)
                    isRemovingMember = false
                    if (result.isSuccess) {
                        memberToRemove = null
                        removeMemberError = null
                        snackbarHostState.showSnackbar("Removed ${targetMember.name} successfully.")
                    } else {
                        removeMemberError = result.exceptionOrNull()?.localizedMessage ?: "Failed to remove member. Please try again."
                    }
                }
            }
        )
    }

    // Child Details Modal
    if (selectedChildForDetails != null) {
        val child = selectedChildForDetails!!
        val isAuthorized = isHubCreator || child.createdByUid == currentUserId || child.reminderResponsibleMemberId == currentUserId
        ChildDetailsModal(
            child = child,
            isAuthorizedToDelete = isAuthorized,
            onDismiss = { selectedChildForDetails = null },
            onDeleteClick = {
                selectedChildForDetails = null
                childToDelete = child
            }
        )
    }

    // Child Delete Confirmation Modal
    if (childToDelete != null) {
        val child = childToDelete!!
        ChildDeleteModal(
            child = child,
            isLoading = isDeletingChild,
            errorMessage = deleteChildError,
            onDismiss = {
                if (!isDeletingChild) {
                    childToDelete = null
                    deleteChildError = null
                }
            },
            onConfirmDelete = {
                val hubId = currentHub?.hubId ?: return@ChildDeleteModal
                isDeletingChild = true
                deleteChildError = null
                coroutineScope.launch {
                    val result = HubDashboardRepository.deleteChildProfile(hubId, child.id, child.name)
                    isDeletingChild = false
                    if (result.isSuccess) {
                        childToDelete = null
                        deleteChildError = null
                        snackbarHostState.showSnackbar("Deleted ${child.name}'s child profile successfully.")
                    } else {
                        deleteChildError = result.exceptionOrNull()?.localizedMessage ?: "Failed to delete child profile. Please try again."
                    }
                }
            }
        )
    }
}
