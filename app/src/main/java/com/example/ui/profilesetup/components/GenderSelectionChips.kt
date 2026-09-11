package com.example.ui.profilesetup.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.example.ui.profilesetup.model.ProfileGender
import com.example.ui.theme.SoraFontFamily

// Scoped Gender Selection Palette
private val ColorDustyTeal = Color(0xFF72B5BA)
private val ColorUnselectedBg = Color(0xFFFAF4EC)
private val ColorBorderUnselected = Color(0xFFE4D5C2)
private val ColorLabel = Color(0xFF514A44)
private val ColorTextUnselected = Color(0xFF665F58)
private val ColorTextSelected = Color(0xFFFFFFFF)
private val ColorChipShadow = Color(0x14786550)

/**
 * Compact, tactile selection chips for Gender choice:
 * Row 1: [ Male ] [ Female ]
 * Row 2: [ Prefer not to say ]
 *
 * Distinct active styling in Dusty Teal with smooth animated color/elevation transitions.
 * Ensures the full text "Prefer not to say" is clearly displayed without any clipping or truncation.
 */
@Composable
fun GenderSelectionChips(
    selectedGender: ProfileGender?,
    onGenderSelected: (ProfileGender) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Gender",
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.5.sp,
            color = ColorLabel,
            letterSpacing = 0.1.sp,
            modifier = Modifier.padding(start = 2.dp)
        )

        // Row 1: Male and Female
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GenderChip(
                gender = ProfileGender.MALE,
                isSelected = selectedGender == ProfileGender.MALE,
                onClick = { onGenderSelected(ProfileGender.MALE) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("gender_chip_male")
            )
            GenderChip(
                gender = ProfileGender.FEMALE,
                isSelected = selectedGender == ProfileGender.FEMALE,
                onClick = { onGenderSelected(ProfileGender.FEMALE) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("gender_chip_female")
            )
        }

        // Row 2: Prefer not to say (full width, ensuring complete text visibility)
        GenderChip(
            gender = ProfileGender.PREFER_NOT_TO_SAY,
            isSelected = selectedGender == ProfileGender.PREFER_NOT_TO_SAY,
            onClick = { onGenderSelected(ProfileGender.PREFER_NOT_TO_SAY) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("gender_chip_prefer_not_to_say")
        )
    }
}

@Composable
private fun GenderChip(
    gender: ProfileGender,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    val bgColor by animateColorAsState(
        targetValue = if (isSelected) ColorDustyTeal else ColorUnselectedBg,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "GenderChipBg"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) ColorTextSelected else ColorTextUnselected,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "GenderChipText"
    )

    val elevation by animateDpAsState(
        targetValue = if (isSelected) 3.dp else 1.dp,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "GenderChipElevation"
    )

    val chipShape = RoundedCornerShape(20.dp)

    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = chipShape,
                ambientColor = ColorChipShadow,
                spotColor = ColorChipShadow
            )
            .clip(chipShape)
            .background(bgColor)
            .border(
                border = BorderStroke(
                    1.dp,
                    if (isSelected) ColorDustyTeal else ColorBorderUnselected
                ),
                shape = chipShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.RadioButton,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .semantics {
                contentDescription = "${gender.label} gender option, ${if (isSelected) "selected" else "not selected"}"
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = gender.label,
            fontFamily = SoraFontFamily,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            fontSize = 12.5.sp,
            color = textColor,
            letterSpacing = 0.05.sp,
            softWrap = false,
            maxLines = 1
        )
    }
}
