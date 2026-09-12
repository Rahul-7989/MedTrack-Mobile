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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.hub.dashboard.model.HubMember
import com.example.ui.profilesetup.components.ProfileAvatarView
import com.example.ui.theme.DarkWarmText
import com.example.ui.theme.DustyTeal
import com.example.ui.theme.MutedTerracotta
import com.example.ui.theme.SoraFontFamily
import com.example.ui.theme.WarmCream
import com.example.ui.theme.WarmIvory
import com.example.ui.theme.WarmNeutral

@Composable
fun ChildDetailsModal(
    child: HubMember,
    isAuthorizedToDelete: Boolean,
    onDismiss: () -> Unit,
    onDeleteClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = WarmIvory,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(WarmCream),
                    contentAlignment = Alignment.Center
                ) {
                    ProfileAvatarView(
                        avatarType = child.avatarType,
                        size = 32.dp
                    )
                }
                Column {
                    Text(
                        text = child.name,
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = DarkWarmText,
                        modifier = Modifier.testTag("child_details_name")
                    )
                    Text(
                        text = "Dependent Child",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = DustyTeal
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!child.reminderResponsibleMemberName.isNullOrBlank()) {
                    Text(
                        text = "Remind: ${child.reminderResponsibleMemberName}",
                        fontFamily = SoraFontFamily,
                        fontSize = 12.sp,
                        color = WarmNeutral
                    )
                }

                if (isAuthorizedToDelete) {
                    Spacer(modifier = Modifier.height(4.dp))
                    // Destructive Delete button in Muted Terracotta (#C8755D)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MutedTerracotta.copy(alpha = 0.12f))
                            .border(BorderStroke(1.dp, MutedTerracotta.copy(alpha = 0.4f)), RoundedCornerShape(12.dp))
                            .clickable(onClick = onDeleteClick)
                            .padding(horizontal = 16.dp)
                            .testTag("child_delete_action_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = null,
                                tint = MutedTerracotta,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Delete",
                                fontFamily = SoraFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = MutedTerracotta
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onDismiss)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("child_details_cancel_button")
            ) {
                Text(
                    text = "Cancel",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF72B5BA)
                )
            }
        }
    )
}
