package com.example.ui.getstarted.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ThreeDotButtonLoader
import com.example.ui.theme.SoraFontFamily

// Scoped color palette
private val ColorWarmAmber = Color(0xFFE5A23C)
private val ColorBurntApricot = Color(0xFFD88B3D)
private val ColorDustyTeal = Color(0xFF72B5BA)
private val ColorDustyTealDarker = Color(0xFF509297)
private val ColorWarmCream = Color(0xFFF3E6D5)
private val ColorWarmCreamHover = Color(0xFFEDE0CF)
private val ColorReturnBorder = Color(0xFFE4D5C2)
private val ColorReturnBorderHover = Color(0xFFC0DCDD)
private val ColorNeutralMuted = Color(0xFF665F58)
private val ColorDisabledBg = Color(0xFFEAE0D3)
private val ColorDisabledText = Color(0xFFA09689)
private val ColorTextLight = Color.White

/**
 * Primary "Create my account" button.
 * Smoothly transitions between disabled and active Warm Amber states.
 * Includes ~2px upward shift on hover, elevated shadow, and ~0.98 scale press effect.
 */
@Composable
fun CreateAccountButton(
    onClick: () -> Unit,
    isEnabled: Boolean,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()

    val easeOutCurve = remember { CubicBezierEasing(0.25f, 0.1f, 0.25f, 1.0f) }

    // Color transition between disabled, normal Warm Amber, and hovered Burnt Apricot
    val targetBgColor = when {
        !isEnabled -> ColorDisabledBg
        isHovered || isPressed -> ColorBurntApricot
        else -> ColorWarmAmber
    }

    val animatedBgColor by animateColorAsState(
        targetValue = targetBgColor,
        animationSpec = tween(durationMillis = 240, easing = easeOutCurve),
        label = "CreateAccountBtnBg"
    )

    val targetContentColor = if (isEnabled) ColorTextLight else ColorDisabledText
    val animatedContentColor by animateColorAsState(
        targetValue = targetContentColor,
        animationSpec = tween(durationMillis = 200),
        label = "CreateAccountBtnContentColor"
    )

    val elevation by animateDpAsState(
        targetValue = when {
            !isEnabled -> 0.dp
            isPressed -> 2.dp
            isHovered -> 7.dp
            else -> 3.dp
        },
        animationSpec = tween(durationMillis = 240, easing = easeOutCurve),
        label = "CreateAccountBtnElevation"
    )

    val offsetY by animateDpAsState(
        targetValue = if (isEnabled && isHovered && !isPressed) (-2).dp else 0.dp,
        animationSpec = tween(durationMillis = 240, easing = easeOutCurve),
        label = "CreateAccountBtnOffset"
    )

    val scale by animateFloatAsState(
        targetValue = if (isEnabled && isPressed) 0.98f else 1.0f,
        animationSpec = tween(durationMillis = 150, easing = easeOutCurve),
        label = "CreateAccountBtnScale"
    )

    val pillShape = RoundedCornerShape(24.dp)

    Button(
        onClick = onClick,
        enabled = isEnabled && !isLoading,
        shape = pillShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = animatedBgColor,
            contentColor = animatedContentColor,
            disabledContainerColor = ColorDisabledBg,
            disabledContentColor = ColorDisabledText
        ),
        border = if (isEnabled) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEDB86A)) else null,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = elevation,
            pressedElevation = 1.dp,
            hoveredElevation = 6.dp,
            focusedElevation = 4.dp
        ),
        interactionSource = interactionSource,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .offset(y = offsetY)
            .scale(scale)
            .then(
                if (isEnabled) {
                    Modifier.shadow(
                        elevation = elevation,
                        shape = pillShape,
                        ambientColor = ColorWarmAmber.copy(alpha = 0.20f),
                        spotColor = ColorWarmAmber.copy(alpha = 0.30f)
                    )
                } else Modifier
            )
            .testTag("create_account_button")
    ) {
        if (isLoading) {
            ThreeDotButtonLoader(
                dotColor = ColorTextLight
            )
        } else {
            Text(
                text = "Create my account",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.5.sp,
                letterSpacing = 0.15.sp
            )
        }
    }
}

/**
 * Bottom "Already have an account? Login here" footer component.
 * Positioned on ONE continuous horizontal line on desktop and tablet.
 * "Login here" is an interactive Dusty Teal #72B5BA link that subtly shifts darker on hover.
 */
@Composable
fun AlreadyHaveAccountFooter(
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()

    val linkColor by animateColorAsState(
        targetValue = if (isHovered || isPressed) ColorDustyTealDarker else ColorDustyTeal,
        animationSpec = tween(durationMillis = 200),
        label = "LoginLinkColor"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Already have an account?",
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            color = ColorNeutralMuted,
            softWrap = false
        )

        Spacer(modifier = Modifier.width(5.dp))

        Box(
            modifier = Modifier
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    role = Role.Button,
                    onClick = onLoginClick
                )
                .padding(vertical = 4.dp, horizontal = 2.dp)
                .testTag("get_started_login_link")
        ) {
            Text(
                text = "Login here",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = linkColor,
                softWrap = false
            )
        }
    }
}

/**
 * Top Home button styled with Warm Cream background, Dusty Teal text/icon,
 * subtle border, smooth hover upward shift, and tactile click feedback.
 * Navigates directly to the MedTrack Home Page.
 */
@Composable
fun GetStartedHomeButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    val isFocused by interactionSource.collectIsFocusedAsState()

    val easeOutCurve = remember { CubicBezierEasing(0.25f, 0.1f, 0.25f, 1.0f) }

    val offsetY by animateDpAsState(
        targetValue = if (isHovered && !isPressed) (-2).dp else 0.dp,
        animationSpec = tween(durationMillis = 240, easing = easeOutCurve),
        label = "HomeOffset"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = tween(durationMillis = 150, easing = easeOutCurve),
        label = "HomeScale"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isHovered || isFocused) ColorWarmCreamHover else ColorWarmCream,
        animationSpec = tween(durationMillis = 240, easing = easeOutCurve),
        label = "HomeBg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isHovered || isFocused) ColorReturnBorderHover else ColorReturnBorder,
        animationSpec = tween(durationMillis = 240, easing = easeOutCurve),
        label = "HomeBorder"
    )

    val elevation by animateDpAsState(
        targetValue = if (isHovered) 3.dp else 0.dp,
        animationSpec = tween(durationMillis = 240, easing = easeOutCurve),
        label = "HomeElevation"
    )

    val pillShape = RoundedCornerShape(18.dp)

    Box(
        modifier = modifier
            .offset(y = offsetY)
            .scale(scale)
            .then(
                if (elevation > 0.dp) {
                    Modifier.shadow(
                        elevation = elevation,
                        shape = pillShape,
                        ambientColor = Color(0x2072B5BA),
                        spotColor = Color(0x3072B5BA)
                    )
                } else Modifier
            )
            .clip(pillShape)
            .background(bgColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = pillShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 7.dp)
            .semantics {
                contentDescription = "Return to Home"
            }
            .testTag("get_started_home_button"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "←",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = ColorDustyTeal
            )
            Text(
                text = "Home",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = ColorDustyTeal,
                letterSpacing = 0.1.sp
            )
        }
    }
}

/**
 * Backward compatibility alias for GetStartedBackButton.
 */
@Composable
fun GetStartedBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GetStartedHomeButton(onClick = onClick, modifier = modifier)
}
