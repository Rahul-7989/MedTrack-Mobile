package com.example.ui.hub.join.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
private val ColorWarmAmberHover = Color(0xFFEDB14E)
private val ColorTextLight = Color(0xFFFAF4EC)
private val ColorDisabledSurface = Color(0xFFE2D6C6)
private val ColorDisabledText = Color(0xFFA89E92)
private val ColorShadowAmber = Color(0x35E5A23C)
private val ColorSpotShadow = Color(0x25786550)

/**
 * Primary Button for "Join the family hub".
 */
@Composable
fun JoinTheFamilyHubButton(
    onClick: () -> Unit,
    isEnabled: Boolean,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()

    val elevation by animateDpAsState(
        targetValue = when {
            !isEnabled || isLoading -> 0.dp
            isPressed -> 1.dp
            isHovered -> 6.dp
            else -> 3.dp
        },
        animationSpec = tween(durationMillis = 150),
        label = "JoinElevation"
    )

    val offsetY by animateDpAsState(
        targetValue = when {
            !isEnabled || isLoading -> 0.dp
            isPressed -> 0.dp
            isHovered -> (-2).dp
            else -> 0.dp
        },
        animationSpec = tween(durationMillis = 150),
        label = "JoinOffsetY"
    )

    val scale by animateFloatAsState(
        targetValue = when {
            !isEnabled || isLoading -> 1.0f
            isPressed -> 0.985f
            isHovered -> 1.01f
            else -> 1.0f
        },
        animationSpec = tween(durationMillis = 120),
        label = "JoinScale"
    )

    val pillShape = RoundedCornerShape(25.dp)

    Button(
        onClick = onClick,
        enabled = isEnabled && !isLoading,
        interactionSource = interactionSource,
        shape = pillShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isHovered && isEnabled && !isLoading) ColorWarmAmberHover else ColorWarmAmber,
            contentColor = ColorTextLight,
            disabledContainerColor = ColorDisabledSurface,
            disabledContentColor = ColorDisabledText
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            hoveredElevation = 0.dp,
            focusedElevation = 0.dp
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
                ambientColor = ColorShadowAmber,
                spotColor = ColorSpotShadow
            )
            .testTag("join_family_hub_button")
    ) {
        if (isLoading) {
            ThreeDotButtonLoader(
                dotColor = ColorTextLight
            )
        } else {
            Text(
                text = "Join the family hub",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.5.sp,
                letterSpacing = 0.15.sp
            )
        }
    }
}

// Backward compatible alias
@Composable
fun JoinTheFamilyHiveButton(
    onClick: () -> Unit,
    isEnabled: Boolean,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    JoinTheFamilyHubButton(
        onClick = onClick,
        isEnabled = isEnabled,
        isLoading = isLoading,
        modifier = modifier
    )
}
