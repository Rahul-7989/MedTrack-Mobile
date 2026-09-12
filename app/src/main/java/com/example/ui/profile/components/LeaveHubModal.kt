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
fun LeaveHubModal(
    hubName: String,
    onDismiss: () -> Unit,
    onConfirmLeave: () -> Unit,
    isLeaving: Boolean,
    errorMessage: String? = null
) {
    AlertDialog(
        onDismissRequest = { if (!isLeaving) onDismiss() },
        containerColor = WarmIvory,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column {
                Text(
                    text = "Leave this hub?",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = DarkWarmText
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Are you sure you want to leave \"$hubName\"?",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = WarmNeutral
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "If you leave, you may lose access to this hub's information and medications.",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.5.sp,
                    color = WarmNeutral
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = MutedTerracotta,
                        modifier = Modifier.testTag("leave_hub_error")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmLeave,
                enabled = !isLeaving,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DustyTeal,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("confirm_leave_hub_button")
            ) {
                if (isLeaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Leave Hub",
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
                enabled = !isLeaving,
                modifier = Modifier.testTag("cancel_leave_hub_button")
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
