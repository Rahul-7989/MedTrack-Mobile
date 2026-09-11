package com.example.ui.hub.dashboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import com.example.ui.hub.dashboard.data.HubDashboardRepository
import com.example.ui.theme.SoraFontFamily

private val ColorDarkWarmText = Color(0xFF514A44)
private val ColorTextMuted = Color(0xFF665F58)

/**
 * Clean date and time display with integrated quick action controls for the Hub Dashboard.
 *
 * Formats:
 * 11 SEP, 2026                 [ + ] [ 🎙 ]
 * 9:42 AM
 */
@Composable
fun HubDashboardTimeBar(
    dateLabel: String = HubDashboardRepository.getCurrentFormattedDate(),
    formattedTime: String,
    onAddMedicationClick: (() -> Unit)? = null,
    onSmartVoiceMemoClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("hub_dashboard_time_bar"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f, fill = false),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = dateLabel.uppercase(),
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                letterSpacing = 1.2.sp,
                color = ColorTextMuted,
                modifier = Modifier.testTag("hub_dashboard_date_label")
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = formattedTime,
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                color = ColorDarkWarmText,
                modifier = Modifier.testTag("hub_dashboard_current_time_text")
            )
        }

        if (onAddMedicationClick != null && onSmartVoiceMemoClick != null) {
            Spacer(modifier = Modifier.width(12.dp))
            HubDashboardActions(
                onAddMedicationClick = onAddMedicationClick,
                onSmartVoiceMemoClick = onSmartVoiceMemoClick
            )
        }
    }
}

