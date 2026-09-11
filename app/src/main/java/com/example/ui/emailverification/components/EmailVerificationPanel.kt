package com.example.ui.emailverification.components

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SoraFontFamily
import kotlinx.coroutines.delay

// Scoped panel palette
private val PanelSurface = Color(0xFFF3E6D5)     // Warm Cream #F3E6D5
private val PanelBorder = Color(0xFFE4D5C2)      // Very subtle warm border
private val TextTitle = Color(0xFF72B5BA)        // Dusty Teal #72B5BA - strong Sora 800/900 presence
private val TextNeutral = Color(0xFF665F58)      // Warm Neutral #665F58
private val ShadowWarmAmbient = Color(0x189C876E)
private val ShadowWarmSpot = Color(0x22786550)

/**
 * Floating Warm Cream email verification panel.
 *
 * Implements a refined, soft-edged aesthetic:
 * - 28dp rounded corners
 * - Delicate warm border (#E4D5C2)
 * - Soft diffused ambient and spot shadows
 * - Generous internal padding
 * - Centered vertical hierarchy with staggered entrance:
 *   1. Minimal verification illustration
 *   2. Extra-extra bold "Check your email" heading
 *   3. Supporting message & secondary line
 *   4. "I've verified my email" primary button
 *   5. "Didn't receive the email? Resend Verification Email" section
 */
