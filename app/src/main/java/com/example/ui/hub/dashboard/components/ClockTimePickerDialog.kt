package com.example.ui.hub.dashboard.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.SoraFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

// MedTrack Palette tokens
private val ColorWarmIvory = Color(0xFFFAF4EC)
private val ColorWarmCream = Color(0xFFF3E6D5)
private val ColorDustyTeal = Color(0xFF72B5BA)
private val ColorPaleTealBg = Color(0xFFEBF5F5)
private val ColorWarmAmber = Color(0xFFE5A23C)
private val ColorDarkWarmText = Color(0xFF514A44)
private val ColorTextMuted = Color(0xFF665F58)
private val ColorBorderWarm = Color(0xFFE4D5C2)
private val ColorBorderActive = Color(0xFF72B5BA)

/**
 * Dedicated Analog Clock Time Picker Dialog for MedTrack reminder time.
 *
 * Implements:
 * - Familiar analog clock face dial (Hour selection 1..12 and Minute selection 00..59)
 * - Draggable/selectable clock hand that smoothly follows user touch/drag
 * - Clear highlighted state for selected hour and minute
 * - Natural transition from hour selection to minute selection
 * - Prominent AM / PM selector
 * - Clean digital readout header (e.g. "9:00 AM" / "2:30 PM")
 * - Restrained MedTrack aesthetic styling with Warm Ivory, Warm Cream, Dusty Teal, and Warm Amber
 */
