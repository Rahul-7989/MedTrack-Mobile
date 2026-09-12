package com.example.ui.profile.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.MeetingRoom
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.profile.model.HubUserRole
import com.example.ui.profile.model.UserHubSummary
import com.example.ui.theme.DarkWarmText
import com.example.ui.theme.DustyTeal
import com.example.ui.theme.MutedTerracotta
import com.example.ui.theme.SoraFontFamily
import com.example.ui.theme.WarmAmber
import com.example.ui.theme.WarmCream
import com.example.ui.theme.WarmIvory
import com.example.ui.theme.WarmNeutral

// Design tokens
private val ColorWarmCream = WarmCream
private val ColorWarmIvory = WarmIvory
private val ColorDarkWarmText = DarkWarmText
private val ColorWarmNeutral = WarmNeutral
private val ColorDustyTeal = DustyTeal
private val ColorWarmAmber = WarmAmber
private val ColorBorderWarm = Color(0xFFE4D5C2)
private val ColorTagBg = Color(0xFFEFE2D3)
private val ColorCreatorTagBg = Color(0xFFE8F2F3)
private val ColorAmbientShadow = Color(0x109C876E)
private val ColorSpotShadow = Color(0x14786550)

/**
 * FAMILY HUBS section in Profile page.
 * Displays Create & Join actions and every hub the user belongs to.
 */
@Composable
fun ProfileFamilyHubsSection(
    hubs: List<UserHubSummary>,
    onCreateHubClick: () -> Unit,
    onJoinHubClick: () -> Unit,
    onHubCardClick: (UserHubSummary) -> Unit,
    onDeleteHubClick: (UserHubSummary) -> Unit,
    onLeaveHubClick: (UserHubSummary) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("profile_family_hubs_section")
    ) {
        // Section Title: FAMILY HUBS
        Text(
            text = "FAMILY HUBS",
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 11.5.sp,
            letterSpacing = 1.6.sp,
            color = ColorDustyTeal,
            modifier = Modifier.testTag("profile_hubs_kicker")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Create & Join Actions Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onCreateHubClick,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorWarmAmber,
                    contentColor = ColorDarkWarmText
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .testTag("profile_create_hub_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = null,
                    tint = ColorDarkWarmText,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Create Hub",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }

            OutlinedButton(
                onClick = onJoinHubClick,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.2.dp, ColorDustyTeal),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = ColorDustyTeal
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .testTag("profile_join_hub_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.MeetingRoom,
                    contentDescription = null,
                    tint = ColorDustyTeal,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Join Hub",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Subheading: YOUR HUBS
        Text(
            text = "YOUR HUBS",
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            letterSpacing = 1.4.sp,
            color = ColorWarmNeutral,
            modifier = Modifier.testTag("profile_your_hubs_kicker")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Existing Hubs list or Empty State
        if (hubs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ColorWarmCream)
                    .border(BorderStroke(1.dp, ColorBorderWarm), RoundedCornerShape(16.dp))
                    .padding(20.dp)
                    .testTag("profile_no_hubs_empty_state"),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.Hub,
                        contentDescription = null,
                        tint = ColorWarmNeutral.copy(alpha = 0.6f),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "No family hubs yet",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = ColorDarkWarmText
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Create a hub or join one to get started.",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = ColorWarmNeutral
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                hubs.forEach { hub ->
                    HubCardItem(
                        hub = hub,
                        onClick = { onHubCardClick(hub) },
                        onDeleteClick = { onDeleteHubClick(hub) },
                        onLeaveClick = { onLeaveHubClick(hub) },
                        modifier = Modifier.testTag("profile_hub_card_${hub.hubId}")
                    )
                }
            }
        }
    }
}

/**
 * Compact rounded hub card with avatar/icon, hub name, role, delete/leave icons, and navigation arrow.
 */
@Composable
private fun HubCardItem(
    hub: UserHubSummary,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onLeaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .scale(if (isPressed) 0.98f else 1.0f)
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = shape,
                ambientColor = ColorAmbientShadow,
                spotColor = ColorSpotShadow
            )
            .clip(shape)
            .background(ColorWarmCream)
            .border(BorderStroke(1.2.dp, ColorBorderWarm), shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .semantics { contentDescription = "${hub.name}, role ${hub.role.label}" }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Icon + Name & Role
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(ColorWarmIvory)
                        .border(BorderStroke(1.dp, ColorBorderWarm), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Group,
                        contentDescription = null,
                        tint = ColorDustyTeal,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = hub.name,
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = ColorDarkWarmText,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    val isCreator = hub.role == HubUserRole.CREATOR
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isCreator) ColorCreatorTagBg else ColorTagBg)
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = hub.role.label,
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.5.sp,
                            color = if (isCreator) ColorDustyTeal else ColorWarmNeutral
                        )
                    }
                }
            }

            // Right: Delete icon, Leave icon, Navigation Arrow
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val isCreator = hub.role == HubUserRole.CREATOR
                val hasOtherMembers = hub.membersCount > 1

                if (isCreator) {
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("delete_hub_icon_${hub.hubId}")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Delete hub ${hub.name}",
                            tint = MutedTerracotta,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (hasOtherMembers) {
                        IconButton(
                            onClick = onLeaveClick,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("leave_hub_icon_${hub.hubId}")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Logout,
                                contentDescription = "Leave hub ${hub.name}",
                                tint = ColorWarmNeutral,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                } else {
                    IconButton(
                        onClick = onLeaveClick,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("leave_hub_icon_${hub.hubId}")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Logout,
                            contentDescription = "Leave hub ${hub.name}",
                            tint = ColorWarmNeutral,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = "Open ${hub.name} dashboard",
                    tint = ColorDustyTeal,
                    modifier = Modifier
                        .size(18.dp)
                        .padding(start = 4.dp)
                )
            }
        }
    }
}
