package com.example.ui.hub.selection.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.profilesetup.model.ProfileAvatarType
import com.example.ui.theme.SoraFontFamily

// Scoped Text Colors
private val ColorDustyTeal = Color(0xFF72B5BA)
private val ColorTextMuted = Color(0xFF665F58)

/**
 * Hub Selection main content panel.
 */
@Composable
fun HubSelectionPanel(
    onCreateHubClick: () -> Unit,
    onJoinHubClick: () -> Unit,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 480.dp)
            .padding(horizontal = if (isCompact) 12.dp else 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Main Heading ("Where's your family?")
        Text(
            text = "Where's your family?",
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = if (isCompact) 26.sp else 30.sp,
            color = ColorDustyTeal,
            textAlign = TextAlign.Center,
            letterSpacing = (-0.6).sp,
            lineHeight = if (isCompact) 32.sp else 36.sp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("hub_selection_heading")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Supporting Text
        Text(
            text = "Create a new family hub or join one that's already waiting for you.",
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = if (isCompact) 13.5.sp else 14.5.sp,
            color = ColorTextMuted,
            textAlign = TextAlign.Center,
            lineHeight = 21.sp,
            letterSpacing = 0.15.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .testTag("hub_selection_subheading")
        )

        Spacer(modifier = Modifier.height(if (isCompact) 26.dp else 32.dp))

        // 3. Choice Cards
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(if (isCompact) 14.dp else 18.dp)
        ) {
            HubChoiceCard(
                type = HubChoiceType.CREATE,
                title = "Create your family hub",
                description = "Start a shared space for your family and invite everyone with a unique Hub Code.",
                onClick = onCreateHubClick,
                isCompact = isCompact
            )

            HubChoiceCard(
                type = HubChoiceType.JOIN,
                title = "Join a family hub",
                description = "Have a Hub Code? Enter it to join your family's shared medication space.",
                onClick = onJoinHubClick,
                isCompact = isCompact
            )
        }
    }
}
