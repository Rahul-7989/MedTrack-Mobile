package com.example.ui.hub.dashboard.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.hub.dashboard.model.HubMember
import com.example.ui.profilesetup.components.ProfileAvatarView
import com.example.ui.theme.DarkWarmText
import com.example.ui.theme.DustyTeal
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
private val ColorMemberTagBg = Color(0xFFEFE2D3)
private val ColorCreatorTagBg = Color(0xFFE8F2F3)
private val ColorChildTagBg = Color(0xFFFDF0DC)
private val ColorAmbientShadow = Color(0x0C9C876E)
private val ColorSpotShadow = Color(0x10786550)

/**
 * MEMBERS section on the Hub Dashboard displayed directly below the medication board.
 * Shows all approved adult and dependent child members of the currently selected family hub.
 */
@Composable
fun HubMembersSection(
    members: List<HubMember>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("hub_members_section")
    ) {
        // Section Header: MEMBERS
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "MEMBERS",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 9.5.sp,
                letterSpacing = 1.4.sp,
                color = ColorDustyTeal,
                modifier = Modifier.testTag("hub_members_header_kicker")
            )

            // Member count chip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(ColorWarmCream)
                    .border(BorderStroke(1.dp, ColorBorderWarm), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "${members.size} Total",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 9.5.sp,
                    color = ColorWarmNeutral
                )
            }
        }

        Spacer(modifier = Modifier.height(7.dp))

        // Members Card Container (Compact)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 1.5.dp,
                    shape = RoundedCornerShape(13.dp),
                    ambientColor = ColorAmbientShadow,
                    spotColor = ColorSpotShadow
                )
                .clip(RoundedCornerShape(13.dp))
                .background(ColorWarmIvory)
                .border(BorderStroke(1.dp, ColorBorderWarm), RoundedCornerShape(13.dp))
                .testTag("hub_members_container")
        ) {
            if (members.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.Group,
                            contentDescription = null,
                            tint = ColorWarmNeutral.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "No approved members yet",
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.5.sp,
                            color = ColorWarmNeutral
                        )
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                    members.forEachIndexed { index, member ->
                        MemberRowItem(
                            member = member,
                            modifier = Modifier.testTag("hub_member_row_${member.id}")
                        )

                        if (index < members.size - 1) {
                            HorizontalDivider(
                                color = ColorBorderWarm.copy(alpha = 0.55f),
                                thickness = 0.8.dp,
                                modifier = Modifier.padding(horizontal = 10.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Compact member item row displaying avatar, name, child reminder info, and role tag.
 */
@Composable
private fun MemberRowItem(
    member: HubMember,
    modifier: Modifier = Modifier
) {
    val roleLabel = when {
        member.isCreator -> "Creator"
        member.isChild -> "Child"
        else -> "Member"
    }

    val tagBgColor = when {
        member.isCreator -> ColorCreatorTagBg
        member.isChild -> ColorChildTagBg
        else -> ColorMemberTagBg
    }

    val tagTextColor = when {
        member.isCreator -> ColorDustyTeal
        member.isChild -> ColorWarmAmber
        else -> ColorWarmNeutral
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 11.dp, vertical = 6.dp)
            .semantics { contentDescription = "${member.name}, role $roleLabel" },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: Small Avatar + Name & Subtitle
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f, fill = false)
        ) {
            ProfileAvatarView(
                avatarType = member.avatarType,
                size = 28.dp
            )

            Column {
                Text(
                    text = member.name,
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = ColorDarkWarmText,
                    maxLines = 1
                )

                if (member.isChild && !member.reminderResponsibleMemberName.isNullOrBlank()) {
                    Text(
                        text = "Remind: ${member.reminderResponsibleMemberName}",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 9.sp,
                        color = ColorWarmNeutral
                    )
                }
            }
        }

        // Right: Role Tag
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(5.dp))
                .background(tagBgColor)
                .padding(horizontal = 6.dp, vertical = 2.dp)
                .testTag("hub_member_role_tag_${member.id}")
        ) {
            Text(
                text = roleLabel,
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 9.5.sp,
                color = tagTextColor
            )
        }
    }
}
