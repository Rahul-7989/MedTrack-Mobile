package com.example.ui.forgotpassword.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ThreeDotButtonLoader
import com.example.ui.theme.DarkWarmText
import com.example.ui.theme.DustyTeal
import com.example.ui.theme.SoraFontFamily
import com.example.ui.theme.WarmAmber
import com.example.ui.theme.WarmCream
import com.example.ui.theme.WarmIvory
import com.example.ui.theme.WarmNeutral

// Color constants for Forgot Password module matching MedTrack palette
private val InputSurface = WarmIvory
private val BorderNormal = Color(0xFFE2D6C6)
private val BorderHover = Color(0xFFA6CCD0)
private val BorderFocus = DustyTeal
private val GlowTeal = Color(0x3372B5BA)
private val MutedTerracotta = Color(0xFFC8755D) // Soft Muted Terracotta #C8755D
private val TextPrimary = Color(0xFF232B2B)
private val TextPlaceholder = Color(0xFF918A82)
private val ColorBurntApricot = Color(0xFFD88B3D)
private val ColorWarmCreamHover = Color(0xFFEFE0CD)
private val ColorReturnBorder = Color(0xFFE4D5C2)
private val ColorReturnBorderHover = DustyTeal

/**
 * Top-left back button that navigates back to the Login page.
 */
@Composable
fun ForgotPasswordBackButton(
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
        label = "ForgotBackOffset"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = tween(durationMillis = 150, easing = easeOutCurve),
        label = "ForgotBackScale"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isHovered || isFocused) ColorWarmCreamHover else WarmCream,
        animationSpec = tween(durationMillis = 240, easing = easeOutCurve),
        label = "ForgotBackBg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isHovered || isFocused) ColorReturnBorderHover else ColorReturnBorder,
        animationSpec = tween(durationMillis = 240, easing = easeOutCurve),
        label = "ForgotBackBorder"
    )

    val elevation by animateDpAsState(
        targetValue = if (isHovered) 3.dp else 0.dp,
        animationSpec = tween(durationMillis = 240, easing = easeOutCurve),
        label = "ForgotBackElevation"
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
                contentDescription = "Return to Login"
            }
            .testTag("forgot_password_back_button"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = null,
                tint = DustyTeal,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = "Login",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = DustyTeal,
                letterSpacing = 0.1.sp
            )
        }
    }
}

/**
 * Custom email input field for Forgot Password matching MedTrack design.
 */
@Composable
fun ForgotPasswordEmailField(
    value: String,
    onValueChange: (String) -> Unit,
    errorMessage: String? = null,
    modifier: Modifier = Modifier,
    onDone: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()

    val hasError = errorMessage != null

    val targetBorderColor = when {
        hasError -> MutedTerracotta
        isFocused -> BorderFocus
        isHovered -> BorderHover
        else -> BorderNormal
    }

    val animatedBorderColor by animateColorAsState(
        targetValue = targetBorderColor,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "ForgotEmailBorderColor"
    )

    val inputShape = RoundedCornerShape(16.dp)

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Email",
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = DarkWarmText,
            modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .then(
                    if (isFocused) {
                        Modifier.shadow(
                            elevation = 3.dp,
                            shape = inputShape,
                            ambientColor = GlowTeal,
                            spotColor = GlowTeal
                        )
                    } else Modifier
                )
                .clip(inputShape)
                .background(InputSurface)
                .border(
                    width = if (isFocused || hasError) 1.5.dp else 1.dp,
                    color = animatedBorderColor,
                    shape = inputShape
                )
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (value.isEmpty()) {
                Text(
                    text = "Enter your email",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.5.sp,
                    color = TextPlaceholder
                )
            }

            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.5.sp,
                    color = TextPrimary
                ),
                cursorBrush = SolidColor(DustyTeal),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { onDone() }
                ),
                interactionSource = interactionSource,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("forgot_password_email_input")
            )
        }

        // Inline Error Message in Muted Terracotta #C8755D
        AnimatedVisibility(
            visible = hasError,
            enter = fadeIn(animationSpec = tween(durationMillis = 200)),
            exit = fadeOut(animationSpec = tween(durationMillis = 150))
        ) {
            Text(
                text = errorMessage.orEmpty(),
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = MutedTerracotta,
                modifier = Modifier
                    .padding(top = 6.dp, start = 4.dp)
                    .testTag("forgot_password_inline_error")
            )
        }
    }
}

/**
 * Primary action button for Forgot Password.
 * Supports loading state via three-dot pulse animation without altering size, radius, or shadow.
 */
@Composable
fun ForgotPasswordPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    testTag: String = "forgot_password_submit_button"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    val isFocused by interactionSource.collectIsFocusedAsState()

    val easeOutCurve = remember { CubicBezierEasing(0.25f, 0.1f, 0.25f, 1.0f) }

    val targetBgColor = when {
        isPressed -> ColorBurntApricot
        isHovered || isFocused -> Color(0xFFE0983A)
        else -> WarmAmber
    }
    val animatedBgColor by animateColorAsState(
        targetValue = targetBgColor,
        animationSpec = tween(durationMillis = 240, easing = easeOutCurve),
        label = "ForgotBtnColor"
    )

    val offsetY by animateDpAsState(
        targetValue = if (isHovered && !isPressed) (-2).dp else 0.dp,
        animationSpec = tween(durationMillis = 220, easing = easeOutCurve),
        label = "ForgotBtnOffset"
    )

    val elevation by animateDpAsState(
        targetValue = when {
            isPressed -> 1.dp
            isHovered || isFocused -> 5.dp
            else -> 2.dp
        },
        animationSpec = tween(durationMillis = 220, easing = easeOutCurve),
        label = "ForgotBtnElevation"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1.0f,
        animationSpec = tween(durationMillis = 150, easing = easeOutCurve),
        label = "ForgotBtnScale"
    )

    val pillShape = RoundedCornerShape(24.dp)

    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        shape = pillShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = animatedBgColor,
            contentColor = Color.White,
            disabledContainerColor = WarmAmber.copy(alpha = 0.5f),
            disabledContentColor = Color.White.copy(alpha = 0.8f)
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
                ambientColor = WarmAmber.copy(alpha = 0.20f),
                spotColor = WarmAmber.copy(alpha = 0.30f)
            )
            .testTag(testTag)
    ) {
        if (isLoading) {
            ThreeDotButtonLoader(
                dotColor = Color.White
            )
        } else {
            Text(
                text = text,
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.5.sp,
                letterSpacing = 0.15.sp,
                color = Color.White
            )
        }
    }
}
