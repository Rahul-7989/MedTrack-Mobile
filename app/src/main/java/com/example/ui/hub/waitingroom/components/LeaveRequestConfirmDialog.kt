package com.example.ui.hub.waitingroom.components

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.SoraFontFamily

// MedTrack Warm Palette Colors
private val ColorWarmIvory = Color(0xFFFAF4EC)
private val ColorWarmCream = Color(0xFFF3E6D5)
private val ColorDarkWarmText = Color(0xFF514A44)
private val ColorWarmNeutral = Color(0xFF665F58)
private val ColorBorderWarm = Color(0xFFE4D5C2)
private val ColorWarmAmber = Color(0xFFE5A23C)
private val ColorDustyTeal = Color(0xFF72B5BA)
private val ColorAmbientShadow = Color(0x149C876E)
private val ColorSpotShadow = Color(0x1E786550)

/**
 * Confirmation dialog shown when the user taps "Back" while waiting for approval.
 *
 * Heading: "Leave this request?"
 * Supporting text: "If you go back, your request to join this family hub will be cancelled."
 * Buttons: "Stay here" (secondary) & "Yes, take me back" (primary Warm Amber).
 */
@Composable
fun LeaveRequestConfirmDialog(
    onDismiss: () -> Unit,
    onConfirmLeave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        val shape = RoundedCornerShape(22.dp)

        Box(
            modifier = modifier
                .fillMaxWidth(0.88f)
                .shadow(
                    elevation = 12.dp,
                    shape = shape,
                    ambientColor = ColorAmbientShadow,
                    spotColor = ColorSpotShadow
                )
                .clip(shape)
                .background(ColorWarmCream)
                .border(BorderStroke(1.2.dp, ColorBorderWarm), shape)
                .padding(24.dp)
                .testTag("leave_request_confirm_dialog"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Heading
                Text(
                    text = "Leave this request?",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = ColorDarkWarmText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("leave_request_dialog_title")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Supporting text
                Text(
                    text = "If you go back, your request to join this family hub will be cancelled.",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = ColorWarmNeutral,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("leave_request_dialog_message")
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Actions: "Stay here" & "Yes, take me back"
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Primary action: "Yes, take me back" (Warm Amber)
                    val confirmInteraction = remember { MutableInteractionSource() }
                    val buttonShape = RoundedCornerShape(14.dp)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .clip(buttonShape)
                            .background(ColorWarmAmber)
                            .clickable(
                                interactionSource = confirmInteraction,
                                indication = null,
                                role = Role.Button,
                                onClick = onConfirmLeave
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .testTag("leave_request_confirm_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Yes, take me back",
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.5.sp,
                            color = ColorWarmIvory,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Secondary quieter action: "Stay here"
                    val dismissInteraction = remember { MutableInteractionSource() }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .clip(buttonShape)
                            .background(ColorWarmCream)
                            .border(BorderStroke(1.dp, ColorBorderWarm), buttonShape)
                            .clickable(
                                interactionSource = dismissInteraction,
                                indication = null,
                                role = Role.Button,
                                onClick = onDismiss
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .testTag("leave_request_stay_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Stay here",
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = ColorDustyTeal,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
