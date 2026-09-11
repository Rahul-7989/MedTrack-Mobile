package com.example.ui.profilesetup.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SoraFontFamily
import kotlin.math.roundToInt

// Scoped Age Slider Palette
private val ColorWarmAmber = Color(0xFFE5A23C)
private val ColorDustyTeal = Color(0xFF72B5BA)
private val ColorPaleTeal = Color(0xFFA9CED0)
private val ColorWarmIvory = Color(0xFFFAF4EC)
private val ColorLabel = Color(0xFF514A44)
private val ColorTextMuted = Color(0xFF665F58)

/**
 * Draggable age slider section.
 *
 * Range: 18 to 70.
 * Displays "X years" for 18..69, and "70+ years" at the maximum limit.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgeSliderSection(
    age: Int,
    onAgeChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    val ageDisplayText = if (age >= 70) "70+ years" else "$age years"

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Your age",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.5.sp,
                color = ColorLabel,
                letterSpacing = 0.1.sp,
                modifier = Modifier.padding(start = 2.dp)
            )

            // Balanced selected age value in Warm Amber (Sora 600, 13sp)
            Text(
                text = ageDisplayText,
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = ColorWarmAmber,
                letterSpacing = 0.1.sp,
                modifier = Modifier
                    .semantics {
                        contentDescription = "Selected age is $ageDisplayText"
                    }
                    .testTag("profile_age_display")
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Slider(
            value = age.toFloat().coerceIn(18f, 70f),
            onValueChange = { onAgeChange(it.roundToInt()) },
            valueRange = 18f..70f,
            interactionSource = interactionSource,
            colors = SliderDefaults.colors(
                thumbColor = ColorWarmAmber,
                activeTrackColor = ColorDustyTeal,
                inactiveTrackColor = ColorPaleTeal.copy(alpha = 0.50f),
                activeTickColor = Color.Transparent,
                inactiveTickColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("profile_age_slider")
        )

        // End limits indicators (18 and 70+)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "18",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                color = ColorTextMuted
            )
            Text(
                text = "70+",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                color = ColorTextMuted
            )
        }
    }
}
