package com.example.ui.hub.dashboard.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.GroupAdd
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.hub.dashboard.model.HubJoinRequest
import com.example.ui.profilesetup.components.ProfileAvatarView
import com.example.ui.theme.SoraFontFamily

// Banner Palette
private val ColorWarmCream = Color(0xFFF3E6D5)
private val ColorWarmIvory = Color(0xFFFAF4EC)
private val ColorDustyTeal = Color(0xFF72B5BA)
private val ColorDarkWarmText = Color(0xFF514A44)
private val ColorWarmNeutral = Color(0xFF665F58)
private val ColorBorderWarm = Color(0xFFE4D5C2)
private val ColorWarmAmber = Color(0xFFE5A23C)
private val ColorMutedTerracotta = Color(0xFFC8755D)
private val ColorAmbientShadow = Color(0x109C876E)
private val ColorSpotShadow = Color(0x16786550)

/**
 * Creator-only Pending Join Requests section.
 *
 * Placed between Date/Time bar and Medications board.
 * Hidden when there are 0 requests.
 * Displays "FAMILY REQUESTS · N" header.
 * Each request is initially compact and expands smoothly on tap to reveal Reject and Accept actions.
 */
@Composable
fun HubJoinRequestsBanner(
    requests: List<HubJoinRequest>,
    onAccept: (requestId: String) -> Unit,
    onReject: (requestId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (requests.isEmpty()) return

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = ColorAmbientShadow,
                spotColor = ColorSpotShadow
            )
            .clip(RoundedCornerShape(18.dp))
            .background(ColorWarmCream)
            .border(BorderStroke(1.2.dp, ColorBorderWarm), RoundedCornerShape(18.dp))
            .padding(14.dp)
            .testTag("hub_dashboard_join_requests_banner")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: "FAMILY REQUESTS · N"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hub_dashboard_join_requests_header"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(ColorDustyTeal.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.GroupAdd,
                        contentDescription = null,
                        tint = ColorDustyTeal,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    text = "FAMILY REQUESTS · ${requests.size}",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.2.sp,
                    color = ColorDarkWarmText,
                    modifier = Modifier.testTag("hub_dashboard_join_requests_title")
                )
            }

            // List of Request Cards
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                requests.forEach { request ->
                    JoinRequestItem(
                        request = request,
                        onAccept = { onAccept(request.id) },
                        onReject = { onReject(request.id) }
                    )
                }
            }
        }
    }
}

/**
 * Individual Join Request card: initially compact, expands on tap to reveal Accept/Reject actions.
 */
@Composable
private fun JoinRequestItem(
    request: HubJoinRequest,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(14.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(ColorWarmIvory)
            .border(BorderStroke(1.dp, ColorBorderWarm.copy(alpha = 0.8f)), shape)
            .clickable(role = Role.Button) { isExpanded = !isExpanded }
            .padding(12.dp)
            .testTag("join_request_item_${request.id}")
    ) {
        // Compact Row: Profile Avatar, Name, "Wants to join your family hub"
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ProfileAvatarView(
                    avatarType = request.avatarType,
                    size = 38.dp,
                    modifier = Modifier.testTag("join_request_avatar_${request.id}")
                )

                Column {
                    Text(
                        text = request.userName,
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = ColorDarkWarmText,
                        modifier = Modifier.testTag("join_request_name_${request.id}")
                    )
                    Text(
                        text = "Wants to join your family hub",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = ColorWarmNeutral,
                        modifier = Modifier.testTag("join_request_caption_${request.id}")
                    )
                }
            }

            Icon(
                imageVector = if (isExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Collapse actions" else "Expand actions",
                tint = ColorWarmNeutral,
                modifier = Modifier.size(20.dp)
            )
        }

        // Smooth Expand/Collapse Actions: Reject & Accept
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reject: Secondary / light treatment with Muted Terracotta
                    val rejectShape = RoundedCornerShape(10.dp)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .clip(rejectShape)
                            .background(ColorMutedTerracotta.copy(alpha = 0.12f))
                            .border(BorderStroke(1.dp, ColorMutedTerracotta.copy(alpha = 0.35f)), rejectShape)
                            .clickable(role = Role.Button, onClick = onReject)
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .testTag("reject_request_button_${request.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Reject",
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = ColorMutedTerracotta
                        )
                    }

                    // Accept: Primary Warm Amber treatment
                    val acceptShape = RoundedCornerShape(10.dp)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .clip(acceptShape)
                            .background(ColorWarmAmber)
                            .clickable(role = Role.Button, onClick = onAccept)
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .testTag("accept_request_button_${request.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Accept",
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = ColorWarmIvory
                        )
                    }
                }
            }
        }
    }
}
