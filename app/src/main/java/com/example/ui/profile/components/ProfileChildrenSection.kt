package com.example.ui.profile.components

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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.profile.model.ChildProfileData
import com.example.ui.profile.model.UserHubSummary
import com.example.ui.profilesetup.components.ProfileAvatarView
import com.example.ui.profilesetup.model.ProfileGender
import com.example.ui.theme.DarkWarmText
import com.example.ui.theme.DustyTeal
import com.example.ui.theme.SoraFontFamily
import com.example.ui.theme.WarmAmber
import com.example.ui.theme.WarmCream
import com.example.ui.theme.WarmIvory
import com.example.ui.theme.WarmNeutral

private val ColorWarmCream = WarmCream
private val ColorWarmIvory = WarmIvory
private val ColorDarkWarmText = DarkWarmText
private val ColorWarmNeutral = WarmNeutral
private val ColorDustyTeal = DustyTeal
private val ColorWarmAmber = WarmAmber
private val ColorBorderWarm = Color(0xFFE4D5C2)
private val ColorTagBg = Color(0xFFFDF0DC)
private val ColorAmbientShadow = Color(0x109C876E)
private val ColorSpotShadow = Color(0x14786550)

/**
 * CHILDREN PROFILES section on the Profile page.
 * Placed directly below the REMINDER ALERTS section.
 */
@Composable
fun ProfileChildrenSection(
    userHubs: List<UserHubSummary>,
    selectedHub: UserHubSummary?,
    children: List<ChildProfileData>,
    onSelectHub: (UserHubSummary) -> Unit,
    onCreateChildClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isHubDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("profile_children_section")
    ) {
        // Section Header: CHILDREN PROFILES
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "CHILDREN PROFILES",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.5.sp,
                letterSpacing = 1.6.sp,
                color = ColorDustyTeal,
                modifier = Modifier.testTag("profile_children_kicker")
            )

            if (children.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(ColorWarmCream)
                        .border(BorderStroke(1.dp, ColorBorderWarm), RoundedCornerShape(8.dp))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${children.size} Children",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp,
                        color = ColorWarmNeutral
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Hub Selector Dropdown (if user belongs to multiple hubs or to select active hub context)
        if (userHubs.isNotEmpty()) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 2.dp,
                            shape = RoundedCornerShape(14.dp),
                            ambientColor = ColorAmbientShadow,
                            spotColor = ColorSpotShadow
                        )
                        .clip(RoundedCornerShape(14.dp))
                        .background(ColorWarmCream)
                        .border(BorderStroke(1.2.dp, ColorBorderWarm), RoundedCornerShape(14.dp))
                        .clickable(
                            role = Role.Button,
                            onClick = {
                                if (userHubs.size > 1) {
                                    isHubDropdownExpanded = true
                                }
                            }
                        )
                        .padding(horizontal = 14.dp, vertical = 11.dp)
                        .testTag("profile_children_hub_dropdown"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "FAMILY HUB",
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.5.sp,
                            letterSpacing = 1.2.sp,
                            color = ColorWarmNeutral
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = selectedHub?.name ?: "Select Hub",
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = ColorDarkWarmText
                        )
                    }

                    if (userHubs.size > 1) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowDropDown,
                            contentDescription = "Switch Hub",
                            tint = ColorDarkWarmText,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = isHubDropdownExpanded,
                    onDismissRequest = { isHubDropdownExpanded = false },
                    shape = RoundedCornerShape(14.dp),
                    containerColor = ColorWarmIvory,
                    tonalElevation = 0.dp,
                    shadowElevation = 6.dp,
                    border = BorderStroke(1.2.dp, ColorBorderWarm),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .clip(RoundedCornerShape(14.dp))
                ) {
                    userHubs.forEach { hub ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = hub.name,
                                    fontFamily = SoraFontFamily,
                                    fontWeight = if (hub.hubId == selectedHub?.hubId) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.5.sp,
                                    color = ColorDarkWarmText
                                )
                            },
                            onClick = {
                                onSelectHub(hub)
                                isHubDropdownExpanded = false
                            },
                            modifier = Modifier.testTag("children_hub_option_${hub.hubId}")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // Children List or Empty State
        if (children.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ColorWarmCream)
                    .border(BorderStroke(1.dp, ColorBorderWarm), RoundedCornerShape(16.dp))
                    .padding(vertical = 20.dp, horizontal = 16.dp)
                    .testTag("profile_no_children_state"),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.Face,
                        contentDescription = null,
                        tint = ColorWarmNeutral.copy(alpha = 0.6f),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "No children profiles yet",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = ColorDarkWarmText
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Add children to manage their medication reminders.",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.5.sp,
                        color = ColorWarmNeutral
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                children.forEach { child ->
                    ChildProfileCard(
                        child = child,
                        modifier = Modifier.testTag("child_profile_card_${child.childId}")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Create Child Profile Button
        Button(
            onClick = onCreateChildClick,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ColorWarmAmber,
                contentColor = ColorDarkWarmText
            ),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 9.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .shadow(
                    elevation = 2.dp,
                    shape = RoundedCornerShape(14.dp),
                    ambientColor = ColorAmbientShadow,
                    spotColor = ColorSpotShadow
                )
                .testTag("profile_create_child_button")
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = null,
                tint = ColorDarkWarmText,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Create Child Profile",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.5.sp,
                color = ColorDarkWarmText
            )
        }
    }
}

/**
 * Individual Child Profile Card.
 */
@Composable
private fun ChildProfileCard(
    child: ChildProfileData,
    modifier: Modifier = Modifier
) {
    val genderLabel = when (child.gender) {
        ProfileGender.MALE -> "Boy"
        ProfileGender.FEMALE -> "Girl"
        ProfileGender.PREFER_NOT_TO_SAY -> "Child"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = ColorAmbientShadow,
                spotColor = ColorSpotShadow
            )
            .clip(RoundedCornerShape(16.dp))
            .background(ColorWarmCream)
            .border(BorderStroke(1.2.dp, ColorBorderWarm), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .semantics { contentDescription = "${child.name}, $genderLabel, remind ${child.reminderResponsibleMemberName}" }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Avatar + Name + Remind Adult Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                ProfileAvatarView(
                    avatarType = child.avatarType,
                    size = 40.dp
                )

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = child.name,
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.5.sp,
                            color = ColorDarkWarmText,
                            maxLines = 1
                        )

                        // Gender tag
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(ColorTagBg)
                                .padding(horizontal = 6.dp, vertical = 1.5.dp)
                        ) {
                            Text(
                                text = genderLabel,
                                fontFamily = SoraFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.5.sp,
                                color = ColorWarmAmber
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.NotificationsActive,
                            contentDescription = null,
                            tint = ColorDustyTeal,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Remind: ${child.reminderResponsibleMemberName}",
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 11.sp,
                            color = ColorWarmNeutral
                        )
                    }
                }
            }
        }
    }
}
