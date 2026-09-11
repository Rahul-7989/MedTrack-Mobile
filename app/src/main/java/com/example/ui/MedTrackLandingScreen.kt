package com.example.ui

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BackgroundOrganicShapes
import com.example.ui.components.GetStartedButton
import com.example.ui.components.LoginButton
import com.example.ui.components.MedTrackLogo
import com.example.ui.theme.DustyTeal
import com.example.ui.theme.MedTrackTextPrimary
import com.example.ui.theme.MedTrackTextSecondary
import com.example.ui.theme.SoraFontFamily
import kotlinx.coroutines.launch

/**
 * Editorial-style brand landing page for MedTrack.
 * Contains ONLY:
 * 1. Logo placeholder
 * 2. App name: "MedTrack"
 * 3. Tagline: "The family medication companion."
 * 4. "Login" button
 * 5. "Get Started" button
 */
@Composable
fun MedTrackLandingScreen(
    modifier: Modifier = Modifier,
    onNavigateToLogin: () -> Unit = {},
    onNavigateToGetStarted: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Check system preference for reduced motion
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

    // Staggered entrance animation triggers
    var startAnimations by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        startAnimations = true
    }

    // Easing curve: refined cubic bezier
    val editorialEasing = remember { CubicBezierEasing(0.25f, 0.1f, 0.25f, 1.0f) }

    // Background reveal
    val bgAlpha by animateFloatAsState(
        targetValue = if (startAnimations) 1f else 0f,
        animationSpec = if (isReducedMotion) tween(0) else tween(durationMillis = 800, easing = editorialEasing),
        label = "BgReveal"
    )

    // Logo placeholder animation: softly fades in and scales from 96% to 100%
    val logoAlpha by animateFloatAsState(
        targetValue = if (startAnimations) 1f else 0f,
        animationSpec = if (isReducedMotion) tween(0) else tween(durationMillis = 700, delayMillis = 100, easing = editorialEasing),
        label = "LogoAlpha"
    )
    val logoScale by animateFloatAsState(
        targetValue = if (startAnimations) 1f else 0.96f,
        animationSpec = if (isReducedMotion) tween(0) else tween(durationMillis = 700, delayMillis = 100, easing = editorialEasing),
        label = "LogoScale"
    )

    // "MedTrack" title animation: smooth entrance (translateY 8dp -> 0dp, 400ms ease-out)
    val titleAlpha by animateFloatAsState(
        targetValue = if (startAnimations) 1f else 0f,
        animationSpec = if (isReducedMotion) tween(0) else tween(durationMillis = 400, delayMillis = 200, easing = editorialEasing),
        label = "TitleAlpha"
    )
    val titleOffsetY by animateFloatAsState(
        targetValue = if (startAnimations) 0f else 8f,
        animationSpec = if (isReducedMotion) tween(0) else tween(durationMillis = 400, delayMillis = 200, easing = editorialEasing),
        label = "TitleOffsetY"
    )

    // Tagline animation: subtle staggered fade-up
    val taglineAlpha by animateFloatAsState(
        targetValue = if (startAnimations) 1f else 0f,
        animationSpec = if (isReducedMotion) tween(0) else tween(durationMillis = 400, delayMillis = 350, easing = editorialEasing),
        label = "TaglineAlpha"
    )
    val taglineOffsetY by animateFloatAsState(
        targetValue = if (startAnimations) 0f else 8f,
        animationSpec = if (isReducedMotion) tween(0) else tween(durationMillis = 400, delayMillis = 350, easing = editorialEasing),
        label = "TaglineOffsetY"
    )

    // Buttons animation: appears last with gentle fade-up
    val buttonsAlpha by animateFloatAsState(
        targetValue = if (startAnimations) 1f else 0f,
        animationSpec = if (isReducedMotion) tween(0) else tween(durationMillis = 650, delayMillis = 600, easing = editorialEasing),
        label = "ButtonsAlpha"
    )
    val buttonsOffsetY by animateFloatAsState(
        targetValue = if (startAnimations) 0f else 18f,
        animationSpec = if (isReducedMotion) tween(0) else tween(durationMillis = 650, delayMillis = 600, easing = editorialEasing),
        label = "ButtonsOffsetY"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("medtrack_container")
    ) {
        // Decorative artistic background
        BackgroundOrganicShapes(
            isReducedMotion = isReducedMotion,
            revealProgress = bgAlpha
        )

        // Main hero content
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            val isWideScreen = maxWidth >= 540.dp
            val logoSize = if (isWideScreen) 114.dp else 98.dp
            val titleFontSize = if (isWideScreen) 40.sp else 33.sp
            val titleLineHeight = if (isWideScreen) 48.sp else 40.sp
            val taglineFontSize = if (isWideScreen) 13.5.sp else 12.5.sp
            val taglineLineHeight = if (isWideScreen) 20.sp else 18.sp

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 1. Official MedTrack Logo (Softly fades in and scales from ~96% to 100%)
                Box(
                    modifier = Modifier
                        .alpha(logoAlpha)
                        .scale(logoScale)
                ) {
                    MedTrackLogo(sizeDp = logoSize)
                }

                Spacer(modifier = Modifier.height(22.dp))

                // 2. App Name: "MedTrack" (Strongest typographic element: Sora 800/900 Black + Dusty Teal #72B5BA)
                Text(
                    text = "MedTrack",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = titleFontSize,
                    lineHeight = titleLineHeight,
                    letterSpacing = (-0.6).sp,
                    color = DustyTeal,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .offset(y = titleOffsetY.dp)
                        .alpha(titleAlpha)
                        .testTag("app_title")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 3. Tagline: "The family medication companion." (Smaller and lighter Sora weight, warm neutral #665F58)
                Text(
                    text = "The family medication companion.",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = taglineFontSize,
                    lineHeight = taglineLineHeight,
                    letterSpacing = 0.1.sp,
                    color = Color(0xFF665F58),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .offset(y = taglineOffsetY.dp)
                        .alpha(taglineAlpha)
                        .widthIn(max = 420.dp)
                        .testTag("app_tagline")
                )

                Spacer(modifier = Modifier.height(34.dp))

                // 4 & 5. Action buttons [ Login ] [ Get Started ]
                Box(
                    modifier = Modifier
                        .offset(y = buttonsOffsetY.dp)
                        .alpha(buttonsAlpha)
                        .widthIn(max = 480.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isWideScreen) {
                        // Desktop / Wide Layout: Side-by-side [ Login ] [ Get Started ]
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LoginButton(
                                onClick = onNavigateToLogin
                            )
                            GetStartedButton(
                                onClick = onNavigateToGetStarted
                            )
                        }
                    } else {
                        // Mobile Layout: Comfortable vertical stack
                        Column(
                            modifier = Modifier.fillMaxWidth().widthIn(max = 260.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            GetStartedButton(
                                onClick = onNavigateToGetStarted,
                                modifier = Modifier.fillMaxWidth()
                            )
                            LoginButton(
                                onClick = onNavigateToLogin,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        // Floating snackbar host for smooth interaction feedback
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
        )
    }
}
