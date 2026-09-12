package com.example.ui.hub.create.components

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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SoraFontFamily

// Scoped Card Palette
private val ColorWarmCream = Color(0xFFF3E6D5)
private val ColorWarmIvory = Color(0xFFFAF4EC)
private val ColorWarmAmber = Color(0xFFE5A23C)
private val ColorDarkWarmText = Color(0xFF514A44)
private val ColorTextMuted = Color(0xFF665F58)
private val ColorBorderWarm = Color(0xFFE4D5C2)
private val ColorLabel = Color(0xFF514A44)
private val ColorAmbientShadow = Color(0x189C876E)
private val ColorSpotShadow = Color(0x22786550)

/**
 * Main elevated panel for the Create Family Hub screen.
 */
@Composable
fun CreateHubPanel(
    hubName: String,
    onHubNameChange: (String) -> Unit,
    hiveCode: String?,
    onGenerateCode: () -> Unit,
    onProceedClick: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    val focusManager = LocalFocusManager.current
    val panelShape = RoundedCornerShape(24.dp)
    val isCodeGenerated = !hiveCode.isNullOrBlank()

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
            text = "Create your family hub",
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = if (isCompact) 24.sp else 27.sp,
            color = ColorWarmAmber,
            textAlign = TextAlign.Center,
            letterSpacing = (-0.5).sp,
            lineHeight = if (isCompact) 30.sp else 34.sp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("create_hub_heading")
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 2. Supporting Text
        Text(
            text = "Give your family a name and create a unique Hub Code to invite them.",
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
                .testTag("create_hub_subheading")
        )

        Spacer(modifier = Modifier.height(if (isCompact) 22.dp else 28.dp))

        // 3. Family Hub Name Input
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Family hub name",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = ColorLabel,
                letterSpacing = 0.1.sp,
                modifier = Modifier.padding(start = 2.dp, bottom = 6.dp)
            )

            OutlinedTextField(
                value = hubName,
                onValueChange = onHubNameChange,
                readOnly = isCodeGenerated,
                placeholder = {
                    Text(
                        text = "Enter name for your hub",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        color = ColorTextMuted.copy(alpha = 0.6f)
                    )
                },
                textStyle = TextStyle(
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = ColorDarkWarmText
                ),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = ColorWarmIvory,
                    unfocusedContainerColor = ColorWarmIvory,
                    disabledContainerColor = ColorWarmIvory.copy(alpha = 0.7f),
                    focusedBorderColor = ColorWarmAmber,
                    unfocusedBorderColor = ColorBorderWarm,
                    cursorColor = ColorWarmAmber
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("family_hub_name_input")
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 4. Hub Code Section
        HubCodeSection(
            hubCode = hiveCode,
            isHubNameEntered = hubName.trim().isNotEmpty(),
            onGenerateCodeClick = {
                focusManager.clearFocus()
                onGenerateCode()
            }
        )

        Spacer(modifier = Modifier.height(if (isCompact) 26.dp else 32.dp))

        // 5. Primary Proceed Button
        ProceedToHubButton(
            onClick = onProceedClick,
            isEnabled = isCodeGenerated && hubName.trim().isNotEmpty(),
            isLoading = isLoading
        )
    }
}
