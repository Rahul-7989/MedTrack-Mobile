package com.example.ui.hub.activity

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.hub.activity.data.HubActivityRepository
import com.example.ui.hub.activity.model.HubActivityEvent
import com.example.ui.profilesetup.components.ProfileAvatarView
import com.example.ui.profilesetup.model.ProfileAvatarType
import com.example.ui.profilesetup.model.toAvatarType
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val ColorWarmIvory = WarmIvory
private val ColorWarmCream = WarmCream
private val ColorDustyTeal = DustyTeal
private val ColorPaleTeal = PaleTeal
private val ColorWarmAmber = WarmAmber
private val ColorBurntApricot = BurntApricot
private val ColorDarkWarmText = DarkWarmText
private val ColorTextMuted = WarmNeutral
private val ColorBorderWarm = Color(0xFFE4D5C2)
private val ColorMutedTerracotta = Color(0xFFC8755D)

private val MONTH_CODES = arrayOf(
    "JAN", "FEB", "MAR", "APR", "MAY", "JUN",
    "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationHistoryScreen(
    hubId: String,
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val events by HubActivityRepository.events.collectAsState()

    // Initialize listeners for the hub
    DisposableEffect(hubId) {
        HubActivityRepository.attachListener(hubId)
        onDispose {
            HubActivityRepository.detachListener()
        }
    }

    // Today calendar instance
    val todayCalendar = remember {
        com.example.util.CurrentTimeService.getCurrentLocalDate()
    }

    var selectedCalendar by remember {
        mutableStateOf(
            com.example.util.CurrentTimeService.getCurrentLocalDate()
        )
    }

    var isYearPickerOpen by remember { mutableStateOf(false) }
    var pickerYear by remember { mutableIntStateOf(selectedCalendar.get(Calendar.YEAR)) }
    var pickerMonth by remember { mutableIntStateOf(selectedCalendar.get(Calendar.MONTH)) }
    var pickerDay by remember { mutableIntStateOf(selectedCalendar.get(Calendar.DAY_OF_MONTH)) }

    val selectedYear = selectedCalendar.get(Calendar.YEAR)
    val selectedDayOfMonth = selectedCalendar.get(Calendar.DAY_OF_MONTH)
    val selectedMonthIdx = selectedCalendar.get(Calendar.MONTH)
    val selectedMonthCode = MONTH_CODES.getOrElse(selectedMonthIdx) { "SEP" }

    fun shiftDays(delta: Int) {
        val newCal = (selectedCalendar.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, delta)
        }
        selectedCalendar = newCal
    }

    val prevCal = remember(selectedCalendar) {
        (selectedCalendar.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
    }
    val nextCal = remember(selectedCalendar) {
        (selectedCalendar.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
    }

    val prevDayNum = prevCal.get(Calendar.DAY_OF_MONTH)
    val prevMonthCode = MONTH_CODES.getOrElse(prevCal.get(Calendar.MONTH)) { "JAN" }
    val prevIsToday = prevCal.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR) &&
            prevCal.get(Calendar.DAY_OF_YEAR) == todayCalendar.get(Calendar.DAY_OF_YEAR)

    val currDayNum = selectedCalendar.get(Calendar.DAY_OF_MONTH)
    val currMonthCode = MONTH_CODES.getOrElse(selectedCalendar.get(Calendar.MONTH)) { "SEP" }
    val currIsToday = selectedCalendar.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR) &&
            selectedCalendar.get(Calendar.DAY_OF_YEAR) == todayCalendar.get(Calendar.DAY_OF_YEAR)

    val nextDayNum = nextCal.get(Calendar.DAY_OF_MONTH)
    val nextMonthCode = MONTH_CODES.getOrElse(nextCal.get(Calendar.MONTH)) { "OCT" }
    val nextIsToday = nextCal.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR) &&
            nextCal.get(Calendar.DAY_OF_YEAR) == todayCalendar.get(Calendar.DAY_OF_YEAR)

    val dateActivityEvents = remember(events, selectedCalendar, hubId) {
        HubActivityRepository.getEventsForDate(hubId, selectedCalendar)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorWarmIvory)
            .testTag("medication_history_screen"),
        containerColor = ColorWarmIvory,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "MEDICATION HISTORY",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = ColorDarkWarmText,
                        letterSpacing = 1.2.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(ColorWarmCream)
                            .testTag("medication_history_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = ColorDarkWarmText,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ColorWarmIvory)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // 3. Interactive Year Display
            Text(
                text = selectedYear.toString(),
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = ColorTextMuted,
                letterSpacing = 1.5.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        pickerYear = selectedYear
                        pickerMonth = selectedMonthIdx
                        pickerDay = selectedDayOfMonth
                        isYearPickerOpen = true
                    }
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .testTag("medication_history_year_display")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 4, 5, 6, 7, 8, 9. Exactly THREE date items with < and > controls and horizontal swipe
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures { _, dragAmount ->
                            if (dragAmount > 30) {
                                shiftDays(-1)
                            } else if (dragAmount < -30) {
                                shiftDays(1)
                            }
                        }
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { shiftDays(-1) },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(ColorWarmCream)
                        .border(BorderStroke(1.dp, ColorBorderWarm), CircleShape)
                        .testTag("medication_history_prev_day_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChevronLeft,
                        contentDescription = "Previous Day",
                        tint = ColorDarkWarmText,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous day card
                    DateBoxCard(
                        dayNum = prevDayNum,
                        monthCode = prevMonthCode,
                        isSelected = false,
                        isToday = prevIsToday,
                        onClick = { shiftDays(-1) },
                        testTag = "date_item_prev"
                    )
                    // Selected day card (Center)
                    DateBoxCard(
                        dayNum = currDayNum,
                        monthCode = currMonthCode,
                        isSelected = true,
                        isToday = currIsToday,
                        onClick = {},
                        testTag = "date_item_selected"
                    )
                    // Next day card
                    DateBoxCard(
                        dayNum = nextDayNum,
                        monthCode = nextMonthCode,
                        isSelected = false,
                        isToday = nextIsToday,
                        onClick = { shiftDays(1) },
                        testTag = "date_item_next"
                    )
                }

                IconButton(
                    onClick = { shiftDays(1) },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(ColorWarmCream)
                        .border(BorderStroke(1.dp, ColorBorderWarm), CircleShape)
                        .testTag("medication_history_next_day_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = "Next Day",
                        tint = ColorDarkWarmText,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 11. Activity Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$selectedDayOfMonth $selectedMonthCode ACTIVITY",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp,
                    color = ColorDarkWarmText,
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.testTag("activity_section_heading")
                )
                Text(
                    text = "${dateActivityEvents.size} events",
                    fontFamily = SoraFontFamily,
                    fontSize = 11.sp,
                    color = ColorTextMuted
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 12, 15, 20. Activity Timeline / Compact Elegant Empty State with subtle watermark structure
            if (dateActivityEvents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(ColorPaleTeal.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.MedicalServices,
                                contentDescription = null,
                                tint = ColorDustyTeal,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No activity on this date",
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = ColorDarkWarmText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Your family's activity will appear here.",
                            fontFamily = SoraFontFamily,
                            fontSize = 12.sp,
                            color = ColorTextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("medication_history_timeline_list"),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp)
                ) {
                    itemsIndexed(dateActivityEvents, key = { _, event -> event.eventId }) { _, event ->
                        ActivityTimelineItem(event = event)
                    }
                }
            }
        }
    }

    if (isYearPickerOpen) {
        AlertDialog(
            onDismissRequest = { isYearPickerOpen = false },
            title = {
                Text("Select Date", fontFamily = SoraFontFamily, fontWeight = FontWeight.Bold, color = ColorDarkWarmText)
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Year", fontFamily = SoraFontFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ColorTextMuted)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(2024, 2025, 2026, 2027, 2028).forEach { yr ->
                            val isSel = pickerYear == yr
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) ColorDustyTeal else ColorWarmCream)
                                    .clickable { pickerYear = yr }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = yr.toString(),
                                    fontFamily = SoraFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isSel) ColorWarmIvory else ColorDarkWarmText
                                )
                            }
                        }
                    }

                    Text("Month", fontFamily = SoraFontFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ColorTextMuted)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        val months = listOf("JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC")
                        for (row in months.chunked(4)) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                row.forEach { mCode ->
                                    val mIdx = months.indexOf(mCode)
                                    val isSel = pickerMonth == mIdx
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSel) ColorDustyTeal else ColorWarmCream)
                                            .clickable { pickerMonth = mIdx }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = mCode,
                                            fontFamily = SoraFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = if (isSel) ColorWarmIvory else ColorDarkWarmText
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Text("Day of Month", fontFamily = SoraFontFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ColorTextMuted)
                    val calTemp = Calendar.getInstance().apply {
                        set(Calendar.YEAR, pickerYear)
                        set(Calendar.MONTH, pickerMonth)
                    }
                    val maxDays = calTemp.getActualMaximum(Calendar.DAY_OF_MONTH)
                    val daysListInt = (1..maxDays).toList()
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth().height(40.dp)
                    ) {
                        items(daysListInt.size) { idx ->
                            val d = daysListInt[idx]
                            val isSel = pickerDay == d
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isSel) ColorDustyTeal else ColorWarmCream)
                                    .clickable { pickerDay = d },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = d.toString(),
                                    fontFamily = SoraFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (isSel) ColorWarmIvory else ColorDarkWarmText
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    isYearPickerOpen = false
                    val newCal = Calendar.getInstance().apply {
                        set(Calendar.YEAR, pickerYear)
                        set(Calendar.MONTH, pickerMonth)
                        set(Calendar.DAY_OF_MONTH, minOf(pickerDay, getActualMaximum(Calendar.DAY_OF_MONTH)))
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    selectedCalendar = newCal
                }) {
                    Text("Confirm", fontFamily = SoraFontFamily, fontWeight = FontWeight.Bold, color = ColorDustyTeal)
                }
            },
            dismissButton = {
                TextButton(onClick = { isYearPickerOpen = false }) {
                    Text("Cancel", fontFamily = SoraFontFamily, color = ColorTextMuted)
                }
            },
            containerColor = ColorWarmIvory
        )
    }
}

