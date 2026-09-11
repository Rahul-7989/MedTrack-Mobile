package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BurntApricot
import com.example.ui.theme.DustyTeal
import com.example.ui.theme.DustyTealText
import com.example.ui.theme.PaleTeal
import com.example.ui.theme.PaleTealBorder
import com.example.ui.theme.SoraFontFamily
import com.example.ui.theme.WarmAmber
import com.example.ui.theme.WarmCream
import com.example.ui.theme.WarmCreamBorder

private val PillShape = RoundedCornerShape(50)

/**
 * Primary "Get Started" tactile button with Warm Amber background,
 * subtle hover shift toward Burnt Apricot, smooth elevation, upward movement,
 * and scale-down feedback on click.
 */
@Composable
fun GetStartedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()

    val targetBgColor = when {
        isPressed -> Color(0xFFC97B2C) // Deeper burnt apricot under click
        isHovered -> BurntApricot
        else -> WarmAmber
    }

    val backgroundColor by animateColorAsState(
        targetValue = targetBgColor,
        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
        label = "GetStartedBgColor"
    )

    val elevation by animateDpAsState(
        targetValue = when {
            isPressed -> 2.dp
            isHovered -> 8.dp
            else -> 4.dp
        },
        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
        label = "GetStartedElevation"
    )

    val offsetY by animateDpAsState(
        targetValue = when {
            isPressed -> 1.dp
            isHovered -> (-2).dp
            else -> 0.dp
        },
        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
        label = "GetStartedOffsetY"
    )

    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.96f
            isHovered -> 1.015f
            else -> 1.0f
        },
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "GetStartedScale"
    )

    Box(
        modifier = modifier
            .offset(y = offsetY)
            .scale(scale)
            .shadow(
                elevation = elevation,
                shape = PillShape,
                ambientColor = Color(0x33B86C21),
                spotColor = Color(0x40E5A23C)
            )
            .clip(PillShape)
            .background(backgroundColor)
            .border(
                border = BorderStroke(1.dp, Color(0xFFF0BD77)),
                shape = PillShape
            )
            .hoverable(interactionSource = interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick
            )
            .defaultMinSize(minWidth = 134.dp, minHeight = 42.dp)
            .padding(horizontal = 20.dp, vertical = 9.5.dp)
            .testTag("get_started_button"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Get Started",
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.5.sp,
            letterSpacing = 0.2.sp,
            color = Color(0xFF221A12), // Rich warm espresso for maximum contrast and legibility
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Secondary "Login" tactile button with Warm Cream / Pale Teal background,
 * Dusty Teal text, soft border, subtle hover transition, and gentle upward shift.
 */
@Composable
fun LoginButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()

    val targetBgColor = when {
        isPressed -> Color(0xFFE5D5C1)
        isHovered -> PaleTeal.copy(alpha = 0.35f)
        else -> WarmCream
    }

    val backgroundColor by animateColorAsState(
        targetValue = targetBgColor,
        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
        label = "LoginBgColor"
    )

    val targetBorderColor = when {
        isHovered -> DustyTeal.copy(alpha = 0.55f)
        else -> WarmCreamBorder
    }

    val borderColor by animateColorAsState(
        targetValue = targetBorderColor,
        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
        label = "LoginBorderColor"
    )

    val elevation by animateDpAsState(
        targetValue = when {
            isPressed -> 1.dp
            isHovered -> 4.dp
            else -> 1.dp
        },
        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
        label = "LoginElevation"
    )

    val offsetY by animateDpAsState(
        targetValue = when {
            isPressed -> 1.dp
            isHovered -> (-2).dp
            else -> 0.dp
        },
        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
        label = "LoginOffsetY"
    )

    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.96f
            isHovered -> 1.01f
            else -> 1.0f
        },
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "LoginScale"
    )

    Box(
        modifier = modifier
            .offset(y = offsetY)
            .scale(scale)
            .shadow(
                elevation = elevation,
                shape = PillShape,
                ambientColor = Color(0x1872B5BA),
                spotColor = Color(0x2072B5BA)
            )
            .clip(PillShape)
            .background(backgroundColor)
            .border(
                border = BorderStroke(1.5.dp, borderColor),
                shape = PillShape
            )
            .hoverable(interactionSource = interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick
            )
            .defaultMinSize(minWidth = 118.dp, minHeight = 42.dp)
            .padding(horizontal = 18.dp, vertical = 9.5.dp)
            .testTag("login_button"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Log in",
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.5.sp,
            letterSpacing = 0.2.sp,
            color = DustyTealText,
            textAlign = TextAlign.Center
        )
    }
}
