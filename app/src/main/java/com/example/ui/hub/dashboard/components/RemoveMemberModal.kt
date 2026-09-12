package com.example.ui.hub.dashboard.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ThreeDotButtonLoader
import com.example.ui.hub.dashboard.model.HubMember
import com.example.ui.profilesetup.model.ProfileGender
import com.example.ui.theme.DarkWarmText
import com.example.ui.theme.DustyTeal
import com.example.ui.theme.MutedTerracotta
import com.example.ui.theme.SoraFontFamily
import com.example.ui.theme.WarmCream
import com.example.ui.theme.WarmIvory
import com.example.ui.theme.WarmNeutral

private val ColorWarmIvory = WarmIvory
private val ColorWarmCream = WarmCream
private val ColorDarkWarmText = DarkWarmText
private val ColorWarmNeutral = WarmNeutral
private val ColorDustyTeal = DustyTeal
private val ColorMutedTerracotta = MutedTerracotta
private val ColorBorderWarm = Color(0xFFE4D5C2)

@Composable
fun RemoveMemberModal(
    member: HubMember,
    isLoading: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onConfirmRemove: () -> Unit
) {
    var typedName by remember { mutableStateOf("") }

    // Pronoun mapping
    val pronoun = when (member.gender) {
        ProfileGender.MALE -> "him"
        ProfileGender.FEMALE -> "her"
        else -> "them"
    }

    val normalizedTargetName = member.name.trim().lowercase()
    val normalizedTypedName = typedName.trim().lowercase()
    val isNameValid = normalizedTypedName == normalizedTargetName && normalizedTargetName.isNotBlank()

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        containerColor = ColorWarmIvory,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "Remove ${member.name}?",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = ColorDarkWarmText,
                modifier = Modifier.testTag("remove_member_modal_title")
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Pronoun question
                Text(
                    text = "Are you sure you want to remove $pronoun?",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    color = ColorMutedTerracotta,
                    modifier = Modifier.testTag("remove_member_pronoun_text")
                )

                // Supporting explanation
                Text(
                    text = "Removing them will remove their access to this family hub and permanently remove their medication-related records from this hub.",
                    fontFamily = SoraFontFamily,
                    fontSize = 11.5.sp,
                    color = ColorWarmNeutral,
                    modifier = Modifier.testTag("remove_member_explanation_text")
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Name confirmation input
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Type ${member.name} to confirm",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = ColorDarkWarmText
                    )
                    OutlinedTextField(
                        value = typedName,
                        onValueChange = { typedName = it },
                        placeholder = {
                            Text(
                                text = member.name,
                                fontFamily = SoraFontFamily,
                                fontSize = 12.sp,
                                color = ColorWarmNeutral.copy(alpha = 0.5f)
                            )
                        },
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 12.sp, fontFamily = SoraFontFamily),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ColorDustyTeal,
                            unfocusedBorderColor = ColorBorderWarm,
                            focusedContainerColor = ColorWarmCream.copy(alpha = 0.4f),
                            unfocusedContainerColor = ColorWarmCream.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("remove_member_name_input")
                    )
                }

                if (!errorMessage.isNullOrBlank()) {
                    Text(
                        text = errorMessage,
                        fontFamily = SoraFontFamily,
                        fontSize = 11.5.sp,
                        color = ColorMutedTerracotta,
                        modifier = Modifier.testTag("remove_member_error_text")
                    )
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    enabled = !isLoading,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFE4D5C2)),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .testTag("remove_member_cancel_button")
                ) {
                    Text(
                        text = "Cancel",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = ColorDarkWarmText
                    )
                }

                Button(
                    onClick = onConfirmRemove,
                    enabled = isNameValid && !isLoading,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorMutedTerracotta,
                        disabledContainerColor = ColorMutedTerracotta.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .testTag("remove_member_confirm_button")
                ) {
                    if (isLoading) {
                        ThreeDotButtonLoader(
                            dotColor = ColorWarmIvory,
                            dotSize = 5.dp,
                            dotSpacing = 5.dp,
                            modifier = Modifier.testTag("remove_member_loading")
                        )
                    } else {
                        Text(
                            text = "Delete",
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = ColorWarmIvory
                        )
                    }
                }
            }
        },
        dismissButton = {}
    )
}
