package com.example.ui.getstarted.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SoraFontFamily

// Scoped color tokens
private val ColorDustyTeal = Color(0xFF72B5BA)
private val ColorNeutralMuted = Color(0xFF828C8D)
private val ColorTextDark = Color(0xFF263031)
private val ColorErrorRestrained = Color(0xFFC85A32)

data class PasswordRequirementsState(
    val hasMinLength: Boolean = false,
    val hasUppercase: Boolean = false,
    val hasNumber: Boolean = false,
    val hasSpecialChar: Boolean = false
) {
    val allSatisfied: Boolean
        get() = hasMinLength && hasUppercase && hasNumber && hasSpecialChar

    companion object {
        private val SPECIAL_CHARS = "!@#\$%^&*()_+-=[]{}|;':\",./<>?`~\\".toSet()

        fun from(password: String): PasswordRequirementsState {
            return PasswordRequirementsState(
                hasMinLength = password.length >= 6,
                hasUppercase = password.any { it.isUpperCase() },
                hasNumber = password.any { it.isDigit() },
                hasSpecialChar = password.any { it in SPECIAL_CHARS || (!it.isLetterOrDigit() && !it.isWhitespace()) }
            )
        }
    }
}

/**
 * Live animated password requirements list.
 * Displays below the password field and dynamically transforms from
 * neutral "○" to Dusty Teal "✓" as each requirement is met.
 */
@Composable
fun PasswordRequirementsView(
    state: PasswordRequirementsState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 2.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Password requirements",
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            letterSpacing = 0.1.sp,
            color = ColorNeutralMuted,
            modifier = Modifier.padding(bottom = 2.dp)
        )

        RequirementItem(
            isSatisfied = state.hasMinLength,
            label = "At least 6 characters",
            testTag = "req_min_length"
        )
        RequirementItem(
            isSatisfied = state.hasUppercase,
            label = "One uppercase letter",
            testTag = "req_uppercase"
        )
        RequirementItem(
            isSatisfied = state.hasNumber,
            label = "One number",
            testTag = "req_number"
        )
        RequirementItem(
            isSatisfied = state.hasSpecialChar,
            label = "One special character",
            testTag = "req_special"
        )
    }
}

@Composable
private fun RequirementItem(
    isSatisfied: Boolean,
    label: String,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val targetColor = if (isSatisfied) ColorDustyTeal else ColorNeutralMuted
    val textColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "ReqColor_$label"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag)
    ) {
        AnimatedContent(
            targetState = isSatisfied,
            transitionSpec = {
                fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(150))
            },
            label = "ReqIcon_$label"
        ) { satisfied ->
            Text(
                text = if (satisfied) "✓" else "○",
                fontFamily = SoraFontFamily,
                fontWeight = if (satisfied) FontWeight.Bold else FontWeight.Normal,
                fontSize = if (satisfied) 12.sp else 11.sp,
                color = if (satisfied) ColorDustyTeal else ColorNeutralMuted,
                modifier = Modifier.width(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = label,
            fontFamily = SoraFontFamily,
            fontWeight = if (isSatisfied) FontWeight.Medium else FontWeight.Normal,
            fontSize = 11.sp,
            letterSpacing = 0.1.sp,
            color = textColor
        )
    }
}

/**
 * Live password match indicator shown below the confirmation password field.
 * Empty: hidden
 * Differs: "✕ Passwords don't match" (restrained error color)
 * Matches: "✓ Passwords match" (Dusty Teal #72B5BA)
 */
@Composable
fun PasswordMatchIndicator(
    password: String,
    confirmPassword: String,
    modifier: Modifier = Modifier
) {
    val isVisible = confirmPassword.isNotEmpty()
    val isMatch = isVisible && password == confirmPassword

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(200)),
        exit = fadeOut(tween(150)),
        modifier = modifier
    ) {
        val indicatorColor by animateColorAsState(
            targetValue = if (isMatch) ColorDustyTeal else ColorErrorRestrained,
            animationSpec = tween(durationMillis = 200),
            label = "MatchColor"
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .testTag("password_match_indicator")
        ) {
            Text(
                text = if (isMatch) "✓" else "✕",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.5.sp,
                color = indicatorColor
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isMatch) "Passwords match" else "Passwords don't match",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = indicatorColor
            )
        }
    }
}
