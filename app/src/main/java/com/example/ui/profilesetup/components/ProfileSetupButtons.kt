package com.example.ui.profilesetup.components

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
private val ColorDisabledBg = Color(0xFFDECFC0)
private val ColorDisabledText = Color(0xFF9E9184)

/**
 * Primary "Continue" button for Profile Setup.
 *
 * Implements MedTrack dimensional tactile design:
 * - Rounded pill shape (24.dp)
 * - Warm Amber (#E5A23C) transitioning toward Burnt Apricot (#D88B3D) on hover
 * - 2px upward lift on hover with elevation boost
 * - 0.98 scale response on press
 * - Clean disabled state when required fields are incomplete
 * - Sora 600 SemiBold typography
 */
@Composable
fun ProfileContinueButton(
    onClick: () -> Unit,
    isEnabled: Boolean,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()

    val containerColor by animateColorAsState(
        targetValue = when {
            !isEnabled -> ColorDisabledBg
            isPressed -> ColorBurntApricot
            isHovered -> ColorBurntApricot
            else -> ColorWarmAmber
        },
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "ContinueButtonColor"
    )

    val offsetY by animateDpAsState(
        targetValue = when {
            !isEnabled -> 0.dp
            isPressed -> 0.dp
            isHovered -> (-2).dp
            else -> 0.dp
        },
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "ContinueButtonOffset"
    )

    val elevation by animateDpAsState(
        targetValue = when {
            !isEnabled -> 0.dp
            isPressed -> 1.dp
            isHovered -> 5.dp
            else -> 3.dp
        },
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "ContinueButtonElevation"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed && isEnabled) 0.98f else 1.0f,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "ContinueButtonScale"
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
            disabledContainerColor = ColorDisabledBg,
            disabledContentColor = ColorDisabledText
        ),
        border = if (isEnabled) BorderStroke(1.dp, ColorAmberBorder) else null,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = elevation,
            pressedElevation = 1.dp,
            hoveredElevation = elevation,
            disabledElevation = 0.dp
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .offset(y = offsetY)
            .scale(scale)
            .hoverable(interactionSource = interactionSource, enabled = isEnabled && !isLoading)
            .shadow(
                elevation = elevation,
                shape = pillShape,
                ambientColor = if (isEnabled) ColorWarmAmber.copy(alpha = 0.20f) else Color.Transparent,
                spotColor = if (isEnabled) ColorWarmAmber.copy(alpha = 0.30f) else Color.Transparent
            )
            .testTag("profile_continue_button")
    ) {
        if (isLoading) {
            ThreeDotButtonLoader(
                dotColor = ColorTextLight
            )
        } else {
            Text(
                text = "Continue",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                letterSpacing = 0.15.sp
            )
        }
    }
}
