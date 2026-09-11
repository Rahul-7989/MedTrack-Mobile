package com.example.ui.emailverification.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ThreeDotButtonLoader
import com.example.ui.theme.SoraFontFamily

// Scoped Button Palette
private val ColorWarmAmber = Color(0xFFE5A23C)
private val ColorBurntApricot = Color(0xFFD88B3D)
private val ColorAmberBorder = Color(0xFFEDB86A)
private val ColorTextLight = Color(0xFFFFFFFF)

/**
 * Primary "I've verified my email" action button.
 *
 * Visual style:
 * - Rounded pill shape (24.dp)
 * - Warm Amber (#E5A23C) transitioning toward Burnt Apricot (#D88B3D) on hover
 * - Subtle border (#EDB86A)
 * - 2px upward shift on hover with soft shadow elevation
 * - 0.98 scale response on press
 * - Sora 600 SemiBold typography
 * - Accessible 48dp+ touch target
 */
@Composable
fun VerifiedEmailPrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    isLoading: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()

    // Smooth hover color transition toward Burnt Apricot #D88B3D (200-300ms)
    val containerColor by animateColorAsState(
        targetValue = when {
            isPressed -> ColorBurntApricot
            isHovered -> ColorBurntApricot
            else -> ColorWarmAmber
        },
        animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
        label = "VerifyButtonColor"
    )

    // ~2px upward movement on hover
    val offsetY by animateDpAsState(
        targetValue = when {
            isPressed -> 0.dp
            isHovered -> (-2).dp
            else -> 0.dp
        },
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "VerifyButtonOffset"
    )

    // Soft shadow elevation increase on hover
    val elevation by animateDpAsState(
        targetValue = when {
            isPressed -> 1.dp
            isHovered -> 5.dp
            else -> 3.dp
        },
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "VerifyButtonElevation"
    )

    // Subtle scale-down around 0.98 on press
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1.0f,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "VerifyButtonScale"
    )

    val pillShape = RoundedCornerShape(24.dp)

    Button(
        onClick = onClick,
        enabled = isEnabled && !isLoading,
        interactionSource = interactionSource,
        shape = pillShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = ColorTextLight,
            disabledContainerColor = ColorWarmAmber.copy(alpha = 0.5f),
            disabledContentColor = ColorTextLight.copy(alpha = 0.7f)
        ),
        border = BorderStroke(1.dp, ColorAmberBorder),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = elevation,
            pressedElevation = 1.dp,
            hoveredElevation = elevation
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .offset(y = offsetY)
            .scale(scale)
            .hoverable(interactionSource = interactionSource, enabled = isEnabled && !isLoading)
            .shadow(
                elevation = elevation,
                shape = pillShape,
                ambientColor = ColorWarmAmber.copy(alpha = 0.20f),
                spotColor = ColorWarmAmber.copy(alpha = 0.30f)
            )
            .testTag("verify_email_primary_button")
    ) {
        if (isLoading) {
            ThreeDotButtonLoader(
                dotColor = ColorTextLight
            )
        } else {
            Text(
                text = "I've verified my email",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.5.sp,
                letterSpacing = 0.15.sp
            )
        }
    }
}
