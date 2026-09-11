package com.example.ui.emailverification

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
import com.example.data.auth.AuthResult
import com.example.data.auth.FirebaseAuthService
import com.example.ui.emailverification.components.EmailVerificationBackgroundShapes
import com.example.ui.emailverification.components.EmailVerificationPanel
import kotlinx.coroutines.launch

// Scoped page background
private val ColorWarmIvory = Color(0xFFFAF4EC)

/**
 * MedTrack Email Verification Screen.
 *
 * Dedicated, independently modular page designed to notify users that a
 * verification email has been dispatched and prompt them to confirm.
 */
@Composable
fun EmailVerificationScreen(
    onVerified: () -> Unit,
    modifier: Modifier = Modifier,
    email: String? = null,
    onResendVerification: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var verificationError by remember { mutableStateOf<String?>(null) }
    var isChecking by remember { mutableStateOf(false) }

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
        label = "VerifyPageAlpha"
    )

    fun handleCheckVerified() {
        if (isChecking) return
        isChecking = true
        verificationError = null

        coroutineScope.launch {
            val result = FirebaseAuthService.Instance.checkEmailVerified(email)
            isChecking = false
            when (result) {
                is AuthResult.Success -> {
                    if (result.data) {
                        onVerified()
                    } else {
                        verificationError = "Please verify your email before proceeding further"
                    }
                }
                is AuthResult.Error -> {
                    verificationError = result.message ?: "Please verify your email before proceeding further"
                }
            }
        }
    }

    fun handleResend() {
        coroutineScope.launch {
            FirebaseAuthService.Instance.resendVerificationEmail()
            onResendVerification()
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
                .testTag("email_verification_screen")
        ) {
            val isCompact = maxWidth < 600.dp
            val scrollState = rememberScrollState()

            // 1. Subtle, floating background geometric forms
            EmailVerificationBackgroundShapes(
                alpha = pageAlpha,
                isReducedMotion = isReducedMotion
            )

            // 2. Centered content presentation
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                .padding(
                    horizontal = if (isCompact) 16.dp else 24.dp,
                    vertical = if (isCompact) 24.dp else 40.dp
                ),
                contentAlignment = Alignment.Center
            ) {
                EmailVerificationPanel(
                    email = email ?: FirebaseAuthService.Instance.pendingVerificationEmail,
                    onVerifiedClick = { handleCheckVerified() },
                    onResendClick = { handleResend() },
                    isLoading = isChecking,
                    verificationError = verificationError,
                    isCompact = isCompact,
                    isReducedMotion = isReducedMotion,
                    modifier = Modifier.alpha(pageAlpha)
                )
            }
        }
    }
}
