package com.example.ui.hub.dashboard.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ThreeDotButtonLoader
import com.example.ui.hub.dashboard.model.HubMember
import com.example.ui.theme.DarkWarmText
import com.example.ui.theme.MutedTerracotta
import com.example.ui.theme.SoraFontFamily
import com.example.ui.theme.WarmIvory
import com.example.ui.theme.WarmNeutral

@Composable
fun ChildDeleteModal(
    child: HubMember,
    isLoading: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    var confirmationInput by remember { mutableStateOf("") }
    val isNameMatched = confirmationInput.trim().equals(child.name.trim(), ignoreCase = true)

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        containerColor = WarmIvory,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "Delete ${child.name}?",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = DarkWarmText,
                modifier = Modifier.testTag("child_delete_modal_title")
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Are you sure you want to delete ${child.name}? Deleting this child profile will permanently remove their profile and their medication-related records from this hub.",
                    fontFamily = SoraFontFamily,
                    fontSize = 11.5.sp,
                    color = WarmNeutral
                )

                Text(
                    text = "Type ${child.name} to confirm",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = DarkWarmText
                )

                OutlinedTextField(
                    value = confirmationInput,
                    onValueChange = { confirmationInput = it },
                    placeholder = { Text(child.name, fontSize = 12.sp) },
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 12.sp, fontFamily = SoraFontFamily),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MutedTerracotta,
                        unfocusedBorderColor = Color(0xFFE4D5C2)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("child_delete_confirmation_input")
                )

                if (!errorMessage.isNullOrBlank()) {
                    Text(
                        text = errorMessage,
                        fontFamily = SoraFontFamily,
                        fontSize = 12.sp,
                        color = MutedTerracotta,
                        modifier = Modifier.testTag("child_delete_error_text")
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
                        .testTag("child_delete_cancel_button")
                ) {
                    Text(
                        text = "Cancel",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = DarkWarmText
                    )
                }

                Button(
                    onClick = onConfirmDelete,
                    enabled = isNameMatched && !isLoading,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MutedTerracotta,
                        disabledContainerColor = MutedTerracotta.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .testTag("child_delete_confirm_button")
                ) {
                    if (isLoading) {
                        ThreeDotButtonLoader(
                            dotColor = WarmIvory,
                            dotSize = 5.dp,
                            dotSpacing = 5.dp,
                            modifier = Modifier.testTag("child_delete_loading")
                        )
                    } else {
                        Text(
                            text = "Delete",
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = WarmIvory
                        )
                    }
                }
            }
        }
    )
}
