package com.example.ui.hub.selection.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.profilesetup.components.ProfileAvatarView
import com.example.ui.profilesetup.model.ProfileAvatarType
import com.example.ui.theme.SoraFontFamily

// Scoped tokens
private val ColorWarmIvory = Color(0xFFFAF4EC)
private val ColorWarmCream = Color(0xFFF3E6D5)
private val ColorDarkWarmText = Color(0xFF514A44)
private val ColorWarmAmber = Color(0xFFE5A23C)
private val ColorTextMuted = Color(0xFF665F58)
private val ColorBorderWarm = Color(0xFFE4D5C2)
private val ColorAmbientShadow = Color(0x129C876E)
private val ColorSpotShadow = Color(0x18786550)

/**
 * Compact top user profile summary header for the Hub Selection page.
 *
 * Displays:
 * [ Gender-based avatar ]
 * User's name
 * Age (e.g. "24 years" or "70+ years")
 */
@Composable
fun UserProfileHeader(
    name: String,
    age: Int,
    avatarType: ProfileAvatarType,
    modifier: Modifier = Modifier
) {
    val ageDisplayText = if (age >= 70) "70+ years" else "$age years"
    val displayName = name.ifBlank { "You" }

    Column(
        modifier = modifier
            .semantics {
                contentDescription = "User profile: $displayName, $ageDisplayText"
            }
            .testTag("hub_user_profile_header"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Compact Avatar (68dp)
        ProfileAvatarView(
            avatarType = avatarType,
            size = 68.dp,
            modifier = Modifier.testTag("hub_header_avatar")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // User's name
        Text(
            text = displayName,
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            color = ColorDarkWarmText,
            letterSpacing = (-0.2).sp,
            modifier = Modifier.testTag("hub_header_user_name")
        )

        Spacer(modifier = Modifier.height(2.dp))

        // User's age
        Text(
            text = ageDisplayText,
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            color = ColorWarmAmber,
            letterSpacing = 0.1.sp,
            modifier = Modifier.testTag("hub_header_user_age")
        )
    }
}
