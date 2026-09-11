package com.example.ui.hub.dashboard.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.Icon
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
import com.example.ui.hub.components.HubAccountMenu
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.profilesetup.components.ProfileAvatarView
import com.example.ui.profilesetup.model.ProfileAvatarType
import com.example.ui.theme.DarkWarmText
import com.example.ui.theme.DustyTeal
import com.example.ui.theme.IbmPlexMonoFontFamily
import com.example.ui.theme.SoraFontFamily
import com.example.ui.theme.WarmCream
import com.example.ui.theme.WarmIvory
import com.example.ui.theme.WarmNeutral
import kotlinx.coroutines.delay

// Scoped MedTrack Palette tokens matching design system
private val ColorWarmCream = WarmCream
private val ColorWarmIvory = WarmIvory
private val ColorDustyTeal = DustyTeal
private val ColorDarkWarmText = DarkWarmText
private val ColorTextMuted = WarmNeutral
private val ColorBorderWarm = Color(0xFFE4D5C2)
private val ColorBorderCodeBlock = Color(0xFFDEC4AA)
private val ColorBorderCodeActive = Color(0xFFD4B090)
private val ColorAmbientShadow = Color(0x109C876E)
private val ColorSpotShadow = Color(0x16786550)

/**
 * Hub Dashboard Top Header:
 * - Left: Hub Kicker, Hub Name (Sora Extra Bold), and copyable Hub Code box (Warm Cream surface, IBM Plex Mono).
 * - Right: Compact profile avatar icon strictly displaying avatar only (no name/age/gender text).
 */
@Composable
fun HubDashboardHeader(
    hubName: String,
    hubCode: String,
    avatarType: ProfileAvatarType,
    onProfileClick: () -> Unit = {},
    onMyProfileClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isCopied by remember { mutableStateOf(false) }
    var isAccountMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(isCopied) {
        if (isCopied) {
            delay(2000)
            isCopied = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("hub_dashboard_header")
    ) {
        // Top Row: Hub Name on left, Profile Avatar on right
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp)
            ) {
                // Section Kicker (SemiBold, not extra bold)
                Text(
                    text = "FAMILY HUB",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp,
                    letterSpacing = 1.6.sp,
                    color = ColorDustyTeal,
                    modifier = Modifier.testTag("hub_dashboard_kicker")
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Prominent Extra-Bold Hub Name (reduced from 24sp to 19sp while keeping ExtraBold)
                Text(
                    text = hubName,
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight(800), // ExtraBold 800
                    fontSize = 19.sp,
                    lineHeight = 23.sp,
                    color = ColorDarkWarmText,
                    modifier = Modifier.testTag("hub_dashboard_title")
                )
            }

            // Top-right compact profile icon with account dropdown menu (My profile, Logout)
            Box(contentAlignment = Alignment.TopEnd) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .shadow(
                            elevation = 2.dp,
                            shape = CircleShape,
                            ambientColor = ColorAmbientShadow,
                            spotColor = ColorSpotShadow
                        )
                        .clip(CircleShape)
                        .background(ColorWarmIvory)
                        .border(BorderStroke(1.2.dp, ColorBorderWarm), CircleShape)
                        .clickable(
                            role = Role.Button,
                            onClick = {
                                onProfileClick()
                                isAccountMenuExpanded = !isAccountMenuExpanded
                            }
                        )
                        .testTag("hub_dashboard_profile_icon"),
                    contentAlignment = Alignment.Center
                ) {
                    ProfileAvatarView(
                        avatarType = avatarType,
                        size = 32.dp,
                        modifier = Modifier.padding(1.dp)
                    )
                }

                // Account dropdown menu providing "My profile" and "Logout"
                HubAccountMenu(
                    expanded = isAccountMenuExpanded,
                    onDismiss = { isAccountMenuExpanded = false },
                    onMyProfileClick = {
                        isAccountMenuExpanded = false
                        onMyProfileClick()
                    },
                    onLogoutClick = {
                        isAccountMenuExpanded = false
                        onLogoutClick()
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Hub Code Sub-Header Section
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "HUB CODE",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                letterSpacing = 1.4.sp,
                color = ColorTextMuted.copy(alpha = 0.8f)
            )

            // Copyable Hub Code Box on Warm Cream Surface with IBM Plex Mono font
            Row(
                modifier = Modifier
                    .shadow(
                        elevation = 2.dp,
                        shape = RoundedCornerShape(9.dp),
                        ambientColor = ColorAmbientShadow,
                        spotColor = ColorSpotShadow
                    )
                    .clip(RoundedCornerShape(9.dp))
                    .background(ColorWarmCream)
                    .border(BorderStroke(1.2.dp, ColorBorderCodeBlock), RoundedCornerShape(9.dp))
                    .clickable {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("MedTrack Hub Code", hubCode)
                        clipboard.setPrimaryClip(clip)
                        isCopied = true
                    }
                    .padding(horizontal = 9.dp, vertical = 4.dp)
                    .semantics { contentDescription = "Hub Code $hubCode, tap to copy" }
                    .testTag("hub_dashboard_hub_code_box"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Text(
                    text = hubCode,
                    fontFamily = IbmPlexMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 1.8.sp,
                    color = ColorDarkWarmText
                )

                // Compact Copy / Copied Action
                Box(
                    modifier = Modifier.size(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCopied) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = "Copied",
                            tint = ColorDustyTeal,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "Copy code",
                            tint = ColorTextMuted,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            // In-place animated "Copied!" confirmation badge
            AnimatedVisibility(
                visible = isCopied,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(ColorDustyTeal.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("hub_dashboard_copied_badge")
                ) {
                    Text(
                        text = "Copied!",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = ColorDustyTeal
                    )
                }
            }
        }
    }
}
