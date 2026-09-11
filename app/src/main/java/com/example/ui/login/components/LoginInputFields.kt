package com.example.ui.login.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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

// Scoped color constants for Login Input Fields
private val InputSurface = Color(0xFFFAF4EC) // Warm Ivory #FAF4EC
private val BorderNormal = Color(0xFFE2D6C6) // Subtle warm border
private val BorderHover = Color(0xFFA6CCD0)  // Very subtle Dusty Teal
private val BorderFocus = Color(0xFF72B5BA)  // Primary Dusty Teal
private val GlowTeal = Color(0x3372B5BA)     // Very subtle teal focus glow
private val MutedTerracotta = Color(0xFFC8755D) // Soft Muted Terracotta #C8755D
private val TextPrimary = Color(0xFF232B2B)
private val TextPlaceholder = Color(0xFF918A82)
private val TextLabel = Color(0xFF514A44)

/**
 * Isolated Email Input Field with custom styling, hover, focus-glow, and warm validation.
 */
@Composable
fun LoginEmailField(
    value: String,
    onValueChange: (String) -> Unit,
    errorMessage: String? = null,
    isError: Boolean = false,
    modifier: Modifier = Modifier,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    imeAction: ImeAction = ImeAction.Next
) {
    LoginFormField(
        label = "Email",
        value = value,
        onValueChange = onValueChange,
        placeholder = "Enter your email",
        errorMessage = errorMessage,
        isError = isError,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = imeAction
        ),
        keyboardActions = keyboardActions,
        testTag = "login_email_input",
        modifier = modifier
    )
}

/**
 * Isolated Password Input Field with animated visibility toggle and focus glow.
 */
@Composable
fun LoginPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    isPasswordVisible: Boolean,
    onTogglePasswordVisibility: () -> Unit,
    errorMessage: String? = null,
    isError: Boolean = false,
    modifier: Modifier = Modifier,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    imeAction: ImeAction = ImeAction.Done
) {
    LoginFormField(
        label = "Password",
        value = value,
        onValueChange = onValueChange,
        placeholder = "Enter your password",
        errorMessage = errorMessage,
        isError = isError,
        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = imeAction
        ),
        keyboardActions = keyboardActions,
        testTag = "login_password_input",
        trailingContent = {
            // Password visibility toggle in Dusty Teal with subtle animation
            val rotation by animateFloatAsState(
                targetValue = if (isPasswordVisible) 0f else 180f,
                animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
                label = "VisibilityRotation"
            )
            val scale by animateFloatAsState(
                targetValue = if (isPasswordVisible) 1.05f else 0.95f,
                animationSpec = tween(durationMillis = 200),
                label = "VisibilityScale"
            )

            Box(
                modifier = Modifier
                    .size(48.dp) // Accessible 48x48dp touch target
                    .clip(RoundedCornerShape(24.dp))
                    .clickable(
                        onClick = onTogglePasswordVisibility,
                        role = Role.Button
                    )
                    .semantics {
                        contentDescription = if (isPasswordVisible) "Hide password" else "Show password"
                    }
                    .testTag("toggle_password_visibility"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                    contentDescription = null,
                    tint = BorderFocus, // Dusty Teal #72B5BA
                    modifier = Modifier
                        .size(20.dp)
                        .scale(scale)
                        .rotate(rotation)
                )
            }
        },
        modifier = modifier
    )
}

/**
 * Base custom form input field designed exclusively for the MedTrack login experience.
 */
@Composable
private fun LoginFormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    errorMessage: String? = null,
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

    val hasError = errorMessage != null || isError

    // Determine current border color with smooth transitions (150-200ms)
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

    Column(modifier = modifier.fillMaxWidth()) {
        // Form Label (Sora Medium, clear and readable)
        Text(
            text = label,
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = TextLabel,
            modifier = Modifier.padding(bottom = 5.dp, start = 2.dp)
        )

        // Raised, softly floating input surface with large rounded corners and warm subtle border
        val shape = RoundedCornerShape(16.dp)
        val inputElevation by animateDpAsState(
            targetValue = if (isFocused) 3.5.dp else if (isHovered) 2.5.dp else 1.5.dp,
            animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
            label = "InputElevation"
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = inputElevation,
                    shape = shape,
                    ambientColor = if (isFocused) GlowTeal else Color(0x189C876E),
                    spotColor = if (isFocused) GlowTeal else Color(0x209C876E)
                )
                .clip(shape)
                .background(InputSurface)
                .border(
                    width = if (isFocused || hasError) 1.5.dp else 1.dp,
                    color = animatedBorderColor,
                    shape = shape
                )
                .padding(horizontal = 14.dp, vertical = 0.dp)
                .height(48.dp),
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
                    // Placeholder text
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.sp,
                            color = TextPlaceholder
                        )
                    }

                    // Native single-line BasicTextField for pristine custom styling
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(testTag),
                        interactionSource = interactionSource,
                        textStyle = TextStyle(
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.sp,
                            color = TextPrimary
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(BorderFocus),
                        visualTransformation = visualTransformation,
                        keyboardOptions = keyboardOptions,
                        keyboardActions = keyboardActions
                    )
                }

                if (trailingContent != null) {
                    trailingContent()
                }
            }
        }

        // Concise error message in warm restrained terracotta
        AnimatedVisibility(
            visible = hasError && errorMessage != null,
            enter = fadeIn(animationSpec = tween(150)),
            exit = fadeOut(animationSpec = tween(150))
        ) {
            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 11.5.sp,
                    color = MutedTerracotta,
                    modifier = Modifier.padding(top = 5.dp, start = 4.dp)
                )
            }
        }
    }
}
