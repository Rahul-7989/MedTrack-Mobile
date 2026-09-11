package com.example.ui.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.auth.FirebaseAuthService
import com.example.ui.hub.components.HubBackButton
import com.example.ui.hub.dashboard.model.HubMember
import com.example.ui.hub.data.FamilyHubRepository
import com.example.ui.profile.components.CreateChildProfileModal
import com.example.ui.profile.components.ProfileChildrenSection
import com.example.ui.profile.components.ProfileEditSection
import com.example.ui.profile.components.ProfileFamilyHubsSection
import com.example.ui.profile.components.ProfileHeaderSection
import com.example.ui.profile.components.ProfileLogoutSection
import com.example.ui.profile.components.ProfileReminderAlertsSection
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

    // Children Profiles State
    var childrenProfiles by remember { mutableStateOf<List<ChildProfileData>>(emptyList()) }
    var approvedHubMembersForChildModal by remember { mutableStateOf<List<HubMember>>(emptyList()) }
    var isCreateChildModalOpen by remember { mutableStateOf(false) }
    var isCreatingChild by remember { mutableStateOf(false) }
    var createChildErrorMessage by remember { mutableStateOf<String?>(null) }

    val pageAlpha by animateFloatAsState(
        targetValue = if (isPageLoaded) 1f else 0f,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "ProfileScreenAlpha"
    )

    // Load profile and hubs on entry
    LaunchedEffect(Unit) {
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
                        }
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
}
