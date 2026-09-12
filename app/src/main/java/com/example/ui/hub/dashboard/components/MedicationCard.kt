package com.example.ui.hub.dashboard.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.hub.dashboard.model.MedicationItem
import com.example.ui.profilesetup.components.ProfileAvatarView
import com.example.ui.theme.SoraFontFamily

// Palette Tokens
private val ColorWarmIvory = Color(0xFFFAF4EC)
private val ColorDustyTeal = Color(0xFF72B5BA)
private val ColorPaleTealBg = Color(0xFFF2F8F8)
private val ColorDarkWarmText = Color(0xFF514A44)
private val ColorTextMuted = Color(0xFF665F58)
private val ColorBorderWarm = Color(0xFFE4D5C2)
private val ColorBorderActive = Color(0xFF72B5BA)
private val ColorDeleteRed = Color(0xFFC85A54)
private val ColorAmbientShadow = Color(0x109C876E)
private val ColorSpotShadow = Color(0x16786550)

/**
 * Individual Medication Card displayed on the shared Family Medication Board.
 */
@Composable
fun MedicationCard(
    medication: MedicationItem,
    isCreator: Boolean,
    onToggleTaken: () -> Unit,
    onCardClick: () -> Unit = {},
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isMenuOpen by remember { mutableStateOf(false) }

    val takenButtonInteractionSource = remember { MutableInteractionSource() }
    val isTakenPressed by takenButtonInteractionSource.collectIsPressedAsState()

    val cardBorderColor by animateColorAsState(
        targetValue = if (medication.isTakenToday) ColorDustyTeal.copy(alpha = 0.55f) else ColorBorderWarm,
        label = "CardBorderColor"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (medication.isTakenToday) 1.5.dp else 2.5.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = ColorAmbientShadow,
                spotColor = ColorSpotShadow
            )
            .clip(RoundedCornerShape(16.dp))
            .background(if (medication.isTakenToday) ColorPaleTealBg.copy(alpha = 0.6f) else ColorWarmIvory)
            .border(BorderStroke(1.2.dp, cardBorderColor), RoundedCornerShape(16.dp))
            .clickable(
                role = Role.Button,
                onClick = onCardClick
            )
            .padding(12.dp)
            .testTag("medication_card_${medication.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top Section: Medicine Thumbnail/Icon + Name/Dosage + 3-Dot Menu (if creator)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Optional uploaded medicine image or subtle icon
                    if (!medication.imageUri.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(medication.imageUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Photo of ${medication.name}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .border(BorderStroke(1.dp, ColorBorderWarm), RoundedCornerShape(9.dp))
                                .testTag("medication_image_${medication.id}")
                        )
                    } else {
                        // Small generic medication pill icon
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(ColorDustyTeal.copy(alpha = 0.12f))
                                .border(BorderStroke(1.dp, ColorDustyTeal.copy(alpha = 0.25f)), RoundedCornerShape(9.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Medication,
                                contentDescription = null,
                                tint = ColorDustyTeal,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Medicine Name and Dosage (scaled down)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = medication.name,
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            lineHeight = 17.sp,
                            color = ColorDarkWarmText,
                            modifier = Modifier.testTag("medication_name_${medication.id}")
                        )

                        Spacer(modifier = Modifier.height(1.dp))

                        Text(
                            text = medication.dosage,
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = ColorTextMuted
                        )
                    }
                }

                // Three-dot options menu strictly visible ONLY for the creator
                if (isCreator) {
                    Box {
                        IconButton(
                            onClick = { isMenuOpen = true },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("medication_menu_button_${medication.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.MoreVert,
                                contentDescription = "Medication options",
                                tint = ColorTextMuted,
                                modifier = Modifier.size(17.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = isMenuOpen,
                            onDismissRequest = { isMenuOpen = false },
                            shape = RoundedCornerShape(14.dp),
                            containerColor = ColorWarmIvory,
                            tonalElevation = 0.dp,
                            shadowElevation = 4.dp,
                            border = BorderStroke(1.2.dp, ColorBorderWarm),
                            modifier = Modifier.clip(RoundedCornerShape(14.dp))
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Edit,
                                            contentDescription = null,
                                            tint = ColorDarkWarmText,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Text(
                                            text = "Edit medication",
                                            fontFamily = SoraFontFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.5.sp,
                                            color = ColorDarkWarmText
                                        )
                                    }
                                },
                                onClick = {
                                    isMenuOpen = false
                                    onEditClick()
                                },
                                modifier = Modifier.testTag("edit_medication_menu_item")
                            )

                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.DeleteOutline,
                                            contentDescription = null,
                                            tint = ColorDeleteRed,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Text(
                                            text = "Delete medication",
                                            fontFamily = SoraFontFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.5.sp,
                                            color = ColorDeleteRed
                                        )
                                    }
                                },
                                onClick = {
                                    isMenuOpen = false
                                    onDeleteClick()
                                },
                                modifier = Modifier.testTag("delete_medication_menu_item")
                            )
                        }
                    }
                }
            }

            // Optional Notes
            if (!medication.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = medication.notes,
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 11.sp,
                    color = ColorTextMuted.copy(alpha = 0.9f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(7.dp))
                        .background(ColorBorderWarm.copy(alpha = 0.25f))
                        .padding(horizontal = 7.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(9.dp))

            // Bottom Section: Recipient + Reminder Time on left, Mark As Taken on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left details: Recipient chip & Reminder Time
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    // Recipient: Profile icon + For [Name]
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        ProfileAvatarView(
                            avatarType = medication.recipientAvatarType,
                            size = 16.dp
                        )
                        Text(
                            text = "For ${medication.recipientName}",
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.5.sp,
                            color = ColorDarkWarmText
                        )
                    }

                    // Reminder Time
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AccessTime,
                            contentDescription = null,
                            tint = ColorTextMuted,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = medication.reminderTime,
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 10.sp,
                            color = ColorTextMuted
                        )
                    }
                }

                // Right: Mark as taken action / Taken state (compact status/action button)
                Box(
                    modifier = Modifier
                        .scale(if (isTakenPressed) 0.96f else 1.0f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (medication.isTakenToday) ColorPaleTealBg else ColorDustyTeal.copy(alpha = 0.10f)
                        )
                        .border(
                            BorderStroke(
                                1.dp,
                                if (medication.isTakenToday) ColorDustyTeal.copy(alpha = 0.4f) else ColorDustyTeal.copy(alpha = 0.35f)
                            ),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable(
                            interactionSource = takenButtonInteractionSource,
                            indication = null,
                            role = Role.Button,
                            onClick = onToggleTaken
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .semantics {
                            contentDescription = if (medication.isTakenToday) "Taken, tap to undo" else "Mark as taken"
                        }
                        .testTag("mark_as_taken_button_${medication.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (medication.isTakenToday) {
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = null,
                                tint = ColorDustyTeal,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Taken",
                                fontFamily = SoraFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp,
                                color = ColorDustyTeal
                            )
                            if (!medication.takenAtTime.isNullOrBlank()) {
                                Text(
                                    text = medication.takenAtTime,
                                    fontFamily = SoraFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 9.sp,
                                    color = ColorTextMuted
                                )
                            }
                        } else {
                            Text(
                                text = "Mark as taken",
                                fontFamily = SoraFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp,
                                color = ColorDustyTeal
                            )
                        }
                    }
                }
            }
        }
    }
}
