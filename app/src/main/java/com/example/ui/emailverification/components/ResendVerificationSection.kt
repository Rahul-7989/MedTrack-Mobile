package com.example.ui.emailverification.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SoraFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Scoped Palette
private val ColorNeutralMuted = Color(0xFF665F58)
private val ColorDustyTeal = Color(0xFF72B5BA)
private val ColorDustyTealHover = Color(0xFF5A999E)

/**
 * Bottom Resend Verification Email section.
 *
 * Requirements:
 * - "Didn't receive the email?" in muted warm neutral (#665F58).
 * - "Resend Verification Email" in Dusty Teal (#72B5BA) as an interactive link.
 * - Single horizontal line whenever width permits, wrapping gracefully on narrow screens.
 * - Smooth hover transition (200-300ms) with optional subtle underline.
 * - Press response: subtle scale and color change.
 * - Resend interaction triggers callback and displays a subtle confirmation state
 *   "Verification email sent again." in Dusty Teal without displacing layout.
 * - Prevents repeated accidental clicks via an elegant 30-second cooldown timer.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ResendVerificationSection(
    onResendClick: () -> Unit,
    modifier: Modifier = Modifier,
    cooldownDurationSeconds: Int = 30
) {
    val coroutineScope = rememberCoroutineScope()
    var secondsRemaining by remember { mutableIntStateOf(0) }
    var showConfirmation by remember { mutableStateOf(false) }

    val isCooldownActive = secondsRemaining > 0

    // Cooldown countdown timer
    LaunchedEffect(isCooldownActive, secondsRemaining) {
        if (isCooldownActive) {
            delay(1000)
            secondsRemaining -= 1
        }
    }

    // Auto-hide confirmation after 6 seconds
    LaunchedEffect(showConfirmation) {
        if (showConfirmation) {
            delay(6000)
            showConfirmation = false
        }
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()

    val linkColor by animateColorAsState(
        targetValue = when {
            isCooldownActive -> ColorDustyTeal.copy(alpha = 0.50f)
            isPressed -> ColorDustyTealHover
            isHovered -> ColorDustyTealHover
            else -> ColorDustyTeal
        },
        animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
        label = "ResendLinkColor"
    )

    val linkScale by animateFloatAsState(
        targetValue = if (isPressed && !isCooldownActive) 0.97f else 1.0f,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "ResendLinkScale"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("resend_verification_section"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Horizontal arrangement with responsive wrapping for very narrow screens
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Didn't receive the email?",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.5.sp,
                color = ColorNeutralMuted,
                letterSpacing = 0.1.sp,
                modifier = Modifier.padding(end = 6.dp)
            )

            val actionText = if (isCooldownActive) {
                "Resend in ${secondsRemaining}s"
            } else {
                "Resend Verification Email"
            }

            Box(
                modifier = Modifier
                    .scale(linkScale)
                    .hoverable(interactionSource = interactionSource, enabled = !isCooldownActive)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        enabled = !isCooldownActive,
                        role = Role.Button,
                        onClick = {
                            if (!isCooldownActive) {
                                secondsRemaining = cooldownDurationSeconds
                                showConfirmation = true
                                onResendClick()
                            }
                        }
                    )
                    .semantics {
                        contentDescription = if (isCooldownActive) {
                            "Resend verification email available in $secondsRemaining seconds"
                        } else {
                            "Resend verification email"
                        }
                    }
                    .testTag("resend_verification_link")
            ) {
                Text(
                    text = actionText,
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.5.sp,
                    color = linkColor,
                    textDecoration = if (isHovered && !isCooldownActive) TextDecoration.Underline else TextDecoration.None,
                    letterSpacing = 0.1.sp
                )
            }
        }

        // Reserved subtle status area (prevents any page jumping / layout shift)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 22.dp)
                .padding(top = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            this@Column.AnimatedVisibility(
                visible = showConfirmation,
                enter = fadeIn(animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)),
                exit = fadeOut(animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing))
            ) {
                Text(
                    text = "Verification email sent again.",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.5.sp,
                    color = ColorDustyTeal,
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.1.sp,
                    modifier = Modifier.testTag("resend_confirmation_message")
                )
            }
        }
    }
}
