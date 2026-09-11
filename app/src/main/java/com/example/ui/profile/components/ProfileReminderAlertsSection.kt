package com.example.ui.profile.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuDefaults
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.profile.model.HubUserRole
import com.example.ui.profile.model.REMINDER_MINUTE_OPTIONS
import com.example.ui.profile.model.UserHubSummary
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
private val ColorAmbientShadow = Color(0x109C876E)
private val ColorSpotShadow = Color(0x14786550)

/**
 * REMINDER ALERTS Section in Profile page.
 * Allows viewing (and editing for Creators) hub-specific reminder delays in minutes.
 */
@Composable
fun ProfileReminderAlertsSection(
    hubs: List<UserHubSummary>,
    selectedHub: UserHubSummary?,
    onSelectHub: (UserHubSummary) -> Unit,
    onUpdateReminderSettings: (hubId: String, missedDosageMinutes: Int, familyNotificationMinutes: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var isHubDropdownExpanded by remember { mutableStateOf(false) }

    // Dialog picker state for editing reminder values
    var editingSettingType by remember { mutableStateOf<ReminderSettingType?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("profile_reminder_alerts_section")
    ) {
        // Section Title: REMINDER ALERTS
        Text(
            text = "REMINDER ALERTS",
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 11.5.sp,
            letterSpacing = 1.6.sp,
            color = ColorDustyTeal,
            modifier = Modifier.testTag("profile_reminder_alerts_kicker")
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Supporting description
        Text(
            text = "Choose a family hub to view its reminder settings.",
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 12.5.sp,
            color = ColorWarmNeutral
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (hubs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ColorWarmCream)
                    .border(BorderStroke(1.dp, ColorBorderWarm), RoundedCornerShape(16.dp))
                    .padding(18.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No family hubs available for reminder settings.",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    color = ColorWarmNeutral
                )
            }
        } else {
            // Label: Hub
            Text(
                text = "Hub",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = ColorDarkWarmText,
                modifier = Modifier.padding(start = 2.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Dropdown Selector Box
            Box(modifier = Modifier.fillMaxWidth()) {
                val shape = RoundedCornerShape(14.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 1.5.dp,
                            shape = shape,
                            ambientColor = ColorAmbientShadow,
                            spotColor = ColorSpotShadow
                        )
                        .clip(shape)
                        .background(ColorWarmCream)
                        .border(BorderStroke(1.2.dp, ColorBorderWarm), shape)
                        .clickable(role = Role.DropdownList) { isHubDropdownExpanded = true }
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                        .testTag("profile_hub_dropdown_selector"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedHub?.name ?: "Select a hub",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = if (selectedHub != null) ColorDarkWarmText else ColorWarmNeutral
                    )

                    Icon(
                        imageVector = Icons.Outlined.ArrowDropDown,
                        contentDescription = "Select hub dropdown",
                        tint = ColorDustyTeal,
                        modifier = Modifier.size(24.dp)
                    )
                }

                DropdownMenu(
                    expanded = isHubDropdownExpanded,
                    onDismissRequest = { isHubDropdownExpanded = false },
                    shape = RoundedCornerShape(14.dp),
                    containerColor = ColorWarmCream,
                    border = BorderStroke(1.dp, ColorBorderWarm),
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .testTag("profile_hub_dropdown_menu")
                ) {
                    hubs.forEachIndexed { index, hub ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = hub.name,
                                        fontFamily = SoraFontFamily,
                                        fontWeight = if (selectedHub?.hubId == hub.hubId) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 13.5.sp,
                                        color = ColorDarkWarmText
                                    )
                                    Text(
                                        text = "(${hub.role.label})",
                                        fontFamily = SoraFontFamily,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 11.sp,
                                        color = ColorWarmNeutral
                                    )
                                }
                            },
                            onClick = {
                                isHubDropdownExpanded = false
                                onSelectHub(hub)
                            },
                            colors = MenuDefaults.itemColors(
                                textColor = ColorDarkWarmText
                            ),
                            modifier = Modifier.testTag("profile_hub_dropdown_item_${hub.hubId}")
                        )
                        if (index < hubs.size - 1) {
                            HorizontalDivider(
                                color = ColorBorderWarm.copy(alpha = 0.5f),
                                thickness = 0.8.dp,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Display Hub Reminder Settings Cards
            if (selectedHub != null) {
                val isCreator = selectedHub.role == HubUserRole.CREATOR

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Setting 1: Reminder time after missed dosage
                    ReminderSettingCard(
                        title = "Reminder time after missed dosage",
                        minutes = selectedHub.missedDosageReminderMinutes,
                        isCreator = isCreator,
                        onEditClick = {
                            editingSettingType = ReminderSettingType.MISSED_DOSAGE
                        },
                        modifier = Modifier.testTag("profile_reminder_missed_dosage_card")
                    )

                    // Setting 2: Reminder time to inform family members
                    ReminderSettingCard(
                        title = "Reminder time to inform family members",
                        minutes = selectedHub.familyNotificationReminderMinutes,
                        isCreator = isCreator,
                        onEditClick = {
                            editingSettingType = ReminderSettingType.FAMILY_NOTIFICATION
                        },
                        modifier = Modifier.testTag("profile_reminder_family_notification_card")
                    )
                }
            }
        }
    }

    // Modal Picker for changing minute setting
    if (editingSettingType != null && selectedHub != null) {
        val currentType = editingSettingType!!
        val currentMinutes = if (currentType == ReminderSettingType.MISSED_DOSAGE) {
            selectedHub.missedDosageReminderMinutes
        } else {
            selectedHub.familyNotificationReminderMinutes
        }

        MinuteSelectionDialog(
            title = if (currentType == ReminderSettingType.MISSED_DOSAGE) {
                "Reminder time after missed dosage"
            } else {
                "Reminder time to inform family members"
            },
            currentMinutes = currentMinutes,
            onMinuteSelected = { newMinutes ->
                if (currentType == ReminderSettingType.MISSED_DOSAGE) {
                    onUpdateReminderSettings(
                        selectedHub.hubId,
                        newMinutes,
                        selectedHub.familyNotificationReminderMinutes
                    )
                } else {
                    onUpdateReminderSettings(
                        selectedHub.hubId,
                        selectedHub.missedDosageReminderMinutes,
                        newMinutes
                    )
                }
                editingSettingType = null
            },
            onDismiss = { editingSettingType = null }
        )
    }
}

enum class ReminderSettingType {
    MISSED_DOSAGE,
    FAMILY_NOTIFICATION
}

/**
 * Compact rounded card displaying a single reminder setting in minutes.
 */
@Composable
private fun ReminderSettingCard(
    title: String,
    minutes: Int,
    isCreator: Boolean,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 1.5.dp,
                shape = shape,
                ambientColor = ColorAmbientShadow,
                spotColor = ColorSpotShadow
            )
            .clip(shape)
            .background(ColorWarmCream)
            .border(BorderStroke(1.2.dp, ColorBorderWarm), shape)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.5.sp,
                    color = ColorWarmNeutral
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Minutes display: e.g. "5 minutes" (Strictly in minutes, never clock 00:05)
                Text(
                    text = "$minutes ${if (minutes == 1) "minute" else "minutes"}",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = ColorDarkWarmText
                )

                if (!isCreator) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Set by hub creator",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 10.5.sp,
                        color = ColorWarmNeutral.copy(alpha = 0.8f)
                    )
                }
            }

            // Creator Edit Affordance (✎)
            if (isCreator) {
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(ColorWarmIvory)
                        .border(BorderStroke(1.dp, ColorBorderWarm), CircleShape)
                        .testTag("edit_reminder_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Edit $title",
                        tint = ColorDustyTeal,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Minute selection modal dialog for Creators.
 */
@Composable
private fun MinuteSelectionDialog(
    title: String,
    currentMinutes: Int,
    onMinuteSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        val shape = RoundedCornerShape(22.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .shadow(elevation = 12.dp, shape = shape)
                .clip(shape)
                .background(ColorWarmCream)
                .border(BorderStroke(1.2.dp, ColorBorderWarm), shape)
                .padding(20.dp)
                .testTag("minute_selection_dialog")
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = title,
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = ColorDarkWarmText
                )

                Text(
                    text = "Select time in minutes:",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    color = ColorWarmNeutral
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    REMINDER_MINUTE_OPTIONS.forEach { min ->
                        val isSelected = min == currentMinutes
                        val itemShape = RoundedCornerShape(12.dp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(itemShape)
                                .background(if (isSelected) ColorDustyTeal.copy(alpha = 0.15f) else ColorWarmIvory)
                                .border(
                                    BorderStroke(
                                        1.dp,
                                        if (isSelected) ColorDustyTeal else ColorBorderWarm.copy(alpha = 0.6f)
                                    ),
                                    itemShape
                                )
                                .clickable { onMinuteSelected(min) }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                .testTag("minute_option_$min"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "$min ${if (min == 1) "minute" else "minutes"}",
                                fontFamily = SoraFontFamily,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.5.sp,
                                color = if (isSelected) ColorDustyTeal else ColorDarkWarmText
                            )

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = "Selected",
                                    tint = ColorDustyTeal,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
