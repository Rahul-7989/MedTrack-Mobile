package com.example.ui.hub.dashboard.components

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.storage.FirebaseStorageService
import com.example.ui.hub.dashboard.model.HubMember
import com.example.ui.hub.dashboard.model.MedicationItem
import com.example.ui.hub.dashboard.model.ReminderCycle
import com.example.ui.profilesetup.components.ProfileAvatarView
import com.example.ui.theme.SoraFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.UUID
import java.util.Locale

// Modal Palette tokens
private val ColorWarmIvory = Color(0xFFFAF4EC)
private val ColorWarmCream = Color(0xFFF3E6D5)
private val ColorWarmAmber = Color(0xFFE5A23C)
private val ColorDustyTeal = Color(0xFF72B5BA)
private val ColorDarkWarmText = Color(0xFF514A44)
private val ColorTextMuted = Color(0xFF665F58)
private val ColorBorderWarm = Color(0xFFE4D5C2)
private val ColorBorderActive = Color(0xFF72B5BA)

/**
 * Add / Edit Medication Modal Sheet.
 *
 * Dimmed backdrop, scrollable form, camera/gallery photo upload, member recipient selector,
 * expandable notes, time selection, reminder cycle segmented control, and MedTrack three-dot loading animation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMedicationModal(
    existingMedication: MedicationItem? = null,
    approvedMembers: List<HubMember>,
    currentUserId: String,
    onSaveMedication: (
        name: String,
        dosage: String,
        recipient: HubMember,
        reminderTime: String,
        reminderHour: Int,
        reminderMinute: Int,
        reminderCycle: ReminderCycle,
        customIntervalDays: Int,
        notes: String?,
        imageUri: String?
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Form fields
    var medicineName by remember { mutableStateOf(existingMedication?.name ?: "") }
    var dosage by remember { mutableStateOf(existingMedication?.dosage ?: "1 tablet") }
    var selectedRecipient by remember {
        mutableStateOf(
            approvedMembers.find { it.id == existingMedication?.recipientId }
                ?: approvedMembers.find { it.id == currentUserId }
                ?: approvedMembers.firstOrNull()
        )
    }
    var isRecipientMenuExpanded by remember { mutableStateOf(false) }

    // Reminder time state
    var reminderHour by remember { mutableIntStateOf(existingMedication?.reminderHour ?: 9) }
    var reminderMinute by remember { mutableIntStateOf(existingMedication?.reminderMinute ?: 0) }
    var isTimePickerOpen by remember { mutableStateOf(false) }

    // Reminder cycle
    var selectedCycle by remember { mutableStateOf(existingMedication?.reminderCycle ?: ReminderCycle.EVERY_24_HOURS) }
    var customIntervalDays by remember { mutableIntStateOf(existingMedication?.customIntervalDays ?: 3) }

    // Notes
    var isNotesExpanded by remember { mutableStateOf(!existingMedication?.notes.isNullOrBlank()) }
    var notesText by remember { mutableStateOf(existingMedication?.notes ?: "") }

    // Image
    var imageUriString by remember { mutableStateOf(existingMedication?.imageUri) }
    var isPhotoSourceDialogOpen by remember { mutableStateOf(false) }

    // Loading & Animation State
    var isLoading by remember { mutableStateOf(false) }

    // Photo picker launcher (0-permission photo picker)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUriString = uri.toString()
        }
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            try {
                val tempFile = File(context.cacheDir, "med_photo_${System.currentTimeMillis()}.jpg")
                tempFile.outputStream().use { out ->
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
                }
                imageUriString = Uri.fromFile(tempFile).toString()
            } catch (_: Exception) {
                // Graceful fallback
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(null)
        }
    }

    val isFormValid = medicineName.trim().isNotBlank() && selectedRecipient != null

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ColorWarmIvory,
        scrimColor = Color(0x701F1D1B),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(ColorBorderWarm)
            )
        },
        modifier = Modifier.imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .testTag("add_edit_medication_modal")
        ) {
            // Header Row: Title & Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (existingMedication == null) "Add Medication" else "Edit Medication",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        color = ColorDarkWarmText
                    )
                    Text(
                        text = "Keep your family's schedule on track.",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.5.sp,
                        color = ColorTextMuted
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = ColorTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 1. Medicine Photo Upload Area (Optional)
            Text(
                text = "Medicine photo (optional)",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.5.sp,
                color = ColorDarkWarmText
            )
            Spacer(modifier = Modifier.height(6.dp))

            if (!imageUriString.isNullOrBlank()) {
                // Photo Preview with Remove Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(ColorWarmCream)
                        .border(BorderStroke(1.2.dp, ColorBorderWarm), RoundedCornerShape(16.dp))
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imageUriString)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Selected medicine photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Remove photo button
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xCC333333))
                            .clickable { imageUriString = null },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Remove photo",
                            tint = ColorWarmIvory,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            } else {
                // Upload trigger area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(ColorWarmCream)
                        .border(BorderStroke(1.2.dp, ColorBorderWarm), RoundedCornerShape(14.dp))
                        .clickable(role = Role.Button) { isPhotoSourceDialogOpen = true }
                        .padding(horizontal = 16.dp)
                        .testTag("add_medicine_photo_upload_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AddAPhoto,
                            contentDescription = null,
                            tint = ColorDustyTeal,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "+ Add medicine photo",
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = ColorDustyTeal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Medicine Name (Required)
            Text(
                text = "Medicine name",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.5.sp,
                color = ColorDarkWarmText
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = medicineName,
                onValueChange = { medicineName = it },
                textStyle = TextStyle(
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 15.sp,
                    color = ColorDarkWarmText
                ),
                placeholder = {
                    Text(
                        text = "Enter medicine name",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.5.sp,
                        color = ColorTextMuted.copy(alpha = 0.65f)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = ColorWarmCream,
                    unfocusedContainerColor = ColorWarmCream,
                    focusedBorderColor = ColorBorderActive,
                    unfocusedBorderColor = ColorBorderWarm,
                    focusedTextColor = ColorDarkWarmText,
                    unfocusedTextColor = ColorDarkWarmText
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("medicine_name_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. For Whom Selector (Approved Members only)
            Text(
                text = "For whom?",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.5.sp,
                color = ColorDarkWarmText
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(ColorWarmCream)
                        .border(BorderStroke(1.2.dp, ColorBorderWarm), RoundedCornerShape(14.dp))
                        .clickable(role = Role.Button) { isRecipientMenuExpanded = true }
                        .padding(horizontal = 14.dp)
                        .testTag("recipient_selector_button"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (selectedRecipient != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ProfileAvatarView(
                                avatarType = selectedRecipient!!.avatarType,
                                size = 26.dp
                            )
                            Text(
                                text = selectedRecipient!!.name,
                                fontFamily = SoraFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.5.sp,
                                color = ColorDarkWarmText
                            )
                        }
                    } else {
                        Text(
                            text = "Select family member",
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.5.sp,
                            color = ColorTextMuted
                        )
                    }

                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "Expand",
                        tint = ColorTextMuted
                    )
                }

                DropdownMenu(
                    expanded = isRecipientMenuExpanded,
                    onDismissRequest = { isRecipientMenuExpanded = false },
                    shape = RoundedCornerShape(14.dp),
                    containerColor = ColorWarmIvory,
                    tonalElevation = 0.dp,
                    shadowElevation = 4.dp,
                    border = BorderStroke(1.2.dp, ColorBorderWarm),
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .clip(RoundedCornerShape(14.dp))
                ) {
                    approvedMembers.forEach { member ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                                            fontSize = 13.5.sp,
                                            color = ColorDarkWarmText
                                        )
                                        if (member.isChild) {
                                            Text(
                                                text = "Child Dependent",
                                                fontFamily = SoraFontFamily,
                                                fontSize = 10.sp,
                                                color = Color(0xFFE5A23C)
                                            )
                                        }
                                    }
                                }
                            },
                            onClick = {
                                selectedRecipient = member
                                isRecipientMenuExpanded = false
                            },
                            modifier = Modifier.testTag("recipient_option_${member.id}")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Dosage
            Text(
                text = "Dosage",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.5.sp,
                color = ColorDarkWarmText
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = dosage,
                onValueChange = { dosage = it },
                textStyle = TextStyle(
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 15.sp,
                    color = ColorDarkWarmText
                ),
                placeholder = {
                    Text(
                        text = "e.g. 1 tablet",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.5.sp,
                        color = ColorTextMuted.copy(alpha = 0.65f)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = ColorWarmCream,
                    unfocusedContainerColor = ColorWarmCream,
                    focusedBorderColor = ColorBorderActive,
                    unfocusedBorderColor = ColorBorderWarm,
                    focusedTextColor = ColorDarkWarmText,
                    unfocusedTextColor = ColorDarkWarmText
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dosage_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Reminder Time
            val formattedSelectedTime = formatTime(reminderHour, reminderMinute)
            Text(
                text = "Reminder time",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.5.sp,
                color = ColorDarkWarmText
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(ColorWarmCream)
                    .border(BorderStroke(1.2.dp, ColorBorderWarm), RoundedCornerShape(14.dp))
                    .clickable(role = Role.Button) { isTimePickerOpen = true }
                    .padding(horizontal = 14.dp)
                    .testTag("reminder_time_picker_button"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AccessTime,
                        contentDescription = null,
                        tint = ColorDustyTeal,
                        modifier = Modifier.size(19.dp)
                    )
                    Text(
                        text = formattedSelectedTime,
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = ColorDarkWarmText
                    )
                }

                Text(
                    text = "Change",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = ColorDustyTeal
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 6. Reminder Cycle (Segmented Choices)
            Text(
                text = "Reminder cycle",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.5.sp,
                color = ColorDarkWarmText
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ColorWarmCream)
                    .border(BorderStroke(1.dp, ColorBorderWarm), RoundedCornerShape(12.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ReminderCycle.values().forEach { cycle ->
                    val isSelected = selectedCycle == cycle
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (isSelected) ColorDustyTeal else Color.Transparent)
                            .clickable(role = Role.Button) { selectedCycle = cycle }
                            .testTag("cycle_option_${cycle.name}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cycle.label,
                            fontFamily = SoraFontFamily,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = if (isSelected) ColorWarmIvory else ColorDarkWarmText
                        )
                    }
                }
            }

            // Custom interval revealed ONLY when "Custom" is selected
            AnimatedVisibility(
                visible = selectedCycle == ReminderCycle.CUSTOM,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Text(
                        text = "Repeat every $customIntervalDays days",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        color = ColorTextMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(2, 3, 5, 7).forEach { days ->
                            val isChosen = customIntervalDays == days
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isChosen) ColorDustyTeal.copy(alpha = 0.2f) else ColorWarmCream)
                                    .border(
                                        BorderStroke(1.dp, if (isChosen) ColorDustyTeal else ColorBorderWarm),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { customIntervalDays = days }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$days days",
                                    fontFamily = SoraFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = if (isChosen) ColorDustyTeal else ColorDarkWarmText
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 7. Notes (Optional & Compact Collapsible)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(role = Role.Button) { isNotesExpanded = !isNotesExpanded }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notes,
                        contentDescription = null,
                        tint = ColorTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Notes (optional)",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.5.sp,
                        color = ColorDarkWarmText
                    )
                }

                Icon(
                    imageVector = if (isNotesExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    contentDescription = if (isNotesExpanded) "Collapse" else "Expand",
                    tint = ColorTextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }

            AnimatedVisibility(
                visible = isNotesExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 6.dp)) {
                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        textStyle = TextStyle(
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                            color = ColorDarkWarmText
                        ),
                        placeholder = {
                            Text(
                                text = "e.g. Take with food and full glass of water",
                                fontFamily = SoraFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 13.sp,
                                color = ColorTextMuted.copy(alpha = 0.65f)
                            )
                        },
                        minLines = 2,
                        maxLines = 4,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = ColorWarmCream,
                            unfocusedContainerColor = ColorWarmCream,
                            focusedBorderColor = ColorBorderActive,
                            unfocusedBorderColor = ColorBorderWarm,
                            focusedTextColor = ColorDarkWarmText,
                            unfocusedTextColor = ColorDarkWarmText
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("medication_notes_input")
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 8. Bottom Action: Create / Save Medication with 3-Dot Animation (compact primary CTA)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(
                        if (isFormValid && !isLoading) ColorWarmAmber else ColorWarmAmber.copy(alpha = 0.45f)
                    )
                    .clickable(
                        enabled = isFormValid && !isLoading,
                        role = Role.Button,
                        onClick = {
                            if (isFormValid && selectedRecipient != null) {
                                isLoading = true
                                coroutineScope.launch {
                                    val finalImageUri = if (!imageUriString.isNullOrBlank() && 
                                        (imageUriString!!.startsWith("content://") || imageUriString!!.startsWith("file://"))) {
                                        val medId = existingMedication?.id ?: UUID.randomUUID().toString()
                                        val hubId = existingMedication?.hubId ?: "hub_shared"
                                        FirebaseStorageService.uploadMedicationImage(
                                             context = context,
                                             sourceUri = Uri.parse(imageUriString!!),
                                             hubId = hubId,
                                             medicationId = medId
                                        ) ?: imageUriString
                                    } else {
                                        imageUriString
                                    }

                                    delay(400) // smooth creation flow
                                    onSaveMedication(
                                        medicineName.trim(),
                                        dosage.trim(),
                                        selectedRecipient!!,
                                        formattedSelectedTime,
                                        reminderHour,
                                        reminderMinute,
                                        selectedCycle,
                                        customIntervalDays,
                                        notesText.trim(),
                                        finalImageUri
                                    )
                                    isLoading = false
                                }
                            }
                        }
                    )
                    .testTag("create_medication_submit_button"),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    MedTrackThreeDotProgress()
                } else {
                    Text(
                        text = if (existingMedication == null) "Create Medication" else "Save Changes",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = ColorWarmIvory
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Photo Source Dialog (Camera vs Gallery)
    if (isPhotoSourceDialogOpen) {
        Dialog(onDismissRequest = { isPhotoSourceDialogOpen = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(ColorWarmIvory)
                    .border(BorderStroke(1.2.dp, ColorBorderWarm), RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Add Medicine Photo",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = ColorDarkWarmText
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Camera Choice
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(72.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(ColorWarmCream)
                                .border(BorderStroke(1.dp, ColorBorderWarm), RoundedCornerShape(14.dp))
                                .clickable {
                                    isPhotoSourceDialogOpen = false
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Icon(
                                    imageVector = Icons.Outlined.CameraAlt,
                                    contentDescription = null,
                                    tint = ColorDustyTeal,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Camera",
                                    fontFamily = SoraFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = ColorDarkWarmText
                                )
                            }
                        }

                        // Gallery Choice
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(72.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(ColorWarmCream)
                                .border(BorderStroke(1.dp, ColorBorderWarm), RoundedCornerShape(14.dp))
                                .clickable {
                                    isPhotoSourceDialogOpen = false
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Icon(
                                    imageVector = Icons.Outlined.Image,
                                    contentDescription = null,
                                    tint = ColorDustyTeal,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Gallery",
                                    fontFamily = SoraFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = ColorDarkWarmText
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Time Selection Dialog with Clock Wheel Picker
    if (isTimePickerOpen) {
        ClockTimePickerDialog(
            initialHour = reminderHour,
            initialMinute = reminderMinute,
            onConfirmTime = { h, m ->
                reminderHour = h
                reminderMinute = m
                isTimePickerOpen = false
            },
            onDismiss = { isTimePickerOpen = false }
        )
    }
}

/**
 * Animated three-dot loading animation:
 * ● · ·
 * · ● ·
 * · · ●
 * ● · ·
 */
@Composable
private fun MedTrackThreeDotProgress() {
    var activeDot by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(240)
            activeDot = (activeDot + 1) % 3
        }
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val isActive = index == activeDot
            Box(
                modifier = Modifier
                    .size(if (isActive) 7.5.dp else 5.dp)
                    .clip(CircleShape)
                    .background(ColorWarmIvory.copy(alpha = if (isActive) 1.0f else 0.45f))
            )
        }
    }
}

private fun formatTime(hour: Int, minute: Int): String {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
    }
    val formatter = SimpleDateFormat("h:mm a", Locale.getDefault())
    return formatter.format(calendar.time)
}
