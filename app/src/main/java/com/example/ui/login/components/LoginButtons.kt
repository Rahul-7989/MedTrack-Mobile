package com.example.ui.login.components

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

// Scoped button color tokens
private val ColorWarmAmber = Color(0xFFE5A23C)
private val ColorBurntApricot = Color(0xFFD88B3D)
private val ColorDustyTeal = Color(0xFF72B5BA)
private val ColorDustyTealDarker = Color(0xFF5B9BA0)
private val ColorTextLight = Color(0xFFFFFFFF)
private val ColorNeutralMuted = Color(0xFF665F58)
private val ColorWarmCream = Color(0xFFF3E6D5)
private val ColorWarmCreamHover = Color(0xFFEFE0CD)
private val ColorReturnBorder = Color(0xFFE4D5C2)
private val ColorReturnBorderHover = Color(0xFF72B5BA)

/**
 * Primary "Log in" pill button.
 * Uses Warm Amber #E5A23C (never teal) and transitions toward Burnt Apricot #D88B3D on hover,
 * with tactile 2px upward translation and ~0.98 scale-down on press.
 */
@Composable
fun LoginPrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    val isFocused by interactionSource.collectIsFocusedAsState()

    val easeOutCurve = remember { CubicBezierEasing(0.25f, 0.1f, 0.25f, 1.0f) }

    // Color transition: Warm Amber -> Burnt Apricot on hover/focus
    val targetBgColor = when {
        isPressed -> ColorBurntApricot
        isHovered || isFocused -> Color(0xFFE0983A)
        else -> ColorWarmAmber
    }
    val animatedBgColor by animateColorAsState(
        targetValue = targetBgColor,
        animationSpec = tween(durationMillis = 240, easing = easeOutCurve),
        label = "LoginBtnColor"
    )

    // Upward offset ~2px on hover
    val offsetY by animateDpAsState(
        targetValue = if (isHovered && !isPressed) (-2).dp else 0.dp,
        animationSpec = tween(durationMillis = 220, easing = easeOutCurve),
        label = "LoginBtnOffset"
    )

    // Elevation increase on hover
    val elevation by animateDpAsState(
        targetValue = when {
            isPressed -> 1.dp
            isHovered || isFocused -> 5.dp
            else -> 2.dp
        },
        animationSpec = tween(durationMillis = 220, easing = easeOutCurve),
        label = "LoginBtnElevation"
    )

    // Subtle scale down (~0.98) when pressed
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1.0f,
        animationSpec = tween(durationMillis = 150, easing = easeOutCurve),
        label = "LoginBtnScale"
    )

    val pillShape = RoundedCornerShape(24.dp)

    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        shape = pillShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = animatedBgColor,
            contentColor = ColorTextLight,
            disabledContainerColor = ColorWarmAmber.copy(alpha = 0.5f),
            disabledContentColor = ColorTextLight.copy(alpha = 0.8f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEDB86A)),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = elevation,
            pressedElevation = 1.dp,
            hoveredElevation = 5.dp,
            focusedElevation = 4.dp
        ),
        interactionSource = interactionSource,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .offset(y = offsetY)
            .scale(scale)
            .shadow(
                elevation = elevation,
                shape = pillShape,
                ambientColor = ColorWarmAmber.copy(alpha = 0.20f),
                spotColor = ColorWarmAmber.copy(alpha = 0.30f)
            )
            .testTag("login_submit_button")
    ) {
        if (isLoading) {
            ThreeDotButtonLoader(
                dotColor = ColorTextLight
            )
        } else {
            Text(
                text = "Log in",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.5.sp,
                letterSpacing = 0.15.sp
            )
        }
    }
}

/**
 * Secondary "Forgot password?" text link aligned to the right under the password field.
 * Uses Dusty Teal #72B5BA with subtle hover shift.
 */
@Composable
fun ForgotPasswordLink(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()

    val textColor by animateColorAsState(
        targetValue = if (isHovered || isPressed) ColorDustyTealDarker else ColorDustyTeal,
        animationSpec = tween(durationMillis = 200),
        label = "ForgotPwColor"
    )

    Box(
        modifier = modifier
            .padding(vertical = 2.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick
            )
            .testTag("forgot_password_link")
    ) {
        Text(
            text = "Forgot password?",
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.5.sp,
            color = textColor
        )
    }
}

/**
 * Bottom "Don't have an account? Get Started" footer component.
 * Guaranteed to appear on ONE continuous horizontal line on normal screens.
 * "Get Started" is an interactive Dusty Teal #72B5BA link that subtly shifts darker on hover.
 */
@Composable
fun LoginGetStartedFooter(
    onGetStartedClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()

    val linkColor by animateColorAsState(
        targetValue = if (isHovered || isPressed) ColorDustyTealDarker else ColorDustyTeal,
        animationSpec = tween(durationMillis = 200),
        label = "GetStartedLinkColor"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Don't have an account?",
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
                    onClick = onGetStartedClick
                )
                .padding(vertical = 4.dp, horizontal = 2.dp)
                .testTag("login_get_started_link")
        ) {
            Text(
                text = "Get Started",
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
 * Return/Home button positioned at the top left of the Login Page.
 * Styled with Warm Cream background, Dusty Teal text/icon, subtle border,
 * smooth hover upward shift, soft shadow, and tactile click scale.
 */
@Composable
fun ReturnHomeButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    val isFocused by interactionSource.collectIsFocusedAsState()

    val easeOutCurve = remember { CubicBezierEasing(0.25f, 0.1f, 0.25f, 1.0f) }

    // Subtle ~2px upward shift on hover
    val offsetY by animateDpAsState(
        targetValue = if (isHovered && !isPressed) (-2).dp else 0.dp,
        animationSpec = tween(durationMillis = 240, easing = easeOutCurve),
        label = "ReturnHomeOffset"
    )

    // Tactile pressed-state scale effect
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = tween(durationMillis = 150, easing = easeOutCurve),
        label = "ReturnHomeScale"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isHovered || isFocused) ColorWarmCreamHover else ColorWarmCream,
        animationSpec = tween(durationMillis = 240, easing = easeOutCurve),
        label = "ReturnHomeBg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isHovered || isFocused) ColorReturnBorderHover else ColorReturnBorder,
        animationSpec = tween(durationMillis = 240, easing = easeOutCurve),
        label = "ReturnHomeBorder"
    )

    val elevation by animateDpAsState(
        targetValue = if (isHovered) 3.dp else 0.dp,
        animationSpec = tween(durationMillis = 240, easing = easeOutCurve),
        label = "ReturnHomeElevation"
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
            .testTag("return_home_button"),
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
