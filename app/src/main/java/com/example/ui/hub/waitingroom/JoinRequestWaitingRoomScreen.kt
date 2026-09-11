package com.example.ui.hub.waitingroom

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Diversity3
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.hub.components.HubBackButton
import com.example.ui.hub.dashboard.model.JoinRequestStatus
import com.example.ui.hub.data.FamilyHubRepository
import com.example.ui.hub.waitingroom.components.LeaveRequestConfirmDialog
import com.example.ui.hub.waitingroom.components.SequentialPulseDots
import com.example.ui.theme.SoraFontFamily
import kotlinx.coroutines.launch

// MedTrack Warm Palette Colors
private val ColorWarmIvory = Color(0xFFFAF4EC)
private val ColorWarmCream = Color(0xFFF3E6D5)
private val ColorDarkWarmText = Color(0xFF514A44)
private val ColorWarmNeutral = Color(0xFF665F58)
private val ColorBorderWarm = Color(0xFFE4D5C2)
private val ColorDustyTeal = Color(0xFF72B5BA)
private val ColorPaleTealBg = Color(0xFFE6F2F3)
private val ColorPaleTealBorder = Color(0xFFA9CED0)
private val ColorWarmAmber = Color(0xFFE5A23C)
private val ColorMutedTerracotta = Color(0xFFC8755D)
private val ColorPaleTerracottaBg = Color(0xFFFDF0ED)
private val ColorPaleTerracottaBorder = Color(0xFFE8BBB0)
private val ColorAmbientShadow = Color(0x149C876E)
private val ColorSpotShadow = Color(0x1E786550)

/**
 * MedTrack Join Request Waiting Room Screen.
 *
 * Dedicated modular screen displayed when a user submits a valid Hub Code and is waiting
 * for the family hub creator's approval.
 *
 * - Listens for real-time status changes:
 *   - ACCEPTED: automatically navigates into the Family Hub Dashboard.
 *   - REJECTED: transforms into the "Request not approved" state with a "Go back" button.
 * - Back button triggers a confirmation dialog ("Leave this request?").
 *   - "Stay here": remains in Waiting Room.
 *   - "Yes, take me back": cancels/deletes the pending join request in Firebase and redirects to Hub Selection.
 */
@Composable
fun JoinRequestWaitingRoomScreen(
    hubId: String,
    hubName: String,
    requestId: String,
    onNavigateToHubSelection: () -> Unit,
    onNavigateToHubDashboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var requestStatus by remember { mutableStateOf(JoinRequestStatus.PENDING) }
    var showLeaveConfirmDialog by remember { mutableStateOf(false) }

    // Real-time listener: detect Creator approval or rejection immediately
    DisposableEffect(hubId, requestId) {
        val cleanupListener = FamilyHubRepository.listenToJoinRequest(hubId, requestId) { updatedStatus ->
            requestStatus = updatedStatus
            if (updatedStatus == JoinRequestStatus.ACCEPTED) {
                coroutineScope.launch {
                    FamilyHubRepository.onJoinRequestAccepted(hubId, hubName)
                    onNavigateToHubDashboard()
                }
            }
        }
        onDispose {
            cleanupListener()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(ColorWarmIvory),
        containerColor = ColorWarmIvory
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .systemBarsPadding()
        ) {
            val isCompactHeight = maxHeight < 680.dp

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Bar: Back Button (MedTrack style)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (requestStatus == JoinRequestStatus.PENDING) {
                        HubBackButton(
                            onClick = { showLeaveConfirmDialog = true },
                            modifier = Modifier.testTag("waiting_room_back_button")
                        )
                    } else {
                        // In rejected state, back navigation is done via the primary "Go back" button
                        Spacer(modifier = Modifier.size(40.dp))
                    }
                }

                // Centered Main Content Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = requestStatus,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "WaitingRoomStateTransition"
                    ) { currentStatus ->
                        when (currentStatus) {
                            JoinRequestStatus.REJECTED -> {
                                WaitingRoomRejectedCard(
                                    hubName = hubName,
                                    onGoBack = onNavigateToHubSelection,
                                    isCompact = isCompactHeight
                                )
                            }
                            else -> {
                                WaitingRoomPendingCard(
                                    hubName = hubName,
                                    isCompact = isCompactHeight
                                )
                            }
                        }
                    }
                }
            }

            // Leave Request Confirmation Dialog
            if (showLeaveConfirmDialog) {
                LeaveRequestConfirmDialog(
                    onDismiss = { showLeaveConfirmDialog = false },
                    onConfirmLeave = {
                        showLeaveConfirmDialog = false
                        coroutineScope.launch {
                            FamilyHubRepository.cancelJoinRequest(hubId, requestId)
                            onNavigateToHubSelection()
                        }
                    }
                )
            }
        }
    }
}