@Composable
fun ClockTimePickerDialog(
    initialHour: Int, // 0..23
    initialMinute: Int, // 0..59
    onConfirmTime: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    // 12-hour breakdown
    var isAm by remember { mutableStateOf(initialHour < 12) }
    var selectedHour12 by remember {
        mutableIntStateOf(
            when (initialHour % 12) {
                0 -> 12
                else -> initialHour % 12
            }
        )
    }
    var selectedMinute by remember { mutableIntStateOf(initialMinute) }

    // Mode: true = selecting hour, false = selecting minute
    var isSelectingHour by remember { mutableStateOf(true) }

    // Live dragging angle (null when not dragging)
    var dragAngleDeg by remember { mutableStateOf<Double?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(24.dp))
                .background(ColorWarmIvory)
                .border(BorderStroke(1.2.dp, ColorBorderWarm), RoundedCornerShape(24.dp))
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .testTag("clock_time_picker_dialog"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Header Kicker & Title
                Text(
                    text = "Select Reminder Time",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = ColorDarkWarmText
                )

                // 2. Digital Readout & AM/PM Toggle Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Time Readout Box: [ HH ] : [ MM ]
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Hour box
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelectingHour) ColorPaleTealBg else ColorWarmCream)
                                .border(
                                    BorderStroke(
                                        if (isSelectingHour) 1.5.dp else 1.dp,
                                        if (isSelectingHour) ColorBorderActive else ColorBorderWarm
                                    ),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable(role = Role.Button) { isSelectingHour = true }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                .testTag("clock_time_picker_hour_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = String.format(Locale.getDefault(), "%02d", selectedHour12),
                                fontFamily = SoraFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 26.sp,
                                color = if (isSelectingHour) ColorDustyTeal else ColorDarkWarmText
                            )
                        }

                        Text(
                            text = ":",
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = ColorDarkWarmText
                        )

                        // Minute box
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (!isSelectingHour) ColorPaleTealBg else ColorWarmCream)
                                .border(
                                    BorderStroke(
                                        if (!isSelectingHour) 1.5.dp else 1.dp,
                                        if (!isSelectingHour) ColorBorderActive else ColorBorderWarm
                                    ),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable(role = Role.Button) { isSelectingHour = false }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                .testTag("clock_time_picker_minute_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = String.format(Locale.getDefault(), "%02d", selectedMinute),
                                fontFamily = SoraFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 26.sp,
                                color = if (!isSelectingHour) ColorDustyTeal else ColorDarkWarmText
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // AM / PM Segmented Column
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .border(BorderStroke(1.dp, ColorBorderWarm), RoundedCornerShape(10.dp))
                            .background(ColorWarmCream)
                    ) {
                        // AM
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(topStart = 9.dp, topEnd = 9.dp))
                                .background(if (isAm) ColorDustyTeal else ColorWarmCream)
                                .clickable(role = Role.Button) { isAm = true }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("clock_time_picker_am_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "AM",
                                fontFamily = SoraFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (isAm) ColorWarmIvory else ColorDarkWarmText
                            )
                        }

                        // Divider
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(1.dp)
                                .background(ColorBorderWarm)
                        )

                        // PM
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(bottomStart = 9.dp, bottomEnd = 9.dp))
                                .background(if (!isAm) ColorDustyTeal else ColorWarmCream)
                                .clickable(role = Role.Button) { isAm = false }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("clock_time_picker_pm_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "PM",
                                fontFamily = SoraFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (!isAm) ColorWarmIvory else ColorDarkWarmText
                            )
                        }
                    }
                }

                // 3. Instruction Label
                Text(
                    text = if (isSelectingHour) "Select hour on clock" else "Select minutes on clock",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = ColorTextMuted
                )

                // 4. Analog Clock Face Dial
                val dialSizeDp = 220.dp
                Box(
                    modifier = Modifier
                        .size(dialSizeDp)
                        .clip(CircleShape)
                        .background(ColorWarmCream)
                        .border(BorderStroke(1.2.dp, ColorBorderWarm), CircleShape)
                        // Touch / Drag detection on clock face
                        .pointerInput(isSelectingHour) {
                            detectTapGestures { offset ->
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val angleDeg = calculateAngleDeg(offset, center)
                                if (isSelectingHour) {
                                    val h = angleToHour(angleDeg)
                                    selectedHour12 = h
                                    // Smooth natural transition into minute selection
                                    coroutineScope.launch {
                                        delay(180)
                                        isSelectingHour = false
                                    }
                                } else {
                                    selectedMinute = angleToMinute(angleDeg)
                                }
                            }
                        }
                        .pointerInput(isSelectingHour) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    val center = Offset(size.width / 2f, size.height / 2f)
                                    val angleDeg = calculateAngleDeg(offset, center)
                                    dragAngleDeg = angleDeg
                                    if (isSelectingHour) {
                                        selectedHour12 = angleToHour(angleDeg)
                                    } else {
                                        selectedMinute = angleToMinute(angleDeg)
                                    }
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    val center = Offset(size.width / 2f, size.height / 2f)
                                    val angleDeg = calculateAngleDeg(change.position, center)
                                    dragAngleDeg = angleDeg
                                    if (isSelectingHour) {
                                        selectedHour12 = angleToHour(angleDeg)
                                    } else {
                                        selectedMinute = angleToMinute(angleDeg)
                                    }
                                },
                                onDragEnd = {
                                    dragAngleDeg = null
                                    if (isSelectingHour) {
                                        // Smooth natural transition into minute selection after drag ends
                                        coroutineScope.launch {
                                            delay(180)
                                            isSelectingHour = false
                                        }
                                    }
                                },
                                onDragCancel = {
                                    dragAngleDeg = null
                                }
                            )
                        }
                        .testTag("clock_face_canvas"),
                    contentAlignment = Alignment.Center
                ) {
                    // Clock Hand & Highlights Canvas
                    val activeAngleDeg = dragAngleDeg ?: if (isSelectingHour) {
                        (selectedHour12 * 30.0) % 360.0
                    } else {
                        (selectedMinute * 6.0) % 360.0
                    }

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val radius = size.width / 2f
                        val handLength = radius * 0.76f

                        // Angle to cartesian (0° is 12 o'clock, clockwise)
                        val angleRad = (activeAngleDeg - 90.0) * (PI / 180.0)
                        val handEnd = Offset(
                            x = center.x + (handLength * cos(angleRad)).toFloat(),
                            y = center.y + (handLength * sin(angleRad)).toFloat()
                        )

                        // Subtle center pivot
                        drawCircle(
                            color = ColorDustyTeal,
                            radius = 5.dp.toPx(),
                            center = center
                        )
                        drawCircle(
                            color = ColorWarmIvory,
                            radius = 2.dp.toPx(),
                            center = center
                        )

                        // Clock hand needle
                        drawLine(
                            color = ColorDustyTeal,
                            start = center,
                            end = handEnd,
                            strokeWidth = 2.5.dp.toPx(),
                            cap = StrokeCap.Round
                        )

                        // End highlight circle
                        val highlightRadius = if (isSelectingHour || selectedMinute % 5 == 0) {
                            16.dp.toPx()
                        } else {
                            7.dp.toPx()
                        }

                        drawCircle(
                            color = ColorDustyTeal,
                            radius = highlightRadius,
                            center = handEnd
                        )

                        // If minute is not a multiple of 5, draw small inner dot
                        if (!isSelectingHour && selectedMinute % 5 != 0) {
                            drawCircle(
                                color = ColorWarmIvory,
                                radius = 2.5.dp.toPx(),
                                center = handEnd
                            )
                        }
                    }

                    // Number Labels Overlay (1..12 or 00..55)
                    AnimatedContent(
                        targetState = isSelectingHour,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "clock_face_numbers"
                    ) { selectingHourMode ->
                        Box(modifier = Modifier.fillMaxSize()) {
                            val clockRadiusPx = with(androidx.compose.ui.platform.LocalDensity.current) {
                                (dialSizeDp / 2).toPx()
                            }
                            val numRadius = clockRadiusPx * 0.76f

                            if (selectingHourMode) {
                                // 12 Hour positions
                                (1..12).forEach { hour ->
                                    val isSelected = selectedHour12 == hour
                                    val angleDeg = (hour * 30.0 - 90.0)
                                    val angleRad = angleDeg * (PI / 180.0)
                                    val xOffset = (numRadius * cos(angleRad)).roundToInt()
                                    val yOffset = (numRadius * sin(angleRad)).roundToInt()

                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .offset { IntOffset(xOffset, yOffset) }
                                            .size(28.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = hour.toString(),
                                            fontFamily = SoraFontFamily,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 13.5.sp,
                                            color = if (isSelected) ColorWarmIvory else ColorDarkWarmText
                                        )
                                    }
                                }
                            } else {
                                // 12 Minute positions (00, 05, 10, ..., 55)
                                (0..11).forEach { step ->
                                    val minuteVal = step * 5
                                    val isSelected = selectedMinute == minuteVal
                                    val angleDeg = (step * 30.0 - 90.0)
                                    val angleRad = angleDeg * (PI / 180.0)
                                    val xOffset = (numRadius * cos(angleRad)).roundToInt()
                                    val yOffset = (numRadius * sin(angleRad)).roundToInt()

                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .offset { IntOffset(xOffset, yOffset) }
                                            .size(28.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = String.format(Locale.getDefault(), "%02d", minuteVal),
                                            fontFamily = SoraFontFamily,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 12.sp,
                                            color = if (isSelected) ColorWarmIvory else ColorDarkWarmText
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 5. Actions: Cancel / Confirm (Set Time)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ColorWarmCream)
                            .border(BorderStroke(1.dp, ColorBorderWarm), RoundedCornerShape(12.dp))
                            .clickable(onClick = onDismiss)
                            .testTag("clock_time_picker_cancel_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Cancel",
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = ColorDarkWarmText
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ColorWarmAmber)
                            .clickable {
                                // Convert 12-hour + AM/PM back to 24-hour hour
                                val finalHour24 = when {
                                    isAm && selectedHour12 == 12 -> 0
                                    isAm -> selectedHour12
                                    !isAm && selectedHour12 == 12 -> 12
                                    else -> selectedHour12 + 12
                                }
                                onConfirmTime(finalHour24, selectedMinute)
                            }
                            .testTag("clock_time_picker_set_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Set Time",
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = ColorWarmIvory
                        )
                    }
                }
            }
        }
    }
}

/**
 * Calculates clockwise angle in degrees (0..360) where 0° is 12 o'clock.
 */
private fun calculateAngleDeg(touch: Offset, center: Offset): Double {
    val dx = touch.x - center.x
    val dy = touch.y - center.y
    val rad = atan2(dy.toDouble(), dx.toDouble())
    var deg = Math.toDegrees(rad) + 90.0
    if (deg < 0) deg += 360.0
    return deg % 360.0
}

/**
 * Maps dial angle (0..360) to hour 1..12.
 */
private fun angleToHour(angleDeg: Double): Int {
    val rawHour = (Math.round(angleDeg / 30.0).toInt() % 12)
    return if (rawHour == 0) 12 else rawHour
}

/**
 * Maps dial angle (0..360) to minute 0..59.
 */
private fun angleToMinute(angleDeg: Double): Int {
    return (Math.round(angleDeg / 6.0).toInt() % 60)
}
