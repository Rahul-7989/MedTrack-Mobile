package com.example.ui.getstarted.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SoraFontFamily

// Scoped color constants for Get Started Input Fields
private val InputSurface = Color(0xFFFAF4EC) // Warm Ivory #FAF4EC
private val BorderNormal = Color(0xFFE2D6C6) // Subtle warm border
private val BorderHover = Color(0xFFA6CCD0)  // Very subtle Dusty Teal
private val BorderFocus = Color(0xFF72B5BA)  // Primary Dusty Teal
private val BorderSuccess = Color(0xFF72B5BA) // Positive state
private val GlowTeal = Color(0x3372B5BA)     // Very subtle teal focus glow
private val MutedTerracotta = Color(0xFFC8755D) // Restrained warm terracotta for errors #C8755D
private val TextPrimary = Color(0xFF232B2B)
private val TextPlaceholder = Color(0xFF918A82)
private val TextLabel = Color(0xFF514A44)

/**
 * Isolated Email Input Field with custom styling, hover, focus-glow, concise error,
 * and subtle positive state on valid email format.
 */
@Composable
fun GetStartedEmailField(
    value: String,
    onValueChange: (String) -> Unit,
    isValid: Boolean,
    errorMessage: String? = null,
    errorTitle: String? = null,
    errorSupporting: String? = null,
    isError: Boolean = false,
    modifier: Modifier = Modifier,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    imeAction: ImeAction = ImeAction.Next
) {
    val showTrailingCheck = isValid && value.isNotBlank() && !isError && errorMessage == null && errorTitle == null
    GetStartedFormField(
        label = "Email",
        value = value,
        onValueChange = onValueChange,
        placeholder = "Enter your email",
        errorMessage = errorMessage,
        errorTitle = errorTitle,
        errorSupporting = errorSupporting,
        isError = isError,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = imeAction
        ),
        keyboardActions = keyboardActions,
        testTag = "get_started_email_input",
        trailingContent = if (showTrailingCheck) {
            {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .padding(end = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Valid email",
                        tint = BorderSuccess,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        } else null,
        modifier = modifier
    )
}

/**
 * Isolated Password Input Field with animated visibility toggle and focus glow.
 */
@Composable
fun GetStartedPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    isPasswordVisible: Boolean,
    onTogglePasswordVisibility: () -> Unit,
    errorMessage: String? = null,
    modifier: Modifier = Modifier,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    imeAction: ImeAction = ImeAction.Next
) {
    GetStartedFormField(
        label = "Password",
        value = value,
        onValueChange = onValueChange,
        placeholder = "Enter your password",
        errorMessage = errorMessage,
        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = imeAction
        ),
        keyboardActions = keyboardActions,
        testTag = "get_started_password_input",
        trailingContent = {
            PasswordVisibilityToggle(
                isPasswordVisible = isPasswordVisible,
                onToggle = onTogglePasswordVisibility,
                testTag = "toggle_get_started_password"
            )
        },
        modifier = modifier
    )
}

/**
 * Isolated Re-enter Password Input Field with animated visibility toggle and focus glow.
 */
@Composable
fun GetStartedConfirmPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    isPasswordVisible: Boolean,
    onTogglePasswordVisibility: () -> Unit,
    errorMessage: String? = null,
    modifier: Modifier = Modifier,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    imeAction: ImeAction = ImeAction.Done
) {
    GetStartedFormField(
        label = "Re-enter password",
        value = value,
        onValueChange = onValueChange,
        placeholder = "Re-enter your password",
        errorMessage = errorMessage,
        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = imeAction
        ),
        keyboardActions = keyboardActions,
        testTag = "get_started_confirm_password_input",
        trailingContent = {
            PasswordVisibilityToggle(
                isPasswordVisible = isPasswordVisible,
                onToggle = onTogglePasswordVisibility,
                testTag = "toggle_get_started_confirm_password"
            )
        },
        modifier = modifier
    )
}

