package com.example.ui.hub.join

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.hub.components.HubTopBar
import com.example.ui.hub.data.FamilyHubRepository
import com.example.ui.hub.join.components.JoinHubBackgroundShapes
import com.example.ui.hub.join.components.JoinHubPanel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Scoped page background
private val ColorWarmIvory = Color(0xFFFAF4EC)

/**
 * MedTrack Join Family Hub Screen.
 *
 * Dedicated modular screen where the user enters/pastes a 6-character Hive Code
 * to join their family's shared medication space.
 *
 * Top bar contains:
 * - Left: Back button returning to Hub Selection
 * - Right: Profile Avatar icon with "My profile" and "Logout" options
 */
@Composable
fun JoinFamilyHubScreen(
    onJoinSuccess: () -> Unit,
    onNavigateToWaitingRoom: (hubId: String, hubName: String, requestId: String) -> Unit = { _, _, _ -> },
    onNavigateBack: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

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
        label = "JoinHubPageAlpha"
    )

    // Screen State
    var hubCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun handleJoin() {
        val trimmedCode = hubCode.trim().uppercase()

        if (trimmedCode.length != 6) {
            errorMessage = "Enter all 6 characters."
            return
        }

        if (!trimmedCode.all { it.isLetterOrDigit() }) {
            errorMessage = "Hub Codes use letters and numbers only."
            return
        }

        errorMessage = null
        isLoading = true

        coroutineScope.launch {
            val result = FamilyHubRepository.validateAndRequestToJoinHub(trimmedCode)
            isLoading = false

            when (result) {
                is com.example.ui.hub.model.HubJoinValidationResult.SuccessPending -> {
                    onNavigateToWaitingRoom(result.hub.hubId, result.hub.name, result.requestId)
                }
                is com.example.ui.hub.model.HubJoinValidationResult.SuccessAlreadyMember -> {
                    onJoinSuccess()
                }
                is com.example.ui.hub.model.HubJoinValidationResult.Error -> {
                    errorMessage = "${result.title}\n${result.message}"
                }
            }
        }
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
                .testTag("join_family_hub_screen")
        ) {
            val isCompact = maxWidth < 600.dp
            val scrollState = rememberScrollState()

            // 1. Abstract floating background geometry
            JoinHubBackgroundShapes(
                alpha = pageAlpha,
                isReducedMotion = isReducedMotion
            )

            // 2. Main content column with Top Bar & Centered content layout
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Bar: Back Button on Left, Profile Avatar on Right
                HubTopBar(
                    showBackButton = true,
                    onNavigateBack = onNavigateBack,
                    onMyProfileClick = onNavigateToProfile,
                    onLogout = onLogout,
                    modifier = Modifier.alpha(pageAlpha)
                )

                // Scrollable center panel
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .verticalScroll(scrollState)
                        .padding(
                            horizontal = if (isCompact) 16.dp else 24.dp,
                            vertical = if (isCompact) 16.dp else 24.dp
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    JoinHubPanel(
                        code = hubCode,
                        onCodeChange = {
                            hubCode = it
                            if (errorMessage != null) {
                                errorMessage = null
                            }
                        },
                        onJoinClick = ::handleJoin,
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        isCompact = isCompact,
                        modifier = Modifier.alpha(pageAlpha)
                    )
                }
            }
        }
    }
}
