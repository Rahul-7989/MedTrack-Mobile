package com.example.ui.hub.create.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkWarmText
import com.example.ui.theme.DustyTeal
import com.example.ui.theme.IbmPlexMonoFontFamily
import com.example.ui.theme.SoraFontFamily
import com.example.ui.theme.WarmAmber
import com.example.ui.theme.WarmCream
import com.example.ui.theme.WarmIvory
import com.example.ui.theme.WarmNeutral
import kotlinx.coroutines.delay

// Scoped tokens matching MedTrack design system
private val ColorWarmIvory = WarmIvory
private val ColorWarmCream = WarmCream
private val ColorWarmAmber = WarmAmber
private val ColorBurntApricot = Color(0xFFD88B3D)
private val ColorDustyTeal = DustyTeal
private val ColorDarkWarmText = DarkWarmText
private val ColorTextMuted = WarmNeutral
private val ColorBorderWarm = Color(0xFFE4D5C2)
private val ColorBorderCodeBlock = Color(0xFFDEC4AA)
private val ColorBorderCodeActive = Color(0xFFD4B090)
private val ColorSuccessGreen = Color(0xFF438A6E)
private val ColorAmbientShadow = Color(0x109C876E)
private val ColorSpotShadow = Color(0x16786550)

/**
 * Hub Code section with blank placeholder state, Generate action,
 * immutable code display with Warm Cream #F3E6D5 dedicated surface, IBM Plex Mono font,
 * Dark Warm Text #514A44 characters, and one-tap copy functionality.
 */
@Composable
fun HubCodeSection(
    hubCode: String?,
    isHubNameEntered: Boolean,
    onGenerateCodeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isCopied by remember { mutableStateOf(false) }

    LaunchedEffect(isCopied) {
        if (isCopied) {
            delay(2200)
            isCopied = false
        }
    }

    val isCodeGenerated = !hubCode.isNullOrBlank()

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        // Section Header: "HUB CODE"
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "HUB CODE",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = ColorTextMuted,
                letterSpacing = 1.sp,
                modifier = Modifier.testTag("hub_code_section_label")
            )

            if (isCodeGenerated) {
                // Subtle permanent badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(ColorWarmCream)
                        .border(BorderStroke(1.dp, ColorBorderWarm), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Permanent",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        color = ColorBurntApricot
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (!isCodeGenerated) {
            // State A: Blank code placeholder on Warm Cream surface
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .shadow(
                        elevation = 2.dp,
                        shape = RoundedCornerShape(16.dp),
                        ambientColor = ColorAmbientShadow,
                        spotColor = ColorSpotShadow
                    )
                    .clip(RoundedCornerShape(16.dp))
                    .background(ColorWarmCream)
                    .border(BorderStroke(1.2.dp, ColorBorderCodeBlock), RoundedCornerShape(16.dp))
                    .testTag("hub_code_blank_placeholder"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "- - - - - -",
                    fontFamily = IbmPlexMonoFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = ColorTextMuted.copy(alpha = 0.45f),
                    letterSpacing = 6.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // "Generate Hub Code" button
            Button(
                onClick = onGenerateCodeClick,
                enabled = isHubNameEntered,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorWarmCream,
                    contentColor = ColorDarkWarmText,
                    disabledContainerColor = ColorWarmCream.copy(alpha = 0.5f),
                    disabledContentColor = ColorTextMuted.copy(alpha = 0.45f)
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isHubNameEntered) ColorBorderWarm else ColorBorderWarm.copy(alpha = 0.4f)
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 2.dp,
                    pressedElevation = 0.dp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("generate_hub_code_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = if (isHubNameEntered) ColorWarmAmber else ColorTextMuted.copy(alpha = 0.45f),
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Generate Hub Code",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        letterSpacing = 0.1.sp
                    )
                }
            }
        } else {
            // State B: Generated Code Display on Warm Cream #F3E6D5 container [ AG7K2P   Copy Button ]
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .shadow(
                        elevation = 3.dp,
                        shape = RoundedCornerShape(16.dp),
                        ambientColor = ColorAmbientShadow,
                        spotColor = ColorSpotShadow
                    )
                    .clip(RoundedCornerShape(16.dp))
                    .background(ColorWarmCream)
                    .border(BorderStroke(1.5.dp, ColorBorderCodeActive), RoundedCornerShape(16.dp))
                    .padding(horizontal = 18.dp)
                    .testTag("hub_code_display_container"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Monospaced Hub Code Characters in IBM Plex Mono and Dark Warm Text
                    Text(
                        text = hubCode,
                        fontFamily = IbmPlexMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = ColorDarkWarmText,
                        letterSpacing = 2.5.sp,
                        modifier = Modifier.testTag("generated_hub_code_text")
                    )

                    // Copy Action Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AnimatedVisibility(
                            visible = isCopied,
                            enter = fadeIn(tween(180)) + scaleIn(initialScale = 0.9f, animationSpec = tween(180)),
                            exit = fadeOut(tween(180)) + scaleOut(targetScale = 0.9f, animationSpec = tween(180))
                        ) {
                            Text(
                                text = "Copied!",
                                fontFamily = SoraFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = ColorDustyTeal,
                                modifier = Modifier.testTag("hub_code_copied_label")
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (isCopied) ColorDustyTeal.copy(alpha = 0.15f) else ColorWarmIvory)
                                .border(
                                    BorderStroke(
                                        1.dp,
                                        if (isCopied) ColorDustyTeal else ColorBorderWarm
                                    ),
                                    CircleShape
                                )
                                .clickable(
                                    role = Role.Button,
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("MedTrack Hub Code", hubCode)
                                        clipboard.setPrimaryClip(clip)
                                        isCopied = true
                                    }
                                )
                                .semantics {
                                    contentDescription = "Copy Hub Code $hubCode"
                                }
                                .testTag("copy_hub_code_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isCopied) Icons.Outlined.Check else Icons.Outlined.ContentCopy,
                                contentDescription = if (isCopied) "Copied" else "Copy code",
                                tint = if (isCopied) ColorDustyTeal else ColorDarkWarmText,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sharing Message
            Text(
                text = "Share this code with your family so they can join your hub.",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 13.5.sp,
                color = ColorTextMuted,
                lineHeight = 19.sp,
                modifier = Modifier
                    .padding(start = 2.dp)
                    .testTag("hub_code_sharing_message")
            )
        }
    }
}

// Backward compatible alias
@Composable
fun HiveCodeSection(
    hiveCode: String?,
    isHubNameEntered: Boolean,
    onGenerateCodeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    HubCodeSection(
        hubCode = hiveCode,
        isHubNameEntered = isHubNameEntered,
        onGenerateCodeClick = onGenerateCodeClick,
        modifier = modifier
    )
}