@Composable
fun EmailVerificationPanel(
    email: String?,
    onVerifiedClick: () -> Unit,
    onResendClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    verificationError: String? = null,
    isCompact: Boolean = false,
    isReducedMotion: Boolean = false
) {
    val cornerRadius = 28.dp
    val panelShape = RoundedCornerShape(cornerRadius)

    val horizontalPadding = if (isCompact) 20.dp else 30.dp
    val verticalPadding = if (isCompact) 24.dp else 32.dp

    // Staggered entrance animation steps
    var animStep1 by remember { mutableStateOf(isReducedMotion) }
    var animStep2 by remember { mutableStateOf(isReducedMotion) }
    var animStep3 by remember { mutableStateOf(isReducedMotion) }
    var animStep4 by remember { mutableStateOf(isReducedMotion) }
    var animStep5 by remember { mutableStateOf(isReducedMotion) }

    LaunchedEffect(Unit) {
        if (!isReducedMotion) {
            animStep1 = true
            delay(100)
            animStep2 = true
            delay(100)
            animStep3 = true
            delay(120)
            animStep4 = true
            delay(120)
            animStep5 = true
        }
    }

    val easeOut = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)

    val alpha1 by animateFloatAsState(
        targetValue = if (animStep1) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = easeOut),
        label = "AnimAlpha1"
    )
    val offsetY1 by animateFloatAsState(
        targetValue = if (animStep1) 0f else 12f,
        animationSpec = tween(durationMillis = 400, easing = easeOut),
        label = "AnimOffset1"
    )

    val alpha2 by animateFloatAsState(
        targetValue = if (animStep2) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = easeOut),
        label = "AnimAlpha2"
    )
    val offsetY2 by animateFloatAsState(
        targetValue = if (animStep2) 0f else 12f,
        animationSpec = tween(durationMillis = 400, easing = easeOut),
        label = "AnimOffset2"
    )

    val alpha3 by animateFloatAsState(
        targetValue = if (animStep3) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = easeOut),
        label = "AnimAlpha3"
    )
    val offsetY3 by animateFloatAsState(
        targetValue = if (animStep3) 0f else 10f,
        animationSpec = tween(durationMillis = 400, easing = easeOut),
        label = "AnimOffset3"
    )

    val alpha4 by animateFloatAsState(
        targetValue = if (animStep4) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = easeOut),
        label = "AnimAlpha4"
    )
    val offsetY4 by animateFloatAsState(
        targetValue = if (animStep4) 0f else 10f,
        animationSpec = tween(durationMillis = 400, easing = easeOut),
        label = "AnimOffset4"
    )

    val alpha5 by animateFloatAsState(
        targetValue = if (animStep5) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = easeOut),
        label = "AnimAlpha5"
    )
    val offsetY5 by animateFloatAsState(
        targetValue = if (animStep5) 0f else 8f,
        animationSpec = tween(durationMillis = 400, easing = easeOut),
        label = "AnimOffset5"
    )

    Box(
        modifier = modifier
            .widthIn(max = 440.dp)
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = panelShape,
                ambientColor = ShadowWarmAmbient,
                spotColor = ShadowWarmSpot
            )
            .clip(panelShape)
            .background(PanelSurface)
            .border(
                border = BorderStroke(1.dp, PanelBorder),
                shape = panelShape
            )
            .padding(horizontal = horizontalPadding, vertical = verticalPadding)
            .testTag("email_verification_panel")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Minimal email verification illustration
            Box(
                modifier = Modifier
                    .offset(y = offsetY1.dp)
                    .alpha(alpha1),
                contentAlignment = Alignment.Center
            ) {
                EmailVerificationIllustration(
                    isReducedMotion = isReducedMotion
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Extra-Extra Bold Heading: "Check your email" (Sora 800/900 + Dusty Teal - KEPT UNCHANGED)
            Text(
                text = "Check your email",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Black,
                fontSize = if (isCompact) 21.sp else 23.sp,
                lineHeight = if (isCompact) 27.sp else 29.sp,
                letterSpacing = (-0.45).sp,
                color = TextTitle,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = offsetY2.dp)
                    .alpha(alpha2)
                    .testTag("email_verification_heading")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 3. Supporting message:
            // "We've sent a verification link to your email address." (or with specific email)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = offsetY3.dp)
                    .alpha(alpha3),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!email.isNullOrBlank()) {
                    Text(
                        text = "We've sent a verification link to",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = if (isCompact) 15.sp else 16.sp,
                        lineHeight = if (isCompact) 21.sp else 22.sp,
                        color = TextNeutral,
                        textAlign = TextAlign.Center,
                        letterSpacing = 0.1.sp,
                        modifier = Modifier.testTag("email_verification_supporting_message")
                    )
                    Text(
                        text = email,
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = if (isCompact) 15.sp else 16.sp,
                        lineHeight = if (isCompact) 21.sp else 22.sp,
                        color = Color(0xFF514A44),
                        textAlign = TextAlign.Center,
                        letterSpacing = 0.1.sp,
                        modifier = Modifier.padding(top = 2.dp).testTag("email_verification_email_address")
                    )
                } else {
                    Text(
                        text = "We've sent a verification link to your email address.",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = if (isCompact) 15.sp else 16.sp,
                        lineHeight = if (isCompact) 21.sp else 22.sp,
                        color = TextNeutral,
                        textAlign = TextAlign.Center,
                        letterSpacing = 0.1.sp,
                        modifier = Modifier.testTag("email_verification_supporting_message")
                    )
                }

                Spacer(modifier = Modifier.height(5.dp))

                // 4. Secondary supporting line: "Please verify your email to continue."
                Text(
                    text = "Please verify your email to continue.",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = if (isCompact) 15.sp else 16.sp,
                    lineHeight = if (isCompact) 21.sp else 22.sp,
                    color = TextNeutral,
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.1.sp,
                    modifier = Modifier.testTag("email_verification_secondary_message")
                )
            }

            // Verification Error Banner
            if (verificationError != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFAF2EE))
                        .border(BorderStroke(1.dp, Color(0xFFE8CBC2)), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("email_verification_error_banner"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = verificationError,
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = if (isCompact) 14.sp else 15.sp,
                        lineHeight = if (isCompact) 19.sp else 20.sp,
                        color = Color(0xFFC8755D),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 5. Primary button: "I've verified my email"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = offsetY4.dp)
                    .alpha(alpha4)
            ) {
                VerifiedEmailPrimaryButton(
                    onClick = onVerifiedClick,
                    isLoading = isLoading
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 6. Secondary resend option at the bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = offsetY5.dp)
                    .alpha(alpha5)
            ) {
                ResendVerificationSection(
                    onResendClick = onResendClick
                )
            }
        }
    }
}