@Composable
private fun PasswordVisibilityToggle(
    isPasswordVisible: Boolean,
    onToggle: () -> Unit,
    testTag: String
) {
    val rotation by animateFloatAsState(
        targetValue = if (isPasswordVisible) 0f else 180f,
        animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
        label = "VisibilityRotation"
    )
    val scale by animateFloatAsState(
        targetValue = if (isPasswordVisible) 1.05f else 0.95f,
        animationSpec = tween(durationMillis = 200),
        label = "VisibilityScale"
    )

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(22.dp))
            .clickable(
                onClick = onToggle,
                role = Role.Button
            )
            .semantics {
                contentDescription = if (isPasswordVisible) "Hide password" else "Show password"
            }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
            contentDescription = null,
            tint = BorderFocus,
            modifier = Modifier
                .size(19.dp)
                .scale(scale)
                .rotate(rotation)
        )
    }
}

/**
 * Base custom form input field designed exclusively for the MedTrack account creation experience.
 */
@Composable
private fun GetStartedFormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    errorMessage: String? = null,
    errorTitle: String? = null,
    errorSupporting: String? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    testTag: String = "",
    trailingContent: (@Composable () -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()

    val hasError = errorMessage != null || errorTitle != null || isError

    val targetBorderColor = when {
        hasError -> MutedTerracotta
        isFocused -> BorderFocus
        isHovered -> BorderHover
        else -> BorderNormal
    }

    val animatedBorderColor by animateColorAsState(
        targetValue = targetBorderColor,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "InputBorderColor"
    )

    val inputShape = RoundedCornerShape(16.dp)
    val inputElevation by animateDpAsState(
        targetValue = if (isFocused) 3.5.dp else if (isHovered) 2.5.dp else 1.5.dp,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "InputElevation"
    )

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Form Label
        Text(
            text = label,
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            letterSpacing = 0.1.sp,
            color = if (hasError) MutedTerracotta else TextLabel,
            modifier = Modifier.padding(bottom = 5.dp, start = 2.dp)
        )

        // Text Field Container - Raised, softly floating surface
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .shadow(
                    elevation = inputElevation,
                    shape = inputShape,
                    ambientColor = if (isFocused) GlowTeal else Color(0x189C876E),
                    spotColor = if (isFocused) GlowTeal else Color(0x209C876E)
                )
                .clip(inputShape)
                .background(InputSurface)
                .border(
                    width = if (isFocused || hasError) 1.5.dp else 1.dp,
                    color = animatedBorderColor,
                    shape = inputShape
                )
                .padding(start = 14.dp, end = if (trailingContent != null) 4.dp else 14.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.sp,
                            letterSpacing = 0.1.sp,
                            color = TextPlaceholder
                        )
                    }

                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(testTag),
                        textStyle = TextStyle(
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.sp,
                            letterSpacing = 0.1.sp,
                            color = TextPrimary
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(BorderFocus),
                        visualTransformation = visualTransformation,
                        keyboardOptions = keyboardOptions,
                        keyboardActions = keyboardActions,
                        interactionSource = interactionSource
                    )
                }

                if (trailingContent != null) {
                    trailingContent()
                }
            }
        }

        // Restrained Validation Error Message (fade-in + subtle 2-4px vertical movement)
        AnimatedVisibility(
            visible = hasError,
            enter = fadeIn(tween(180)) + slideInVertically(tween(180)) { -4 },
            exit = fadeOut(tween(120))
        ) {
            Column(modifier = Modifier.padding(start = 4.dp, top = 4.dp)) {
                if (errorTitle != null) {
                    Text(
                        text = errorTitle,
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = MutedTerracotta,
                        modifier = Modifier.testTag("get_started_email_error_title")
                    )
                }
                if (!errorSupporting.isNullOrBlank()) {
                    Text(
                        text = errorSupporting,
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.sp,
                        color = MutedTerracotta.copy(alpha = 0.9f),
                        modifier = Modifier
                            .padding(top = 1.dp)
                            .testTag("get_started_email_error_supporting")
                    )
                }
                if (errorMessage != null && errorTitle == null) {
                    Text(
                        text = errorMessage,
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.5.sp,
                        color = MutedTerracotta
                    )
                }
            }
        }
    }
}
