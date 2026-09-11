package com.example.ui.hub.components

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
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.auth.FirebaseAuthService
import com.example.ui.hub.data.FamilyHubRepository
import com.example.ui.profilesetup.components.ProfileAvatarView
import com.example.ui.profilesetup.model.ProfileAvatarType
import com.example.ui.profilesetup.model.UserProfileData
import com.example.ui.profilesetup.model.toAvatarType
import com.example.ui.theme.DarkWarmText
import com.example.ui.theme.DustyTeal
import com.example.ui.theme.SoraFontFamily
import com.example.ui.theme.WarmAmber
import com.example.ui.theme.WarmCream
import com.example.ui.theme.WarmIvory
import com.example.ui.theme.WarmNeutral

private val DialogSurface = WarmCream
private val DialogBorder = Color(0xFFE4D5C2)
private val ColorBurntApricot = Color(0xFFD88B3D)
private val TagBackground = Color(0xFFEFE2D3)

/**
 * Modal dialog presenting "My Profile" details for the active user in MedTrack design system.
 */
@Composable
fun MyProfileDialog(
    userProfile: UserProfileData?,
    onDismiss: () -> Unit,
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        MyProfileContent(
            userProfile = userProfile,
            onDismiss = onDismiss,
            onLogout = onLogout,
            modifier = modifier
        )
    }
}

/**
 * Content surface for My Profile view.
 */
@Composable
fun MyProfileContent(
    userProfile: UserProfileData?,
    onDismiss: () -> Unit,
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentUser = FirebaseAuthService.Instance.currentUser
    val email = currentUser?.email?.ifBlank { null } ?: "Not available"
    val name = userProfile?.name?.ifBlank { null } ?: "Family Member"
    val avatarType = userProfile?.avatarType
        ?: userProfile?.gender?.toAvatarType()
        ?: ProfileAvatarType.MALE
    val currentHub = FamilyHubRepository.currentHub.value

    Box(
        modifier = modifier
            .padding(24.dp)
            .fillMaxWidth(0.92f),
        contentAlignment = Alignment.Center
    ) {
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = DialogSurface,
                border = BorderStroke(1.dp, DialogBorder),
                shadowElevation = 12.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("my_profile_dialog")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header row: Title on left, Close button on right
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "My Profile",
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = DustyTeal,
                            modifier = Modifier.testTag("my_profile_dialog_title")
                        )

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("my_profile_close_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Close Profile",
                                tint = WarmNeutral,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Center Avatar
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .shadow(
                                elevation = 4.dp,
                                shape = CircleShape,
                                ambientColor = Color(0x189C876E),
                                spotColor = Color(0x28786550)
                            )
                            .clip(CircleShape)
                            .background(WarmIvory)
                            .border(BorderStroke(2.dp, DialogBorder), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        ProfileAvatarView(
                            avatarType = avatarType,
                            size = 72.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // User Name
                    Text(
                        text = name,
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = DarkWarmText,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("my_profile_name")
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Email Address
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Email,
                            contentDescription = null,
                            tint = WarmNeutral,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = email,
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.sp,
                            color = WarmNeutral,
                            modifier = Modifier.testTag("my_profile_email")
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Profile Badges (Age, Gender)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (userProfile != null && userProfile.age > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(TagBackground)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${userProfile.age} yrs",
                                    fontFamily = SoraFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = DarkWarmText
                                )
                            }
                        }

                        if (userProfile != null) {
                            val genderText = userProfile.gender.name.lowercase().replaceFirstChar { it.uppercase() }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(TagBackground)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = genderText,
                                    fontFamily = SoraFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = DarkWarmText
                                )
                            }
                        }

                        if (currentHub != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(DustyTeal.copy(alpha = 0.15f))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = currentHub.name.ifBlank { "Family Hub" },
                                    fontFamily = SoraFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = DustyTeal
                                )
                            }
                        }
                    }

                    // About Me Section (if provided)
                    if (!userProfile?.aboutMe.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(WarmIvory)
                                .border(BorderStroke(1.dp, DialogBorder), RoundedCornerShape(14.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = userProfile?.aboutMe.orEmpty(),
                                fontFamily = SoraFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 12.5.sp,
                                lineHeight = 17.sp,
                                color = DarkWarmText
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action Buttons: Logout and Done
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                onDismiss()
                                onLogout()
                            },
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, ColorBurntApricot),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = ColorBurntApricot
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("my_profile_dialog_logout_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Logout,
                                contentDescription = null,
                                tint = ColorBurntApricot,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Logout",
                                fontFamily = SoraFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }

                        Button(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DustyTeal,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("my_profile_dialog_done_button")
                        ) {
                            Text(
                                text = "Done",
                                fontFamily = SoraFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
