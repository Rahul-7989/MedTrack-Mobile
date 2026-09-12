package com.example.ui.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.auth.FirebaseAuthService
import com.example.ui.hub.components.HubBackButton
import com.example.ui.hub.dashboard.model.HubMember
import com.example.ui.hub.data.FamilyHubRepository
import com.example.ui.splash.MedTrackSplashScreen
import com.example.ui.profile.components.CreateChildProfileModal
import com.example.ui.profile.components.DeleteHubModal
import com.example.ui.profile.components.LeaveHubModal
import com.example.ui.profile.components.ProfileChildrenSection
import com.example.ui.profile.components.ProfileEditSection
import com.example.ui.profile.components.ProfileFamilyHubsSection
import com.example.ui.profile.components.ProfileHeaderSection
import com.example.ui.profile.components.ProfileLogoutSection
import com.example.ui.profile.components.ProfileReminderAlertsSection
import com.example.ui.profile.components.TransferCreatorModal
import com.example.ui.profile.data.ProfileRepository
import com.example.ui.profile.model.ChildProfileData
import com.example.ui.profile.model.UserHubSummary
import com.example.ui.profilesetup.data.UserProfileRepository
import com.example.ui.profilesetup.model.ProfileAvatarType
import com.example.ui.profilesetup.model.ProfileGender
import com.example.ui.profilesetup.model.toAvatarType
import com.example.ui.theme.DarkWarmText
import com.example.ui.theme.DustyTeal
import com.example.ui.theme.SoraFontFamily
import com.example.ui.theme.WarmIvory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Background color
private val ColorWarmIvory = WarmIvory
private val ColorDarkWarmText = DarkWarmText
private val ColorDustyTeal = DustyTeal

