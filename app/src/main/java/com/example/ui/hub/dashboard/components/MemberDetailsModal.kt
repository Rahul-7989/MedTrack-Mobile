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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Person
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
import com.example.ui.theme.WarmAmber
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

/**
 * Member details and action modal allowing the Hub Creator to view member info
 * and access the destructive "Remove" action in Muted Terracotta (#C8755D).
 */
@Composable
fun MemberDetailsModal(
    member: HubMember,
    onDismiss: () -> Unit,
    onRemoveClick: () -> Unit
) {
    val roleLabel = when {
        member.isCreator -> "Hub Creator"
        member.isChild -> "Dependent Child"
        else -> "Family Member"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ColorWarmIvory,
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
                        .background(ColorWarmCream),
                    contentAlignment = Alignment.Center
                ) {
                    ProfileAvatarView(
                        avatarType = member.avatarType,
                        size = 32.dp
                    )
                }
                Column {
                    Text(
                        text = member.name,
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = ColorDarkWarmText,
                        modifier = Modifier.testTag("member_details_name")
                    )
                    Text(
                        text = roleLabel,
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = ColorDustyTeal
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
                if (!member.isCreator && !member.isChild) {
                    Spacer(modifier = Modifier.height(4.dp))
                    // Destructive Remove button in Muted Terracotta (#C8755D)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ColorMutedTerracotta.copy(alpha = 0.12f))
                            .border(BorderStroke(1.dp, ColorMutedTerracotta.copy(alpha = 0.4f)), RoundedCornerShape(12.dp))
                            .clickable(onClick = onRemoveClick)
                            .padding(horizontal = 16.dp)
                            .testTag("member_remove_action_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = null,
                                tint = ColorMutedTerracotta,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Remove Member",
                                fontFamily = SoraFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = ColorMutedTerracotta
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
                    .testTag("member_details_close_button")
            ) {
                Text(
                    text = "Close",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = ColorDustyTeal
                )
            }
        }
    )
}