@Composable
private fun DateBoxCard(
    dayNum: Int,
    monthCode: String,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val width = if (isSelected) 68.dp else 56.dp
    val height = if (isSelected) 68.dp else 56.dp
    val containerBg = if (isSelected) ColorDustyTeal else ColorWarmCream
    val textColor = if (isSelected) ColorWarmIvory else ColorDarkWarmText

    Box(
        modifier = Modifier
            .size(width, height)
            .shadow(if (isSelected) 4.dp else 0.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(containerBg)
            .border(
                BorderStroke(
                    if (isSelected) 1.5.dp else 1.dp,
                    if (isSelected) ColorDustyTeal else ColorBorderWarm
                ),
                RoundedCornerShape(16.dp)
            )
            .clickable(role = Role.Button, onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = dayNum.toString(),
                fontFamily = SoraFontFamily,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                fontSize = if (isSelected) 15.sp else 13.sp,
                color = textColor
            )
            Text(
                text = monthCode,
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = if (isSelected) 10.5.sp else 9.5.sp,
                color = if (isSelected) ColorWarmIvory.copy(alpha = 0.9f) else ColorTextMuted
            )
            if (isToday && !isSelected) {
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(ColorWarmAmber)
                )
            }
        }
    }
}

@Composable
fun ActivityTimelineItem(event: HubActivityEvent) {
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val formattedTime = remember(event.timestamp) { timeFormat.format(event.timestamp) }

    val (actionLabel, accentColor, iconVector) = getEventVisuals(event.eventType)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ColorWarmCream)
            .border(BorderStroke(1.1.dp, ColorBorderWarm), RoundedCornerShape(16.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Timeline accent bar + icon
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(36.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.18f))
                    .border(BorderStroke(1.dp, accentColor), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            // 1. Exact Time & Actor
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ProfileAvatarView(
                        avatarType = try {
                            ProfileAvatarType.valueOf(event.actorAvatarType)
                        } catch (_: Exception) { ProfileAvatarType.MALE },
                        size = 20.dp
                    )
                    Text(
                        text = event.actorName,
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = ColorDarkWarmText
                    )
                }
                Text(
                    text = formattedTime,
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = ColorTextMuted
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 2. Action
            Text(
                text = actionLabel,
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = accentColor
            )

            // 3. Relevant object / metadata details
            val metadataLines = buildMetadataDescription(event)
            if (metadataLines.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = metadataLines,
                    fontFamily = SoraFontFamily,
                    fontSize = 12.sp,
                    color = ColorDarkWarmText
                )
            }
        }
    }
}