/**
 * MedTrack Profile Screen.
 *
 * Dedicated, independently modular screen presenting the authenticated user's:
 * 1. Profile Header (Avatar, Name, Email, Edit Profile action)
 * 2. Inline Profile Editor (Name, Gender, Age, About Me)
 * 3. Family Hubs (Create, Join, list of all user's hubs with Creator/Member roles)
 * 4. Reminder Alerts (Hub dropdown, per-hub missed dosage & family notification settings, Creator permissions)
 * 5. Log Out button
 */
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCreateHub: () -> Unit,
    onNavigateToJoinHub: () -> Unit,
    onNavigateToHubDashboard: (hubId: String) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    // User & Profile State
    val userProfile by UserProfileRepository.userProfile.collectAsState()
    val currentUser = FirebaseAuthService.Instance.currentUser
    val userEmail = currentUser?.email?.ifBlank { null } ?: "Not available"

    // Edit Profile form states
    var isEditingProfile by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(userProfile?.name ?: "") }
    var editGender by remember { mutableStateOf(userProfile?.gender ?: ProfileGender.PREFER_NOT_TO_SAY) }
    var editAge by remember { mutableIntStateOf(userProfile?.age ?: 30) }
    var editAboutMe by remember { mutableStateOf(userProfile?.aboutMe ?: "") }
    var isAboutMeExpanded by remember { mutableStateOf(!userProfile?.aboutMe.isNullOrBlank()) }
    var isSavingProfile by remember { mutableStateOf(false) }
    var profileErrorMessage by remember { mutableStateOf<String?>(null) }
    var profileSuccessMessage by remember { mutableStateOf<String?>(null) }

    // User's Hubs list state
    var userHubs by remember { mutableStateOf<List<UserHubSummary>>(emptyList()) }
    var selectedHubForAlerts by remember { mutableStateOf<UserHubSummary?>(null) }
    var isPageLoaded by remember { mutableStateOf(false) }
    var profileLoadError by remember { mutableStateOf<String?>(null) }

    // Children Profiles State
    var childrenProfiles by remember { mutableStateOf<List<ChildProfileData>>(emptyList()) }
    var approvedHubMembersForChildModal by remember { mutableStateOf<List<HubMember>>(emptyList()) }
    var isCreateChildModalOpen by remember { mutableStateOf(false) }
    var isCreatingChild by remember { mutableStateOf(false) }
    var createChildErrorMessage by remember { mutableStateOf<String?>(null) }

    // Hub Management State (Delete, Leave, Transfer Creator)
    var hubToDelete by remember { mutableStateOf<UserHubSummary?>(null) }
    var hubToLeave by remember { mutableStateOf<UserHubSummary?>(null) }
    var hubToTransferAndLeave by remember { mutableStateOf<UserHubSummary?>(null) }
    var isHubActionRunning by remember { mutableStateOf(false) }
    var hubActionErrorMessage by remember { mutableStateOf<String?>(null) }
    var eligibleMembersForTransfer by remember { mutableStateOf<List<HubMember>>(emptyList()) }

    fun handleDeleteHub(hub: UserHubSummary) {
        hubActionErrorMessage = null
        hubToDelete = hub
    }

    fun handleLeaveHub(hub: UserHubSummary) {
        hubActionErrorMessage = null
        val isCreator = hub.role == com.example.ui.profile.model.HubUserRole.CREATOR
        val hasOtherMembers = hub.membersCount > 1
        if (isCreator && hasOtherMembers) {
            coroutineScope.launch {
                isHubActionRunning = true
                val members = ProfileRepository.loadHubApprovedMembers(hub.hubId).filter { it.id != currentUser?.uid }
                eligibleMembersForTransfer = members
                isHubActionRunning = false
                hubToTransferAndLeave = hub
            }
        } else {
            hubToLeave = hub
        }
    }

    fun confirmDeleteHub() {
        val hub = hubToDelete ?: return
        isHubActionRunning = true
        hubActionErrorMessage = null
        coroutineScope.launch {
            val result = ProfileRepository.deleteHub(hub.hubId)
            isHubActionRunning = false
            if (result.isSuccess) {
                hubToDelete = null
                val hubs = ProfileRepository.loadUserHubs()
                userHubs = hubs
                selectedHubForAlerts = hubs.firstOrNull()
                snackbarHostState.showSnackbar("Hub deleted successfully")
            } else {
                hubActionErrorMessage = result.exceptionOrNull()?.localizedMessage ?: "Failed to delete hub"
            }
        }
    }

    fun confirmLeaveHub() {
        val hub = hubToLeave ?: return
        isHubActionRunning = true
        hubActionErrorMessage = null
        coroutineScope.launch {
            val result = ProfileRepository.leaveHub(hub.hubId)
            isHubActionRunning = false
            if (result.isSuccess) {
                hubToLeave = null
                val hubs = ProfileRepository.loadUserHubs()
                userHubs = hubs
                selectedHubForAlerts = hubs.firstOrNull()
                snackbarHostState.showSnackbar("You left the hub")
            } else {
                hubActionErrorMessage = result.exceptionOrNull()?.localizedMessage ?: "Failed to leave hub"
            }
        }
    }

    fun confirmTransferAndLeave(newCreatorId: String) {
        val hub = hubToTransferAndLeave ?: return
        isHubActionRunning = true
        hubActionErrorMessage = null
        coroutineScope.launch {
            val result = ProfileRepository.transferCreatorAndLeave(hub.hubId, newCreatorId)
            isHubActionRunning = false
            if (result.isSuccess) {
                hubToTransferAndLeave = null
                val hubs = ProfileRepository.loadUserHubs()
                userHubs = hubs
                selectedHubForAlerts = hubs.firstOrNull()
                snackbarHostState.showSnackbar("Transferred ownership and left hub")
            } else {
                hubActionErrorMessage = result.exceptionOrNull()?.localizedMessage ?: "Failed to transfer ownership"
            }
        }
    }

    val pageAlpha by animateFloatAsState(
        targetValue = if (isPageLoaded) 1f else 0f,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "ProfileScreenAlpha"
    )

    // Load profile and hubs on entry
    fun loadProfileData() {
        coroutineScope.launch {
            profileLoadError = null
            try {
                UserProfileRepository.loadProfile()
                val hubs = ProfileRepository.loadUserHubs()
                userHubs = hubs

                // Default selected hub for alerts
                val activeHub = FamilyHubRepository.currentHub.value
                selectedHubForAlerts = hubs.find { it.hubId == activeHub?.hubId } ?: hubs.firstOrNull()

                // Sync edit form fields with loaded profile
                val profile = UserProfileRepository.userProfile.value
                if (profile != null) {
                    editName = profile.name
                    editGender = profile.gender
                    editAge = profile.age
                    editAboutMe = profile.aboutMe.orEmpty()
                    isAboutMeExpanded = !profile.aboutMe.isNullOrBlank()
                }

                isPageLoaded = true
            } catch (e: Exception) {
                profileLoadError = "Couldn't load your profile. Please try again."
                isPageLoaded = true
            }
        }
    }

    LaunchedEffect(Unit) {
        loadProfileData()
    }

    if (!isPageLoaded) {
        MedTrackSplashScreen(skipNavigation = true)
        return
    }

    if (profileLoadError != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(WarmIvory)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = profileLoadError!!,
                    fontFamily = SoraFontFamily,
                    fontSize = 14.sp,
                    color = com.example.ui.theme.MutedTerracotta,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                androidx.compose.material3.Button(
                    onClick = {
                        isPageLoaded = false
                        loadProfileData()
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = com.example.ui.theme.MutedTerracotta
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Retry", fontFamily = SoraFontFamily, color = WarmIvory)
                }
            }
        }
        return
    }

    // Load children and approved members whenever active hub selection changes
    LaunchedEffect(selectedHubForAlerts?.hubId) {
        val hubId = selectedHubForAlerts?.hubId
        if (!hubId.isNullOrBlank()) {
            childrenProfiles = ProfileRepository.loadChildProfiles(hubId)
            approvedHubMembersForChildModal = ProfileRepository.loadHubApprovedMembers(hubId)
        } else {
            childrenProfiles = emptyList()
            approvedHubMembersForChildModal = emptyList()
        }
    }

    // Update edit form state when userProfile changes
    LaunchedEffect(userProfile) {
        if (!isEditingProfile && userProfile != null) {
            editName = userProfile!!.name
            editGender = userProfile!!.gender
            editAge = userProfile!!.age
            editAboutMe = userProfile!!.aboutMe.orEmpty()
            isAboutMeExpanded = !userProfile!!.aboutMe.isNullOrBlank()
        }
    }

    fun handleSaveProfile() {
        if (editName.trim().isBlank()) {
            profileErrorMessage = "Name is required"
            return
        }

        isSavingProfile = true
        profileErrorMessage = null
        profileSuccessMessage = null

        coroutineScope.launch {
            val result = ProfileRepository.saveUserProfile(
                name = editName,
                gender = editGender,
                age = editAge,
                aboutMe = editAboutMe
            )

            isSavingProfile = false
            if (result.isSuccess) {
                profileSuccessMessage = "Profile updated"
                delay(1200)
                isEditingProfile = false
                profileSuccessMessage = null
            } else {
                profileErrorMessage = result.exceptionOrNull()?.message ?: "Failed to save profile. Please try again."
            }
        }
    }

    fun handleUpdateReminderSettings(hubId: String, missedDosage: Int, familyNotify: Int) {
        coroutineScope.launch {
            val result = ProfileRepository.updateHubReminderSettings(hubId, missedDosage, familyNotify)
            if (result.isSuccess) {
                // Refresh hubs list
                userHubs = userHubs.map { hub ->
                    if (hub.hubId == hubId) {
                        hub.copy(
                            missedDosageReminderMinutes = missedDosage,
                            familyNotificationReminderMinutes = familyNotify
                        )
                    } else hub
                }
                if (selectedHubForAlerts?.hubId == hubId) {
                    selectedHubForAlerts = selectedHubForAlerts?.copy(
                        missedDosageReminderMinutes = missedDosage,
                        familyNotificationReminderMinutes = familyNotify
                    )
                }
            }
        }
    }

    fun handleCreateChildProfile(
        name: String,
        gender: ProfileGender,
        reminderMemberId: String,
        reminderMemberName: String
    ) {
        val hub = selectedHubForAlerts ?: return
        isCreatingChild = true
        createChildErrorMessage = null
        coroutineScope.launch {
            val result = ProfileRepository.createChildProfile(
                hubId = hub.hubId,
                name = name,
                gender = gender,
                reminderResponsibleMemberId = reminderMemberId,
                reminderResponsibleMemberName = reminderMemberName
            )
            isCreatingChild = false
            if (result.isSuccess) {
                isCreateChildModalOpen = false
                childrenProfiles = ProfileRepository.loadChildProfiles(hub.hubId)
                snackbarHostState.showSnackbar("Child profile created successfully.")
            } else {
                createChildErrorMessage = result.exceptionOrNull()?.message ?: "Failed to create child profile."
            }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(ColorWarmIvory),
        containerColor = ColorWarmIvory,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .systemBarsPadding()
                .imePadding()
                .testTag("profile_screen"),
            contentAlignment = Alignment.TopCenter
        ) {
            val isCompact = maxWidth < 600.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(pageAlpha)
            ) {
                // Top Bar with "← Back" button and "PROFILE" title
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .testTag("profile_top_bar"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    HubBackButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("profile_back_button")
                    )

                    Text(
                        text = "PROFILE",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 1.6.sp,
                        color = ColorDustyTeal,
                        modifier = Modifier.testTag("profile_page_title")
                    )

                    // Spacer for symmetry
                    Spacer(modifier = Modifier.size(40.dp))
                }

                // Scrollable Content Column
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .verticalScroll(scrollState)
                        .padding(
                            horizontal = if (isCompact) 20.dp else 32.dp,
                            vertical = 8.dp
                        )
                        .widthIn(max = 640.dp)
                        .align(Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(22.dp)
                ) {
                    // 1. Profile Header Section
                    val currentAvatarType = if (isEditingProfile) {
                        editGender.toAvatarType()
                    } else {
                        userProfile?.avatarType ?: (userProfile?.gender?.toAvatarType() ?: ProfileAvatarType.ANONYMOUS)
                    }

                    ProfileHeaderSection(
                        name = if (isEditingProfile) editName else (userProfile?.name ?: ""),
                        email = userEmail,
                        avatarType = currentAvatarType,
                        isEditing = isEditingProfile,
                        onEditProfileClick = {
                            profileErrorMessage = null
                            profileSuccessMessage = null
                            isEditingProfile = true
                        }
                    )

                    // Profile Details Card (Age, Gender, About Me when not editing)
                    if (!isEditingProfile && userProfile != null) {
                        val ageDisplay = when {
                            userProfile!!.age >= 70 -> "70+ years"
                            else -> "${userProfile!!.age} years"
                        }
                        val genderDisplay = when (userProfile!!.gender) {
                            ProfileGender.MALE -> "Male"
                            ProfileGender.FEMALE -> "Female"
                            else -> "Prefer not to say"
                        }
                        val aboutMeText = userProfile!!.aboutMe

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(
                                    elevation = 1.5.dp,
                                    shape = RoundedCornerShape(13.dp),
                                    ambientColor = Color(0x0C9C876E),
                                    spotColor = Color(0x10786550)
                                )
                                .clip(RoundedCornerShape(13.dp))
                                .background(WarmIvory)
                                .border(BorderStroke(1.dp, Color(0xFFE4D5C2)), RoundedCornerShape(13.dp))
                                .padding(16.dp)
                                .testTag("profile_details_card"),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Age",
                                        fontFamily = SoraFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = DustyTeal
                                    )
                                    Text(
                                        text = ageDisplay,
                                        fontFamily = SoraFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.5.sp,
                                        color = DarkWarmText
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Gender",
                                        fontFamily = SoraFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = DustyTeal
                                    )
                                    Text(
                                        text = genderDisplay,
                                        fontFamily = SoraFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.5.sp,
                                        color = DarkWarmText
                                    )
                                }
                            }

                            if (!aboutMeText.isNullOrBlank()) {
                                androidx.compose.material3.HorizontalDivider(
                                    color = Color(0xFFE4D5C2).copy(alpha = 0.5f),
                                    thickness = 0.8.dp
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "About Me",
                                        fontFamily = SoraFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = DustyTeal
                                    )
                                    Text(
                                        text = aboutMeText,
                                        fontFamily = SoraFontFamily,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 13.sp,
                                        color = DarkWarmText
                                    )
                                }
                            }
                        }
                    }

                    // 2. Edit Profile Form (when active)
                    AnimatedVisibility(
                        visible = isEditingProfile,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        ProfileEditSection(
                            name = editName,
                            onNameChange = { editName = it },
                            gender = editGender,
                            onGenderChange = { editGender = it },
                            age = editAge,
                            onAgeChange = { editAge = it },
                            aboutMe = editAboutMe,
                            onAboutMeChange = { editAboutMe = it },
                            isAboutMeExpanded = isAboutMeExpanded,
                            onToggleAboutMeExpanded = { isAboutMeExpanded = it },
                            isSaving = isSavingProfile,
                            errorMessage = profileErrorMessage,
                            successMessage = profileSuccessMessage,
                            onSaveChanges = ::handleSaveProfile,
                            onCancel = {
                                profileErrorMessage = null
                                profileSuccessMessage = null
                                // Revert to stored profile
                                if (userProfile != null) {
                                    editName = userProfile!!.name
                                    editGender = userProfile!!.gender
                                    editAge = userProfile!!.age
                                    editAboutMe = userProfile!!.aboutMe.orEmpty()
                                }
                                isEditingProfile = false
                            }
                        )
                    }

                    // 3. Family Hubs Section (Create, Join, Your Hubs)
                    ProfileFamilyHubsSection(
                        hubs = userHubs,
                        onCreateHubClick = onNavigateToCreateHub,
                        onJoinHubClick = onNavigateToJoinHub,
                        onHubCardClick = { selectedHub ->
                            coroutineScope.launch {
                                ProfileRepository.selectAndOpenHub(selectedHub)
                                onNavigateToHubDashboard(selectedHub.hubId)
                            }
                        },
                        onDeleteHubClick = ::handleDeleteHub,
                        onLeaveHubClick = ::handleLeaveHub
                    )

                    // 4. Reminder Alerts Section
                    ProfileReminderAlertsSection(
                        hubs = userHubs,
                        selectedHub = selectedHubForAlerts,
                        onSelectHub = { hub -> selectedHubForAlerts = hub },
                        onUpdateReminderSettings = ::handleUpdateReminderSettings
                    )

                    // 5. Children Profiles Section (placed directly below Reminder Alerts)
                    ProfileChildrenSection(
                        userHubs = userHubs,
                        selectedHub = selectedHubForAlerts,
                        children = childrenProfiles,
                        onSelectHub = { hub -> selectedHubForAlerts = hub },
                        onCreateChildClick = {
                            createChildErrorMessage = null
                            isCreateChildModalOpen = true
                        }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // 6. Log out Button at bottom
                    ProfileLogoutSection(
                        onLogoutClick = onLogout
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }

    // Modal Sheet: Create Child Profile
    if (isCreateChildModalOpen && selectedHubForAlerts != null) {
        CreateChildProfileModal(
            hubId = selectedHubForAlerts!!.hubId,
            hubName = selectedHubForAlerts!!.name,
            approvedMembers = approvedHubMembersForChildModal,
            onDismiss = { isCreateChildModalOpen = false },
            onCreateChild = ::handleCreateChildProfile,
            isSubmitting = isCreatingChild,
            errorMessage = createChildErrorMessage
        )
    }

    // Modal: Delete Hub
    if (hubToDelete != null) {
        DeleteHubModal(
            hubName = hubToDelete!!.name,
            onDismiss = { hubToDelete = null; hubActionErrorMessage = null },
            onConfirmDelete = ::confirmDeleteHub,
            isDeleting = isHubActionRunning,
            errorMessage = hubActionErrorMessage
        )
    }

    // Modal: Leave Hub
    if (hubToLeave != null) {
        LeaveHubModal(
            hubName = hubToLeave!!.name,
            onDismiss = { hubToLeave = null; hubActionErrorMessage = null },
            onConfirmLeave = ::confirmLeaveHub,
            isLeaving = isHubActionRunning,
            errorMessage = hubActionErrorMessage
        )
    }

    // Modal: Transfer Creator & Leave Hub
    if (hubToTransferAndLeave != null) {
        TransferCreatorModal(
            hubName = hubToTransferAndLeave!!.name,
            eligibleMembers = eligibleMembersForTransfer,
            onDismiss = { hubToTransferAndLeave = null; hubActionErrorMessage = null },
            onConfirmTransferAndLeave = ::confirmTransferAndLeave,
            isTransferring = isHubActionRunning,
            errorMessage = hubActionErrorMessage
        )
    }
}