/**
 * Main Waiting Room Card: Pending State
 */
@Composable
private fun WaitingRoomPendingCard(
    hubName: String,
    isCompact: Boolean,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(24.dp)

    Box(
        modifier = modifier
            .widthIn(max = 440.dp)
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = shape,
                ambientColor = ColorAmbientShadow,
                spotColor = ColorSpotShadow
            )
            .clip(shape)
            .background(ColorWarmCream)
            .border(BorderStroke(1.2.dp, ColorBorderWarm), shape)
            .padding(horizontal = 28.dp, vertical = if (isCompact) 28.dp else 36.dp)
            .testTag("waiting_room_pending_card"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Family / Hub Illustration / Icon Badge
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(ColorPaleTealBg)
                    .border(BorderStroke(1.2.dp, ColorPaleTealBorder), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Diversity3,
                    contentDescription = null,
                    tint = ColorDustyTeal,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Hub Name
            Text(
                text = hubName,
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = ColorDustyTeal,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("waiting_room_hub_name")
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Heading: "Waiting for approval"
            Text(
                text = "Waiting for approval",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = if (isCompact) 22.sp else 24.sp,
                color = ColorDarkWarmText,
                textAlign = TextAlign.Center,
                letterSpacing = (-0.4).sp,
                modifier = Modifier.testTag("waiting_room_heading")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Supporting text
            Text(
                text = "Please wait until the creator allows you to join this family hub.",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.5.sp,
                lineHeight = 21.sp,
                color = ColorWarmNeutral,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("waiting_room_supporting_text")
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Subtle sequential pulse waiting indicator
            SequentialPulseDots(
                dotColor = ColorDustyTeal,
                dotSize = 8.dp,
                spacing = 10.dp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Status Caption
            Text(
                text = "Your request has been sent.",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = ColorDarkWarmText.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("waiting_room_status_caption")
            )
        }
    }
}

/**
 * Main Waiting Room Card: Rejected State
 */
@Composable
private fun WaitingRoomRejectedCard(
    hubName: String,
    onGoBack: () -> Unit,
    isCompact: Boolean,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(24.dp)
    val buttonShape = RoundedCornerShape(14.dp)
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .widthIn(max = 440.dp)
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = shape,
                ambientColor = ColorAmbientShadow,
                spotColor = ColorSpotShadow
            )
            .clip(shape)
            .background(ColorWarmCream)
            .border(BorderStroke(1.2.dp, ColorBorderWarm), shape)
            .padding(horizontal = 28.dp, vertical = if (isCompact) 28.dp else 36.dp)
            .testTag("waiting_room_rejected_card"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Neutral / Terracotta Status Badge
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(ColorPaleTerracottaBg)
                    .border(BorderStroke(1.2.dp, ColorPaleTerracottaBorder), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = ColorMutedTerracotta,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Hub Name
            Text(
                text = hubName,
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = ColorDarkWarmText,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("waiting_room_rejected_hub_name")
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Heading: "Request not approved"
            Text(
                text = "Request not approved",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = if (isCompact) 22.sp else 24.sp,
                color = ColorDarkWarmText,
                textAlign = TextAlign.Center,
                letterSpacing = (-0.4).sp,
                modifier = Modifier.testTag("waiting_room_rejected_heading")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Supporting text
            Text(
                text = "Your request to join this family hub was not approved.",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.5.sp,
                lineHeight = 21.sp,
                color = ColorWarmNeutral,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("waiting_room_rejected_supporting_text")
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Primary "Go back" button (Immediately redirects to Hub Selection, no confirmation dialog)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clip(buttonShape)
                    .background(ColorWarmAmber)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        role = Role.Button,
                        onClick = onGoBack
                    )
                    .padding(horizontal = 24.dp, vertical = 13.dp)
                    .testTag("waiting_room_go_back_button"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Go back",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = ColorWarmIvory,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