fun getEventVisuals(eventType: String): Triple<String, Color, androidx.compose.ui.graphics.vector.ImageVector> {
    return when (eventType) {
        "MEDICATION_CREATED" -> Triple("Created medication", ColorDustyTeal, Icons.Outlined.MedicalServices)
        "MEDICATION_EDITED" -> Triple("Edited medication", ColorDustyTeal, Icons.Outlined.Edit)
        "MEDICATION_DELETED" -> Triple("Deleted medication", ColorMutedTerracotta, Icons.Outlined.Delete)
        "MEDICATION_MARKED_TAKEN" -> Triple("Medication marked as taken", ColorDustyTeal, Icons.Outlined.CheckCircle)
        "MEMBER_JOINED" -> Triple("Joined the family hub", ColorWarmAmber, Icons.Outlined.PersonAdd)
        "JOIN_REQUEST_ACCEPTED" -> Triple("Approved join request", ColorWarmAmber, Icons.Outlined.PersonAdd)
        "JOIN_REQUEST_REJECTED" -> Triple("Rejected join request", ColorMutedTerracotta, Icons.Outlined.PersonOutline)
        "MEMBER_REMOVED" -> Triple("Removed member from hub", ColorMutedTerracotta, Icons.Outlined.Delete)
        "CHILD_CREATED" -> Triple("Created child profile", ColorBurntApricot, Icons.Outlined.PersonAdd)
        "CHILD_EDITED" -> Triple("Updated child profile", ColorBurntApricot, Icons.Outlined.Edit)
        "CHILD_DELETED", "CHILD_PROFILE_DELETED" -> Triple("Deleted child profile", ColorMutedTerracotta, Icons.Outlined.Delete)
        "HUB_NAME_CHANGED" -> Triple("Updated Hub Name", ColorWarmAmber, Icons.Outlined.Settings)
        "REMINDER_SETTINGS_CHANGED" -> Triple("Updated reminder settings", ColorWarmAmber, Icons.Outlined.Settings)
        else -> Triple("Hub activity", ColorDustyTeal, Icons.Outlined.MedicalServices)
    }
}

