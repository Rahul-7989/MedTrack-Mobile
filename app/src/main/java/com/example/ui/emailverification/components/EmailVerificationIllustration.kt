package com.example.ui.emailverification.components

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

// Scoped palette for Email Verification Illustration
private val ColorDustyTeal = Color(0xFF72B5BA)
private val ColorPaleTeal = Color(0xFFA9CED0)
private val ColorWarmAmber = Color(0xFFE5A23C)
private val ColorWarmIvory = Color(0xFFFAF4EC)
private val ColorWarmCream = Color(0xFFF3E6D5)
private val ColorEnvelopeShadow = Color(0x24786550)

/**
 * Minimal, elegant email verification illustration.
 *
 * Visual construction:
 * - Soft warm background circle for ambient depth
 * - Rounded geometric envelope in Dusty Teal (#72B5BA) with Pale Teal fold flap lines
 * - Subtle Warm Amber accent
 * - Circular verification badge overlay with a crisp Warm Ivory (#FAF4EC) checkmark
 * - Gentle ease-out fade and 0.9 -> 1.0 scale transition for the checkmark badge
 * - Zero medical crosses, pills, or cybersecurity cliches
 */
@Composable
fun EmailVerificationIllustration(
    modifier: Modifier = Modifier,
    isReducedMotion: Boolean = false
) {
    var checkmarkVisible by remember { mutableStateOf(isReducedMotion) }

    LaunchedEffect(Unit) {
        if (!isReducedMotion) {
            // Small stagger before checkmark appears
            kotlinx.coroutines.delay(180)
            checkmarkVisible = true
        }
    }

    val checkmarkAlpha by animateFloatAsState(
        targetValue = if (checkmarkVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 400,
            easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f) // Gentle ease-out
        ),
        label = "CheckmarkAlpha"
    )

    val checkmarkScale by animateFloatAsState(
        targetValue = if (checkmarkVisible) 1f else 0.9f,
        animationSpec = tween(
            durationMillis = 400,
            easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f) // No bounce, tiny scale
        ),
        label = "CheckmarkScale"
    )

    Box(
        modifier = modifier
            .size(width = 100.dp, height = 86.dp)
            .semantics {
                contentDescription = "Illustration of an envelope with a verified checkmark"
            }
            .testTag("email_verification_illustration"),
        contentAlignment = Alignment.Center
    ) {
        // Base Canvas: Ambient soft glow, Envelope Body, and Flap lines
        Canvas(modifier = Modifier.size(width = 100.dp, height = 86.dp)) {
            val w = size.width
            val h = size.height

            // 1. Soft subtle circular backdrop for tactile warmth
            drawCircle(
                color = ColorWarmCream.copy(alpha = 0.65f),
                radius = w * 0.42f,
                center = Offset(w * 0.46f, h * 0.48f)
            )
            drawCircle(
                color = ColorPaleTeal.copy(alpha = 0.20f),
                radius = w * 0.36f,
                center = Offset(w * 0.44f, h * 0.46f)
            )

            // 2. Envelope Dimensions
            val envLeft = w * 0.12f
            val envTop = h * 0.24f
            val envWidth = w * 0.68f
            val envHeight = h * 0.54f
            val cornerR = 14f

            // Envelope subtle ambient shadow
            val shadowPath = Path().apply {
                addRoundRect(
                    RoundRect(
                        left = envLeft,
                        top = envTop + 3f,
                        right = envLeft + envWidth,
                        bottom = envTop + envHeight + 3f,
                        cornerRadius = CornerRadius(cornerR, cornerR)
                    )
                )
            }
            drawPath(path = shadowPath, color = ColorEnvelopeShadow)

            // Envelope main body in Dusty Teal
            val envBodyPath = Path().apply {
                addRoundRect(
                    RoundRect(
                        left = envLeft,
                        top = envTop,
                        right = envLeft + envWidth,
                        bottom = envTop + envHeight,
                        cornerRadius = CornerRadius(cornerR, cornerR)
                    )
                )
            }
            drawPath(path = envBodyPath, color = ColorDustyTeal)

            // Top envelope fold flap (subtle Pale Teal tone for dimension)
            val flapPath = Path().apply {
                moveTo(envLeft + 2f, envTop + 2f)
                lineTo(envLeft + envWidth / 2f, envTop + envHeight * 0.58f)
                lineTo(envLeft + envWidth - 2f, envTop + 2f)
            }
            drawPath(
                path = flapPath,
                color = ColorPaleTeal.copy(alpha = 0.45f),
                style = Stroke(width = 2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Envelope diagonal lower seams
            val lowerSeamLeft = Path().apply {
                moveTo(envLeft + 2f, envTop + envHeight - 2f)
                lineTo(envLeft + envWidth * 0.42f, envTop + envHeight * 0.52f)
            }
            val lowerSeamRight = Path().apply {
                moveTo(envLeft + envWidth - 2f, envTop + envHeight - 2f)
                lineTo(envLeft + envWidth * 0.58f, envTop + envHeight * 0.52f)
            }
            drawPath(
                path = lowerSeamLeft,
                color = Color(0xFF5E9DA2).copy(alpha = 0.55f),
                style = Stroke(width = 1.8f, cap = StrokeCap.Round)
            )
            drawPath(
                path = lowerSeamRight,
                color = Color(0xFF5E9DA2).copy(alpha = 0.55f),
                style = Stroke(width = 1.8f, cap = StrokeCap.Round)
            )

            // Small Warm Amber accent: Top flap apex dot
            drawCircle(
                color = ColorWarmAmber,
                radius = 3f,
                center = Offset(envLeft + envWidth / 2f, envTop + envHeight * 0.58f)
            )
        }

        // Checkmark badge overlay with subtle fade and 0.9 -> 1.0 scale
        Canvas(
            modifier = Modifier
                .size(width = 100.dp, height = 86.dp)
                .graphicsLayer {
                    alpha = checkmarkAlpha
                    scaleX = checkmarkScale
                    scaleY = checkmarkScale
                }
        ) {
            val w = size.width
            val h = size.height

            // Badge Position (Bottom-Right overlay)
            val badgeCenterX = w * 0.74f
            val badgeCenterY = h * 0.68f
            val badgeRadius = w * 0.165f

            // Badge ambient shadow
            drawCircle(
                color = Color(0x35604020),
                radius = badgeRadius + 2f,
                center = Offset(badgeCenterX, badgeCenterY + 2f)
            )

            // Badge circular background: Warm Amber
            drawCircle(
                color = ColorWarmAmber,
                radius = badgeRadius,
                center = Offset(badgeCenterX, badgeCenterY)
            )

            // Crisp inner border for tactile finish
            drawCircle(
                color = Color(0xFFF1C17C),
                radius = badgeRadius - 1.2f,
                center = Offset(badgeCenterX, badgeCenterY),
                style = Stroke(width = 1.2f)
            )

            // Crisp checkmark in Warm Ivory (#FAF4EC)
            val checkPath = Path().apply {
                moveTo(badgeCenterX - badgeRadius * 0.44f, badgeCenterY)
                lineTo(badgeCenterX - badgeRadius * 0.10f, badgeCenterY + badgeRadius * 0.38f)
                lineTo(badgeCenterX + badgeRadius * 0.46f, badgeCenterY - badgeRadius * 0.34f)
            }

            drawPath(
                path = checkPath,
                color = ColorWarmIvory,
                style = Stroke(
                    width = 3.2f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }
}
