package com.example.ui.profilesetup.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SoraFontFamily

// Scoped Input Palette
private val ColorFieldBg = Color(0xFFFAF4EC)
private val ColorBorderResting = Color(0xFFE4D5C2)
private val ColorBorderFocused = Color(0xFF72B5BA)
private val ColorLabel = Color(0xFF514A44)
private val ColorTextDark = Color(0xFF2E2824)
private val ColorPlaceholder = Color(0xFF9E958C)
private val ColorFieldShadow = Color(0x12786550)

/**
 * Elevated, tactile single-line text input for the user's display name.
 */
@Composable
fun ProfileNameInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val borderColor by animateColorAsState(
        targetValue = if (isFocused) ColorBorderFocused else ColorBorderResting,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "NameBorderColor"
    )

    val fieldShape = RoundedCornerShape(14.dp)

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Your name",
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.5.sp,
            color = ColorLabel,
            letterSpacing = 0.1.sp,
            modifier = Modifier.padding(start = 2.dp, bottom = 6.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = if (isFocused) 3.dp else 1.5.dp,
                    shape = fieldShape,
                    ambientColor = ColorFieldShadow,
                    spotColor = ColorFieldShadow
                )
                .clip(fieldShape)
                .background(ColorFieldBg)
                .border(BorderStroke(1.dp, borderColor), fieldShape)
                .padding(horizontal = 14.dp, vertical = 13.dp)
                .semantics {
                    contentDescription = "Your name input field"
                },
            contentAlignment = Alignment.CenterStart
        ) {
            if (value.isEmpty()) {
                Text(
                    text = "Enter your name",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.5.sp,
                    color = ColorPlaceholder,
                    letterSpacing = 0.1.sp
                )
            }

            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                interactionSource = interactionSource,
                singleLine = true,
                textStyle = TextStyle(
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.5.sp,
                    color = ColorTextDark,
                    letterSpacing = 0.1.sp
                ),
                cursorBrush = SolidColor(ColorBorderFocused),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = imeAction
                ),
                keyboardActions = KeyboardActions(
                    onAny = { onImeAction() }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_name_input")
            )
        }
    }
}
