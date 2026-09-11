package com.example.ui.profile.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
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
import com.example.ui.profilesetup.components.ProfileAvatarView
import com.example.ui.profilesetup.model.ProfileAvatarType
import com.example.ui.theme.DarkWarmText
import com.example.ui.theme.DustyTeal
import com.example.ui.theme.SoraFontFamily
import com.example.ui.theme.WarmCream
import com.example.ui.theme.WarmIvory
import com.example.ui.theme.WarmNeutral

// Design tokens
private val ColorDarkWarmText = DarkWarmText
private val ColorWarmNeutral = WarmNeutral
private val ColorDustyTeal = DustyTeal
private val ColorWarmCream = WarmCream
private val ColorWarmIvory = WarmIvory
private val ColorBorderWarm = Color(0xFFE4D5C2)
private val ColorAmbientShadow = Color(0x129C876E)
private val ColorSpotShadow = Color(0x18786550)

/**
 * Compact personal profile header in MedTrack visual language.
 * Displays:
 * - Gender-based avatar
 * - User Name
 * - User Email
 * - "Edit Profile" compact outlined button
 */
@Composable
fun ProfileHeaderSection(
    name: String,
    email: String,
    avatarType: ProfileAvatarType,
    isEditing: Boolean,
    onEditProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("profile_header_section"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Dynamic Profile Avatar
        Box(
            modifier = Modifier
                .size(76.dp)
                .shadow(
                    elevation = 3.dp,
                    shape = CircleShape,
                    ambientColor = ColorAmbientShadow,
                    spotColor = ColorSpotShadow
                )
                .clip(CircleShape)
                .background(ColorWarmCream)
                .border(BorderStroke(1.5.dp, ColorBorderWarm), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            ProfileAvatarView(
                avatarType = avatarType,
                size = 68.dp,
                modifier = Modifier.testTag("profile_header_avatar")
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // User Name
        Text(
            text = name.ifBlank { "Family Member" },
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp,
            color = ColorDarkWarmText,
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag("profile_header_name")
        )

        Spacer(modifier = Modifier.height(3.dp))

        // User Email (from Firebase Auth)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier.testTag("profile_header_email_row")
        ) {
            Icon(
                imageVector = Icons.Outlined.Email,
                contentDescription = null,
                tint = ColorWarmNeutral,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = email.ifBlank { "No email associated" },
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 12.5.sp,
                color = ColorWarmNeutral,
                modifier = Modifier.testTag("profile_header_email")
            )
        }

        if (!isEditing) {
            Spacer(modifier = Modifier.height(12.dp))

            // Compact secondary/outlined MedTrack "Edit Profile" button
            OutlinedButton(
                onClick = onEditProfileClick,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.2.dp, ColorDustyTeal),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = ColorDustyTeal
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                modifier = Modifier
                    .height(36.dp)
                    .testTag("profile_edit_profile_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = null,
                    tint = ColorDustyTeal,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Edit Profile",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.5.sp,
                    color = ColorDustyTeal
                )
            }
        }
    }
}
