package com.example.ui.profilesetup

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.profilesetup.components.ProfileSetupBackgroundShapes
import com.example.ui.profilesetup.components.ProfileSetupPanel
import com.example.ui.profilesetup.data.UserProfileRepository
import com.example.ui.profilesetup.model.ProfileGender
import com.example.ui.profilesetup.model.UserProfileData
import com.example.ui.profilesetup.model.toAvatarType

// Scoped page background
private val ColorWarmIvory = Color(0xFFFAF4EC)

/**
 * MedTrack Profile Setup Screen.
 *
 * Dedicated, independently modular page where the user sets up their basic family profile:
 * - Dynamic Profile Avatar based on selected gender (Male / Female / Anonymous)
 * - Required: Name, Gender, Age
 * - Optional: About Me (maximum 200 characters)
 * - Tactile Warm Cream elevated panel on Warm Ivory canvas
 */
@Composable
fun ProfileSetupScreen(
    onProfileSaved: (UserProfileData) -> Unit,
    modifier: Modifier = Modifier,
    initialName: String = "",
    initialGender: ProfileGender? = null,
    initialAge: Int = 18
) {
    val context = LocalContext.current

    // Detect system preference for reduced motion
    val isReducedMotion = remember {
        try {
            val resolver = context.contentResolver
            val transitionScale = android.provider.Settings.Global.getFloat(
                resolver,
                android.provider.Settings.Global.TRANSITION_ANIMATION_SCALE,
                1.0f
            )
            val animatorScale = android.provider.Settings.Global.getFloat(
                resolver,
                android.provider.Settings.Global.ANIMATOR_DURATION_SCALE,
                1.0f
            )
            transitionScale == 0f || animatorScale == 0f
        } catch (_: Exception) {
            false
        }
    }

    var isPageLoaded by remember { mutableStateOf(isReducedMotion) }

    LaunchedEffect(Unit) {
        if (!isReducedMotion) {
            isPageLoaded = true
        }
    }

    val pageAlpha by animateFloatAsState(
        targetValue = if (isPageLoaded) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "ProfilePageAlpha"
    )

    // Form State
    var name by remember { mutableStateOf(initialName) }
    var gender by remember { mutableStateOf<ProfileGender?>(initialGender) }
    var age by remember { mutableIntStateOf(initialAge) }
    var isAboutMeExpanded by remember { mutableStateOf(false) }
    var aboutMeText by remember { mutableStateOf("") }

    // Validation
    val isFormValid by remember {
        derivedStateOf {
            name.trim().isNotEmpty() && gender != null
        }
    }

    fun handleContinue() {
        val selectedGender = gender ?: return
        if (name.trim().isEmpty()) return

        val profile = UserProfileData(
            name = name.trim(),
            gender = selectedGender,
            age = age,
            aboutMe = aboutMeText.trim().ifEmpty { null },
            avatarType = selectedGender.toAvatarType(),
            isCompleted = true
        )

        UserProfileRepository.saveProfile(profile)
        onProfileSaved(profile)
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(ColorWarmIvory),
        containerColor = ColorWarmIvory
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .systemBarsPadding()
                .imePadding()
                .testTag("profile_setup_screen")
        ) {
            val isCompact = maxWidth < 600.dp
            val scrollState = rememberScrollState()

            // 1. Abstract floating background geometry
            ProfileSetupBackgroundShapes(
                alpha = pageAlpha,
                isReducedMotion = isReducedMotion
            )

            // 2. Centered content layout
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(
                        horizontal = if (isCompact) 16.dp else 24.dp,
                        vertical = if (isCompact) 20.dp else 36.dp
                    ),
                contentAlignment = Alignment.Center
            ) {
                ProfileSetupPanel(
                    name = name,
                    onNameChange = { name = it },
                    gender = gender,
                    onGenderChange = { gender = it },
                    age = age,
                    onAgeChange = { age = it },
                    isAboutMeExpanded = isAboutMeExpanded,
                    onAboutMeExpandedChange = { isAboutMeExpanded = it },
                    aboutMeText = aboutMeText,
                    onAboutMeTextChange = { aboutMeText = it },
                    isFormValid = isFormValid,
                    onContinueClick = ::handleContinue,
                    isCompact = isCompact,
                    isReducedMotion = isReducedMotion,
                    modifier = Modifier.alpha(pageAlpha)
                )
            }
        }
    }
}
