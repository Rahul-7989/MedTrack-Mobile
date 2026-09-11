package com.example.ui.hub.join.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SoraFontFamily

// Scoped Card Palette
private val ColorWarmCream = Color(0xFFF3E6D5)
private val ColorDustyTeal = Color(0xFF72B5BA)
private val ColorTextMuted = Color(0xFF665F58)
private val ColorBorderWarm = Color(0xFFE4D5C2)
private val ColorAmbientShadow = Color(0x189C876E)
private val ColorSpotShadow = Color(0x22786550)

/**
 * Main elevated panel for the Join Family Hub screen.
 */
@Composable
fun JoinHubPanel(
    code: String,
    onCodeChange: (String) -> Unit,
    onJoinClick: () -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    val panelShape = RoundedCornerShape(24.dp)
    val isCodeComplete = code.length == 6

    Column(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 460.dp)
            .shadow(
                elevation = 6.dp,
                shape = panelShape,
                ambientColor = ColorAmbientShadow,
                spotColor = ColorSpotShadow
            )
            .clip(panelShape)
            .background(ColorWarmCream)
            .border(BorderStroke(1.5.dp, ColorBorderWarm), panelShape)
            .padding(
                horizontal = if (isCompact) 20.dp else 28.dp,
                vertical = if (isCompact) 24.dp else 32.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Primary Heading
        Text(
            text = "Join your family hub",
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = if (isCompact) 24.sp else 27.sp,
            color = ColorDustyTeal,
            textAlign = TextAlign.Center,
            letterSpacing = (-0.5).sp,
            lineHeight = if (isCompact) 30.sp else 34.sp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("join_hub_heading")
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 2. Supporting Text
        Text(
            text = "Enter the Hub Code shared by your family member.",
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = if (isCompact) 14.sp else 15.sp,
            color = ColorTextMuted,
            textAlign = TextAlign.Center,
            lineHeight = 21.sp,
            letterSpacing = 0.15.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .testTag("join_hub_subheading")
        )

        Spacer(modifier = Modifier.height(if (isCompact) 24.dp else 30.dp))

        // 3. 6-Character Hub Code Input
        HubCodeInputBox(
            code = code,
            onCodeChange = onCodeChange,
            onImeDone = {
                if (isCodeComplete && !isLoading) {
                    onJoinClick()
                }
            },
            errorMessage = errorMessage,
            isCompact = isCompact
        )

        Spacer(modifier = Modifier.height(if (isCompact) 26.dp else 32.dp))

        // 4. Primary Join Button
        JoinTheFamilyHubButton(
            onClick = onJoinClick,
            isEnabled = isCodeComplete,
            isLoading = isLoading
        )
    }
}
