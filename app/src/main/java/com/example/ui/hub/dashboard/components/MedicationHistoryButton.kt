package com.example.ui.hub.dashboard.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkWarmText
import com.example.ui.theme.SoraFontFamily
import com.example.ui.theme.WarmIvory

private val ColorBorderWarm = Color(0xFFE4D5C2)
private val ColorWarmIvory = WarmIvory
private val ColorDarkWarmText = DarkWarmText

/**
 * Clean Medication History placeholder button placed directly below the Members section.
 * Designed to maintain the MedTrack aesthetic; non-functional placeholder.
 */
@Composable
fun MedicationHistoryButton(
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = {
            // Intentional placeholder: non-functional per specifications
        },
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.2.dp, ColorBorderWarm),
        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
            containerColor = ColorWarmIvory,
            contentColor = ColorDarkWarmText
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .testTag("medication_history_placeholder_button")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.History,
                contentDescription = null,
                tint = ColorDarkWarmText.copy(alpha = 0.8f),
                modifier = Modifier.size(17.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Medication History",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.5.sp,
                color = ColorDarkWarmText
            )
        }
    }
}
