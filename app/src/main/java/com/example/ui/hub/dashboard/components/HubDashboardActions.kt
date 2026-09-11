package com.example.ui.hub.dashboard.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

// Action Button Palette
private val ColorWarmAmber = Color(0xFFE5A23C)
private val ColorDustyTeal = Color(0xFF72B5BA)
private val ColorPaleTealBg = Color(0xFFF0F7F7)
private val ColorDustyTealBorder = Color(0xFFA9CED0)
private val ColorWarmIvory = Color(0xFFFAF4EC)
private val ColorAmbientShadow = Color(0x149C876E)
private val ColorSpotShadow = Color(0x1A786550)

/**
 * Compact, icon-only circular action buttons for the Hub Dashboard:
 * - Primary: `(+)` Add Medication in Warm Amber
 * - Secondary: `(🎙)` Smart Voice Memo in Pale Teal / Dusty Teal
 */
@Composable
fun HubDashboardActions(
    onAddMedicationClick: () -> Unit,
    onSmartVoiceMemoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val addInteractionSource = remember { MutableInteractionSource() }
    val isAddPressed by addInteractionSource.collectIsPressedAsState()

    val memoInteractionSource = remember { MutableInteractionSource() }
    val isMemoPressed by memoInteractionSource.collectIsPressedAsState()

    Row(
        modifier = modifier.testTag("hub_dashboard_actions_row"),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Primary Action: + Add Medication (Circular Warm Amber Button)
        Box(
            modifier = Modifier
                .size(48.dp)
                .scale(if (isAddPressed) 0.93f else 1.0f)
                .shadow(
                    elevation = 2.dp,
                    shape = CircleShape,
                    ambientColor = ColorAmbientShadow,
                    spotColor = ColorSpotShadow
                )
                .clip(CircleShape)
                .background(ColorWarmAmber)
                .clickable(
                    interactionSource = addInteractionSource,
                    indication = null,
                    role = Role.Button,
                    onClick = onAddMedicationClick
                )
                .semantics {
                    contentDescription = "Add medication"
                    role = Role.Button
                }
                .testTag("hub_dashboard_add_medication_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = null,
                tint = ColorWarmIvory,
                modifier = Modifier.size(24.dp)
            )
        }

        // 2. Secondary Action: 🎙 Smart Voice Memo (Circular Pale Teal Button)
        Box(
            modifier = Modifier
                .size(48.dp)
                .scale(if (isMemoPressed) 0.93f else 1.0f)
                .shadow(
                    elevation = 1.dp,
                    shape = CircleShape,
                    ambientColor = ColorAmbientShadow,
                    spotColor = ColorSpotShadow
                )
                .clip(CircleShape)
                .background(ColorPaleTealBg)
                .border(BorderStroke(1.2.dp, ColorDustyTealBorder), CircleShape)
                .clickable(
                    interactionSource = memoInteractionSource,
                    indication = null,
                    role = Role.Button,
                    onClick = onSmartVoiceMemoClick
                )
                .semantics {
                    contentDescription = "Smart Voice Memo"
                    role = Role.Button
                }
                .testTag("hub_dashboard_smart_voice_memo_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Mic,
                contentDescription = null,
                tint = ColorDustyTeal,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

