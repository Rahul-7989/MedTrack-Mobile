package com.example.ui.hub.create

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
import com.example.ui.hub.create.components.CreateHubBackgroundShapes
import com.example.ui.hub.create.components.CreateHubPanel
import com.example.ui.hub.data.FamilyHubRepository
import kotlinx.coroutines.launch

// Scoped page background
private val ColorWarmIvory = Color(0xFFFAF4EC)

/**
 * MedTrack Create Family Hub Screen.
 *
 * Dedicated modular screen where the user:
 * 1. Names their family hub
 * 2. Generates an immutable 6-character Hive Code
 * 3. Copies and shares the code
 * 4. Proceeds into their Family Hive
 *
 * Top bar contains:
 * - Left: Back button returning to Hub Selection
 * - Right: Profile Avatar icon with "My profile" and "Logout" options
 */
@Composable
fun CreateFamilyHubScreen(
    onProceedToHub: () -> Unit,
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
        label = "CreateHubPageAlpha"
    )

    // Screen State
    var hubName by remember { mutableStateOf("") }
    var generatedHubCode by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    fun handleGenerateCode() {
        if (hubName.trim().isNotEmpty() && generatedHubCode == null) {
            generatedHubCode = FamilyHubRepository.generateRandomHiveCode()
        }
    }

    fun handleProceed() {
        val code = generatedHubCode ?: return
        if (hubName.trim().isEmpty() || isLoading) return

        isLoading = true
        coroutineScope.launch {
            FamilyHubRepository.createHub(hubName, code)
            isLoading = false
            onProceedToHub()
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
                .testTag("create_family_hub_screen")
        ) {
            val isCompact = maxWidth < 600.dp
            val scrollState = rememberScrollState()

            // 1. Abstract floating background geometry
            CreateHubBackgroundShapes(
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
                    CreateHubPanel(
                        hubName = hubName,
                        onHubNameChange = {
                            if (generatedHubCode == null) {
                                hubName = it
                            }
                        },
                        hiveCode = generatedHubCode,
                        onGenerateCode = ::handleGenerateCode,
                        onProceedClick = ::handleProceed,
                        isLoading = isLoading,
                        isCompact = isCompact,
                        modifier = Modifier.alpha(pageAlpha)
                    )
                }
            }
        }
    }
}
