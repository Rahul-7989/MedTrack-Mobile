package com.example.ui.hub.dashboard.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.Pending
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.hub.dashboard.model.MedicationItem
import com.example.ui.hub.dashboard.model.ReminderCycle
import com.example.ui.profilesetup.components.ProfileAvatarView
import com.example.ui.theme.SoraFontFamily

// Palette Tokens
private val ColorWarmIvory = Color(0xFFFAF4EC)
private val ColorWarmCream = Color(0xFFF3E6D5)
private val ColorDustyTeal = Color(0xFF72B5BA)
private val ColorPaleTealBg = Color(0xFFF0F7F7)
private val ColorDarkWarmText = Color(0xFF514A44)
private val ColorTextMuted = Color(0xFF665F58)
private val ColorBorderWarm = Color(0xFFE4D5C2)
private val ColorWarmAmber = Color(0xFFE5A23C)
private val ColorAmbientShadow = Color(0x189C876E)
private val ColorSpotShadow = Color(0x22786550)

/**
 * Read-only Medication Details Dialog.
 *
 * Opens when a user taps on a medication card.
 * Displays all medication information clearly without any edit or delete options.
 */
@Composable
fun MedicationDetailsDialog(
    medication: MedicationItem,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(26.dp),
                        ambientColor = ColorAmbientShadow,
                        spotColor = ColorSpotShadow
                    )
                    .clip(RoundedCornerShape(26.dp))
                    .background(ColorWarmIvory)
                    .border(BorderStroke(1.2.dp, ColorBorderWarm), RoundedCornerShape(26.dp))
                    .padding(22.dp)
                    .testTag("medication_details_dialog")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 580.dp)
                        .verticalScroll(scrollState)
                ) {
                    // Header: Tag + Close Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "MEDICATION DETAILS",
                                fontFamily = SoraFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 1.3.sp,
                                color = ColorDustyTeal
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = medication.name,
                                fontFamily = SoraFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 21.sp,
                                color = ColorDarkWarmText,
                                modifier = Modifier.testTag("medication_details_title")
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(ColorWarmCream.copy(alpha = 0.7f))
                                .border(BorderStroke(1.dp, ColorBorderWarm), CircleShape)
                                .testTag("medication_details_close_icon_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Close details",
                                tint = ColorDarkWarmText,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Optional Photo / Banner
                    if (!medication.imageUri.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(BorderStroke(1.dp, ColorBorderWarm), RoundedCornerShape(16.dp))
                                .background(ColorWarmCream)
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(medication.imageUri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Photo of ${medication.name}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .testTag("medication_details_image")
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Details Grid
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Dosage Row
                        DetailItemRow(
                            icon = Icons.Outlined.Medication,
                            label = "Dosage",
                            value = medication.dosage
                        )

                        // Recipient Row
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(ColorWarmCream.copy(alpha = 0.55f))
                                .border(BorderStroke(1.dp, ColorBorderWarm.copy(alpha = 0.8f)), RoundedCornerShape(14.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                ProfileAvatarView(
                                    avatarType = medication.recipientAvatarType,
                                    size = 32.dp
                                )
                                Column {
                                    Text(
                                        text = "Prescribed For",
                                        fontFamily = SoraFontFamily,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 11.5.sp,
                                        color = ColorTextMuted
                                    )
                                    Text(
                                        text = medication.recipientName,
                                        fontFamily = SoraFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.5.sp,
                                        color = ColorDarkWarmText
                                    )
                                }
                            }
                        }

                        // Schedule & Frequency Row
                        val cycleDescription = when (medication.reminderCycle) {
                            ReminderCycle.EVERY_24_HOURS -> "Every 24 hours (Daily)"
                            ReminderCycle.EVERY_48_HOURS -> "Every 48 hours (Every 2 days)"
                            ReminderCycle.CUSTOM -> {
                                if (medication.customDaysOfWeek.isNotEmpty()) {
                                    "On ${medication.customDaysOfWeek.joinToString(", ")}"
                                } else {
                                    "Every ${medication.customIntervalDays} days"
                                }
                            }
                        }

                        DetailItemRow(
                            icon = Icons.Outlined.AccessTime,
                            label = "Scheduled Time & Frequency",
                            value = "${medication.reminderTime} • $cycleDescription"
                        )

                        // Status Row (Taken / Scheduled)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (medication.isTakenToday) ColorPaleTealBg else ColorWarmCream.copy(alpha = 0.55f)
                                )
                                .border(
                                    BorderStroke(
                                        1.dp,
                                        if (medication.isTakenToday) ColorDustyTeal.copy(alpha = 0.6f) else ColorBorderWarm.copy(alpha = 0.8f)
                                    ),
                                    RoundedCornerShape(14.dp)
                                )
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = if (medication.isTakenToday) Icons.Outlined.CheckCircle else Icons.Outlined.Pending,
                                    contentDescription = null,
                                    tint = if (medication.isTakenToday) ColorDustyTeal else ColorWarmAmber,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        text = "Today's Status",
                                        fontFamily = SoraFontFamily,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 11.5.sp,
                                        color = ColorTextMuted
                                    )
                                    Text(
                                        text = if (medication.isTakenToday) {
                                            if (!medication.takenAtTime.isNullOrBlank()) {
                                                "Taken today at ${medication.takenAtTime}"
                                            } else {
                                                "Taken today"
                                            }
                                        } else {
                                            "Scheduled for today (Pending)"
                                        },
                                        fontFamily = SoraFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (medication.isTakenToday) ColorDustyTeal else ColorDarkWarmText
                                    )
                                }
                            }
                        }

                        // Instructions / Notes (if provided)
                        if (!medication.notes.isNullOrBlank()) {
                            DetailItemRow(
                                icon = Icons.Outlined.Description,
                                label = "Instructions & Notes",
                                value = medication.notes
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Clean Close Button (No Edit or Delete)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(ColorWarmAmber)
                            .border(BorderStroke(1.dp, Color(0xFFEDB86A)), RoundedCornerShape(16.dp))
                            .clickable(
                                role = Role.Button,
                                onClick = onDismiss
                            )
                            .testTag("medication_details_done_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Done",
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = ColorWarmIvory
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailItemRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(ColorWarmCream.copy(alpha = 0.55f))
            .border(BorderStroke(1.dp, ColorBorderWarm.copy(alpha = 0.8f)), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(ColorDustyTeal.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ColorDustyTeal,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 11.5.sp,
                    color = ColorTextMuted
                )
                Text(
                    text = value,
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = ColorDarkWarmText
                )
            }
        }
    }
}
