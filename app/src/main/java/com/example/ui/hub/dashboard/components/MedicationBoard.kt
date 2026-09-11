package com.example.ui.hub.dashboard.components

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
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.ui.hub.dashboard.model.MedicationItem
import com.example.ui.theme.SoraFontFamily

// Board Palette
private val ColorWarmCream = Color(0xFFF3E6D5)
private val ColorWarmIvory = Color(0xFFFAF4EC)
private val ColorWarmAmber = Color(0xFFE5A23C)
private val ColorDustyTeal = Color(0xFF72B5BA)
private val ColorDarkWarmText = Color(0xFF514A44)
private val ColorTextMuted = Color(0xFF665F58)
private val ColorBorderWarm = Color(0xFFE4D5C2)
private val ColorAmbientShadow = Color(0x129C876E)
private val ColorSpotShadow = Color(0x18786550)

/**
 * Main Family Medication Board container.
 *
 * Warm Cream rounded surface housing today's scheduled medications or a friendly empty state.
 */
@Composable
fun MedicationBoard(
    medications: List<MedicationItem>,
    currentUserId: String,
    onToggleTaken: (medicationId: String) -> Unit,
    onCardClick: (medication: MedicationItem) -> Unit = {},
    onEditMedication: (medication: MedicationItem) -> Unit,
    onDeleteMedication: (medication: MedicationItem) -> Unit,
    onAddMedicationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = ColorAmbientShadow,
                spotColor = ColorSpotShadow
            )
            .clip(RoundedCornerShape(24.dp))
            .background(ColorWarmCream)
            .border(BorderStroke(1.2.dp, ColorBorderWarm), RoundedCornerShape(24.dp))
            .padding(18.dp)
            .testTag("hub_dashboard_medication_board")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Board Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "MEDICATIONS",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 1.4.sp,
                        color = ColorDustyTeal
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "TODAY'S SCHEDULE",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = ColorDarkWarmText
                    )
                }

                if (medications.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9.dp))
                            .background(ColorWarmIvory)
                            .border(BorderStroke(1.dp, ColorBorderWarm), RoundedCornerShape(9.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${medications.count { it.isTakenToday }}/${medications.size} Done",
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = if (medications.all { it.isTakenToday }) ColorDustyTeal else ColorTextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Content: Medication Cards list or Friendly Empty State
            if (medications.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    medications.forEach { med ->
                        val isCreator = med.createdByUid.isNotBlank() && (
                            med.createdByUid == currentUserId ||
                            (currentUserId == "current_user_local" && (med.createdByUid == "current_user_local" || med.createdByUid.isBlank()))
                        )
                        MedicationCard(
                            medication = med,
                            isCreator = isCreator,
                            onToggleTaken = { onToggleTaken(med.id) },
                            onCardClick = { onCardClick(med) },
                            onEditClick = { onEditMedication(med) },
                            onDeleteClick = { onDeleteMedication(med) }
                        )
                    }
                }
            } else {
                // Friendly Warm Empty State
                EmptyMedicationState(onAddMedicationClick = onAddMedicationClick)
            }
        }
    }
}

/**
 * Friendly, minimal empty state when the hub has no medications scheduled yet.
 */
@Composable
private fun EmptyMedicationState(
    onAddMedicationClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(ColorWarmIvory)
            .border(BorderStroke(1.dp, ColorBorderWarm.copy(alpha = 0.8f)), RoundedCornerShape(18.dp))
            .padding(vertical = 28.dp, horizontal = 20.dp)
            .testTag("medication_board_empty_state"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Minimal illustration icon
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(ColorDustyTeal.copy(alpha = 0.12f))
                .border(BorderStroke(1.2.dp, ColorDustyTeal.copy(alpha = 0.3f)), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Medication,
                contentDescription = null,
                tint = ColorDustyTeal,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Your family's schedule\nstarts here.",
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            lineHeight = 21.sp,
            color = ColorDarkWarmText,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = "Add a medication to\nget everyone on track.",
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 12.5.sp,
            lineHeight = 17.sp,
            color = ColorTextMuted,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(18.dp))

        // In-card + Add Medication Button
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(ColorWarmAmber)
                .clickable(
                    role = Role.Button,
                    onClick = onAddMedicationClick
                )
                .padding(horizontal = 16.dp, vertical = 9.dp)
                .testTag("empty_state_add_medication_button"),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    tint = ColorWarmIvory,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Add Medication",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.5.sp,
                    color = ColorWarmIvory
                )
            }
        }
    }
}
