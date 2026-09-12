package com.example.ui.profile.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun DeleteHubModal(
    hubName: String,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit,
    isDeleting: Boolean,
    errorMessage: String? = null
) {
    var typedName by remember { mutableStateOf("") }
    val isExactMatch = typedName == hubName

    AlertDialog(
        onDismissRequest = { if (!isDeleting) onDismiss() },
        containerColor = WarmIvory,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column {
                Text(
                    text = "Delete this hub?",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = DarkWarmText
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Are you sure you want to delete this family hub?",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = MutedTerracotta
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "You can't retrieve this information after the hub is deleted. All members will be removed and the hub will be permanently deleted.",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.5.sp,
                    color = WarmNeutral
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Type \"$hubName\" to confirm:",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = DarkWarmText
                )

                OutlinedTextField(
                    value = typedName,
                    onValueChange = { typedName = it },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MutedTerracotta,
                        unfocusedBorderColor = Color(0xFFE4D5C2),
                        focusedContainerColor = WarmCream,
                        unfocusedContainerColor = WarmCream
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("delete_hub_name_input")
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = MutedTerracotta,
                        modifier = Modifier.testTag("delete_hub_error")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmDelete,
                enabled = isExactMatch && !isDeleting,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MutedTerracotta,
                    contentColor = Color.White,
                    disabledContainerColor = MutedTerracotta.copy(alpha = 0.4f),
                    disabledContentColor = Color.White.copy(alpha = 0.6f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("confirm_delete_hub_button")
            ) {
                if (isDeleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Delete Hub",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isDeleting,
                modifier = Modifier.testTag("cancel_delete_hub_button")
            ) {
                Text(
                    text = "Cancel",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = WarmNeutral
                )
            }
        }
    )
}
