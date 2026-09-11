package com.example.ui.getstarted.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SoraFontFamily

// Scoped panel palette for Get Started page
private val PanelSurface = Color(0xFFF3E6D5)     // Warm Cream #F3E6D5
private val PanelBorder = Color(0xFFE4D5C2)      // Very subtle warm border
private val TextTitle = Color(0xFFE5A23C)        // Warm Amber #E5A23C - strong Sora 800/900 presence
private val TextSubtitle = Color(0xFF665F58)     // Warm neutral #665F58
private val ShadowWarmAmbient = Color(0x189C876E)
private val ShadowWarmSpot = Color(0x22786550)

/**
 * Floating Warm Cream account creation panel for MedTrack.
 *
 * Implements a refined, soft-edged aesthetic with 26dp rounded corners,
 * delicate warm border, and diffused ambient shadow. Contains the header
 * ("Create your account" and "Start your MedTrack journey.") and the form content.
 */
@Composable
fun GetStartedPanel(
    modifier: Modifier = Modifier,
    isCompact: Boolean = false,
    headerAlpha: Float = 1f,
    headerOffsetY: Dp = 0.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val cornerRadius = 26.dp
    val panelShape = RoundedCornerShape(cornerRadius)

    val horizontalPadding = if (isCompact) 20.dp else 30.dp
    val verticalPadding = if (isCompact) 22.dp else 28.dp

    Box(
        modifier = modifier
            .widthIn(max = 440.dp)
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = panelShape,
                ambientColor = ShadowWarmAmbient,
                spotColor = ShadowWarmSpot
            )
            .clip(panelShape)
            .background(PanelSurface)
            .border(
                width = 1.dp,
                color = PanelBorder,
                shape = panelShape
            )
            .padding(horizontal = horizontalPadding, vertical = verticalPadding)
            .testTag("get_started_panel")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Block: Centered "Create your account" & "Start your MedTrack journey."
            Text(
                text = "Create your account",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Black,
                fontSize = if (isCompact) 21.sp else 22.5.sp,
                lineHeight = if (isCompact) 26.sp else 28.sp,
                letterSpacing = (-0.45).sp,
                color = TextTitle,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = headerOffsetY)
                    .alpha(headerAlpha)
                    .testTag("get_started_title")
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Start your MedTrack journey.",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = if (isCompact) 12.sp else 12.5.sp,
                lineHeight = 17.sp,
                color = TextSubtitle,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = headerOffsetY)
                    .alpha(headerAlpha)
                    .testTag("get_started_subtitle")
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Form inputs and actions
            content()
        }
    }
}