fun buildMetadataDescription(event: HubActivityEvent): String {
    val meta = event.metadata
    val sb = StringBuilder()
    when (event.eventType) {
        "MEDICATION_CREATED", "MEDICATION_EDITED", "MEDICATION_DELETED" -> {
            val medName = meta["medicineName"] ?: meta["name"] ?: event.targetId
            val dosage = meta["dosage"] ?: ""
            val recipient = meta["recipientName"] ?: ""
            if (medName.isNotBlank()) sb.append(medName)
            if (dosage.isNotBlank()) sb.append("\n$dosage")
            if (recipient.isNotBlank()) sb.append(" · For $recipient")
        }
        "MEDICATION_MARKED_TAKEN" -> {
            val medName = meta["medicineName"] ?: event.targetId
            val dosage = meta["dosage"] ?: ""
            val scheduled = meta["scheduledTime"] ?: ""
            val taken = meta["takenTime"] ?: ""
            if (medName.isNotBlank()) sb.append(medName)
            if (dosage.isNotBlank()) sb.append("\n$dosage")
            if (scheduled.isNotBlank() || taken.isNotBlank()) {
                sb.append("\nScheduled $scheduled · Taken $taken")
            }
        }
        "MEMBER_JOINED", "JOIN_REQUEST_ACCEPTED" -> {
            val name = meta["memberName"] ?: meta["name"] ?: ""
            if (name.isNotBlank()) sb.append(name)
        }
        "MEMBER_REMOVED" -> {
            val name = meta["memberName"] ?: meta["name"] ?: ""
            if (name.isNotBlank()) sb.append("Removed $name")
        }
        "CHILD_CREATED", "CHILD_EDITED" -> {
            val childName = meta["childName"] ?: meta["name"] ?: event.targetId
            val remindName = meta["remindName"] ?: meta["parentName"] ?: ""
            if (childName.isNotBlank()) sb.append(childName)
            if (remindName.isNotBlank()) sb.append("\nRemind: $remindName")
        }
        "CHILD_PROFILE_DELETED", "CHILD_DELETED" -> {
            val childName = meta["childName"] ?: meta["memberName"] ?: meta["name"] ?: event.targetId
            val actor = event.actorName.ifBlank { "Member" }
            if (childName.isNotBlank()) sb.append("$actor deleted $childName's child profile.")
        }
        else -> {
            meta.entries.joinToString("\n") { "${it.key}: ${it.value}" }.let {
                if (it.isNotBlank()) sb.append(it)
            }
        }
    }
    return sb.toString()
}
