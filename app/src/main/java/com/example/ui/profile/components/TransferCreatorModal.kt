package com.example.ui.profile.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.hub.dashboard.model.HubMember
import com.example.ui.theme.*

@Composable
fun TransferCreatorModal(
    hubName: String,
    eligibleMembers: List<HubMember>,
    onDismiss: () -> Unit,
    onConfirmTransferAndLeave: (selectedMemberId: String) -> Unit,
    isTransferring: Boolean,
    errorMessage: String? = null
) {
    var selectedMember by remember { mutableStateOf<HubMember?>(null) }

    AlertDialog(
        onDismissRequest = { if (!isTransferring) onDismiss() },
        containerColor = WarmIvory,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column {
                Text(
                    text = "Before you leave",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = DarkWarmText
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "You’re the creator of \"$hubName\". Before you leave, choose a member to become the new creator.",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.5.sp,
                    color = WarmNeutral
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 260.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "NEW CREATOR",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.2.sp,
                    color = DustyTeal
                )

                if (eligibleMembers.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No eligible adult members found to transfer creator role.",
                            fontFamily = SoraFontFamily,
                            fontSize = 12.5.sp,
                            color = MutedTerracotta
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(WarmCream)
                            .border(BorderStroke(1.dp, Color(0xFFE4D5C2)), RoundedCornerShape(14.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(eligibleMembers) { member ->
                            val isSelected = selectedMember?.id == member.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) DustyTeal.copy(alpha = 0.15f) else Color.Transparent)
                                    .clickable { selectedMember = member }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                                    .testTag("transfer_member_item_${member.id}"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(WarmIvory)
                                            .border(BorderStroke(1.dp, Color(0xFFE4D5C2)), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Person,
                                            contentDescription = null,
                                            tint = DustyTeal,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = member.name,
                                            fontFamily = SoraFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.5.sp,
                                            color = DarkWarmText
                                        )
                                        Text(
                                            text = "Member",
                                            fontFamily = SoraFontFamily,
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 11.sp,
                                            color = WarmNeutral
                                        )
                                    }
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Outlined.Check,
                                        contentDescription = "Selected",
                                        tint = DustyTeal,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = MutedTerracotta,
                        modifier = Modifier.testTag("transfer_error")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedMember?.id?.let { onConfirmTransferAndLeave(it) }
                },
                enabled = selectedMember != null && !isTransferring,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DustyTeal,
                    contentColor = Color.White,
                    disabledContainerColor = DustyTeal.copy(alpha = 0.4f),
                    disabledContentColor = Color.White.copy(alpha = 0.6f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("confirm_transfer_leave_button")
            ) {
                if (isTransferring) {
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
                enabled = !isTransferring,
                modifier = Modifier.testTag("cancel_transfer_button")
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
