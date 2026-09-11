package com.example.ui.profilesetup.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SoraFontFamily

// Scoped About Me Palette
private val ColorFieldBg = Color(0xFFFAF4EC)
private val ColorBorderResting = Color(0xFFE4D5C2)
private val ColorBorderFocused = Color(0xFF72B5BA)
private val ColorDustyTeal = Color(0xFF72B5BA)
private val ColorNeutral = Color(0xFF665F58)
private val ColorTextDark = Color(0xFF2E2824)
private val ColorPlaceholder = Color(0xFF9E958C)
private val ColorTriggerBg = Color(0xFFFAF4EC)
private val ColorShadow = Color(0x12786550)

/**
 * Optional About Me section.
 *
 * Collapsed by default to keep the profile flow minimal and friendly.
 * When activated, smoothly reveals a multiline text area strictly limited to 200 characters
 * with a live character counter and an unobtrusive "Remove" option.
 */
@Composable
fun AboutMeSection(
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    aboutMeText: String,
    onAboutMeChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val borderColor by animateColorAsState(
        targetValue = if (isFocused) ColorBorderFocused else ColorBorderResting,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "AboutMeBorderColor"
    )

    val fieldShape = RoundedCornerShape(14.dp)
    val triggerShape = RoundedCornerShape(18.dp)

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!isExpanded) {
            // Collapsed state: Friendly prompt + compact pill trigger
            Text(
                text = "Want to tell your family a little about you?",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                color = ColorNeutral,
                letterSpacing = 0.1.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Box(
                modifier = Modifier
                    .shadow(
                        elevation = 1.dp,
                        shape = triggerShape,
                        ambientColor = ColorShadow,
                        spotColor = ColorShadow
                    )
                    .clip(triggerShape)
                    .background(ColorTriggerBg)
                    .border(BorderStroke(1.dp, ColorBorderResting), triggerShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.Button,
                        onClick = { onExpandedChange(true) }
                    )
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .semantics {
                        contentDescription = "Add something about me optional field"
                    }
                    .testTag("add_about_me_trigger"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+ Add something about me",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = ColorDustyTeal,
                    letterSpacing = 0.1.sp
                )
            }
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(animationSpec = tween(280, easing = FastOutSlowInEasing)) +
                    fadeIn(animationSpec = tween(280, easing = FastOutSlowInEasing)),
            exit = shrinkVertically(animationSpec = tween(220, easing = FastOutSlowInEasing)) +
                    fadeOut(animationSpec = tween(220, easing = FastOutSlowInEasing))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "About you (optional)",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.5.sp,
                        color = ColorNeutral,
                        letterSpacing = 0.1.sp,
                        modifier = Modifier.padding(start = 2.dp)
                    )

                    Text(
                        text = "Remove",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.5.sp,
                        color = ColorNeutral,
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                role = Role.Button,
                                onClick = {
                                    onAboutMeChange("")
                                    onExpandedChange(false)
                                }
                            )
                            .padding(4.dp)
                            .testTag("about_me_remove_button")
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = if (isFocused) 3.dp else 1.5.dp,
                            shape = fieldShape,
                            ambientColor = ColorShadow,
                            spotColor = ColorShadow
                        )
                        .clip(fieldShape)
                        .background(ColorFieldBg)
                        .border(BorderStroke(1.dp, borderColor), fieldShape)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .semantics {
                            contentDescription = "About you multiline text area"
                        },
                    contentAlignment = Alignment.TopStart
                ) {
                    if (aboutMeText.isEmpty()) {
                        Text(
                            text = "Tell your family a little about you...",
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.sp,
                            color = ColorPlaceholder,
                            lineHeight = 18.sp,
                            letterSpacing = 0.1.sp
                        )
                    }

                    BasicTextField(
                        value = aboutMeText,
                        onValueChange = { newText ->
                            // Strictly enforce 200 maximum character limit
                            if (newText.length <= 200) {
                                onAboutMeChange(newText)
                            }
                        },
                        interactionSource = interactionSource,
                        minLines = 3,
                        maxLines = 4,
                        textStyle = TextStyle(
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = ColorTextDark,
                            letterSpacing = 0.1.sp
                        ),
                        cursorBrush = SolidColor(ColorBorderFocused),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("about_me_input")
                    )
                }

                // Live Character Counter: "0 / 200", "56 / 200"
                Text(
                    text = "${aboutMeText.length} / 200",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 11.sp,
                    color = ColorNeutral,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 4.dp, end = 4.dp)
                        .testTag("about_me_char_counter")
                )
            }
        }
    }
}
