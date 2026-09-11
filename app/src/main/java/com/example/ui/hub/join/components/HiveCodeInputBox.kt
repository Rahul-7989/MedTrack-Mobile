package com.example.ui.hub.join.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkWarmText
import com.example.ui.theme.DustyTeal
import com.example.ui.theme.IbmPlexMonoFontFamily
import com.example.ui.theme.SoraFontFamily
import com.example.ui.theme.WarmCream
import com.example.ui.theme.WarmIvory
import com.example.ui.theme.WarmNeutral

// Scoped Palette matching MedTrack design system
private val ColorWarmIvory = WarmIvory
private val ColorWarmCream = WarmCream
private val ColorDustyTeal = DustyTeal
private val ColorDarkWarmText = DarkWarmText
private val ColorTextMuted = WarmNeutral
private val ColorBorderWarm = Color(0xFFE4D5C2)
private val ColorBorderEmptySlot = Color(0xFFDEC4AA)
private val ColorBorderFilledSlot = Color(0xFFD4B090)
private val ColorBorderActive = DustyTeal
private val ColorError = Color(0xFFC8755D)
private val ColorAmbientShadow = Color(0x109C876E)
private val ColorSpotShadow = Color(0x16786550)

/**
 * 6-position visual Hub Code input box with Warm Cream #F3E6D5 code blocks,
 * IBM Plex Mono monospace font, Dark Warm Text characters, and Dusty Teal focus highlight.
 */
@Composable
fun HubCodeInputBox(
    code: String,
    onCodeChange: (String) -> Unit,
    onImeDone: () -> Unit,
    errorMessage: String? = null,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    val textFieldValue = remember(code) {
        TextFieldValue(text = code, selection = TextRange(code.length))
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        // Section Header: "HUB CODE"
        Text(
            text = "HUB CODE",
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = ColorTextMuted,
            letterSpacing = 1.sp,
            modifier = Modifier.testTag("join_hub_code_label")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Visual 6-character Warm Cream code blocks layout
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    focusRequester.requestFocus()
                },
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 6) {
                    val char = code.getOrNull(i)?.toString() ?: ""
                    val isCurrentSlot = code.length == i
                    val isFilled = char.isNotEmpty()

                    val borderColor = when {
                        errorMessage != null -> ColorError
                        isCurrentSlot -> ColorBorderActive
                        isFilled -> ColorBorderFilledSlot
                        else -> ColorBorderEmptySlot
                    }

                    val borderWidth = if (isCurrentSlot) 2.dp else 1.2.dp

                    val slotElevation by animateDpAsState(
                        targetValue = if (isCurrentSlot) 4.dp else 2.dp,
                        animationSpec = tween(180),
                        label = "SlotElevation$i"
                    )

                    val ambientShadow = if (isCurrentSlot) Color(0x2072B5BA) else ColorAmbientShadow
                    val spotShadow = if (isCurrentSlot) Color(0x3072B5BA) else ColorSpotShadow

                    val slotSize = if (isCompact) 44.dp else 50.dp

                    Box(
                        modifier = Modifier
                            .size(slotSize)
                            .shadow(
                                elevation = slotElevation,
                                shape = RoundedCornerShape(12.dp),
                                ambientColor = ambientShadow,
                                spotColor = spotShadow
                            )
                            .clip(RoundedCornerShape(12.dp))
                            .background(ColorWarmCream)
                            .border(BorderStroke(borderWidth, borderColor), RoundedCornerShape(12.dp))
                            .testTag("hub_code_slot_$i"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isFilled) {
                            Text(
                                text = char,
                                fontFamily = IbmPlexMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = if (isCompact) 18.sp else 21.sp,
                                color = ColorDarkWarmText,
                                textAlign = TextAlign.Center
                            )
                        } else {
                            Text(
                                text = "_",
                                fontFamily = IbmPlexMonoFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = if (isCompact) 16.sp else 18.sp,
                                color = ColorTextMuted.copy(alpha = 0.45f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                    }
                }
            }

            // Invisible BasicTextField capturing keystrokes and paste events seamlessly
            BasicTextField(
                value = textFieldValue,
                onValueChange = { newValue ->
                    // Filter: Uppercase, alphanumeric only, max 6 chars
                    val filtered = newValue.text
                        .filter { it.isLetterOrDigit() }
                        .uppercase()
                        .take(6)
                    onCodeChange(filtered)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isCompact) 44.dp else 50.dp)
                    .focusRequester(focusRequester)
                    .testTag("hidden_hub_code_input"),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    keyboardType = KeyboardType.Ascii,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        onImeDone()
                    }
                ),
                cursorBrush = SolidColor(Color.Transparent),
                textStyle = TextStyle(color = Color.Transparent)
            )
        }

        // Inline error / status feedback
        AnimatedVisibility(
            visible = errorMessage != null,
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(150))
        ) {
            val lines = errorMessage.orEmpty().split("\n")
            if (lines.size > 1) {
                Column(
                    modifier = Modifier
                        .padding(top = 8.dp, start = 2.dp)
                        .testTag("join_hub_code_error_container")
                ) {
                    Text(
                        text = lines[0],
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = ColorError,
                        modifier = Modifier.testTag("join_hub_code_error_title")
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = lines.subList(1, lines.size).joinToString("\n"),
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = ColorError,
                        modifier = Modifier.testTag("join_hub_code_error_text")
                    )
                }
            } else {
                Text(
                    text = errorMessage.orEmpty(),
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.5.sp,
                    lineHeight = 18.sp,
                    color = ColorError,
                    modifier = Modifier
                        .padding(top = 8.dp, start = 2.dp)
                        .testTag("join_hub_code_error_text")
                )
            }
        }
    }
}

// Backward compatible alias
@Composable
fun HiveCodeInputBox(
    code: String,
    onCodeChange: (String) -> Unit,
    onImeDone: () -> Unit,
    errorMessage: String? = null,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    HubCodeInputBox(
        code = code,
        onCodeChange = onCodeChange,
        onImeDone = onImeDone,
        errorMessage = errorMessage,
        modifier = modifier,
        isCompact = isCompact
    )
}
