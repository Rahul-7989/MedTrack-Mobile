package com.example.ui.profile.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.profilesetup.components.AboutMeSection
import com.example.ui.profilesetup.components.AgeSliderSection
import com.example.ui.profilesetup.components.GenderSelectionChips
import com.example.ui.profilesetup.components.ProfileNameInput
import com.example.ui.profilesetup.model.ProfileGender
import com.example.ui.theme.DarkWarmText
import com.example.ui.theme.DustyTeal
import com.example.ui.theme.SoraFontFamily
import com.example.ui.theme.WarmAmber
import com.example.ui.theme.WarmCream
import com.example.ui.theme.WarmNeutral

// Color tokens
private val ColorWarmCream = WarmCream
private val ColorDarkWarmText = DarkWarmText
private val ColorWarmNeutral = WarmNeutral
private val ColorDustyTeal = DustyTeal
private val ColorWarmAmber = WarmAmber
private val ColorTerracotta = Color(0xFFC8755D)
private val ColorBorderWarm = Color(0xFFE4D5C2)
private val ColorPaleGreenBg = Color(0xFFE8F5E9)
private val ColorSuccessGreen = Color(0xFF2E7D32)
private val ColorAmbientShadow = Color(0x109C876E)
private val ColorSpotShadow = Color(0x14786550)

/**
 * Edit Profile Form card in MedTrack aesthetic.
 * Allows user to modify:
 * - Name (required)
 * - Gender (Male, Female, Prefer not to say)
 * - Age (18 to 70+)
 * - About Me (optional, max 200 chars, live counter)
 */
@Composable
fun ProfileEditSection(
    name: String,
    onNameChange: (String) -> Unit,
    gender: ProfileGender,
    onGenderChange: (ProfileGender) -> Unit,
    age: Int,
    onAgeChange: (Int) -> Unit,
    aboutMe: String,
    onAboutMeChange: (String) -> Unit,
    isAboutMeExpanded: Boolean,
    onToggleAboutMeExpanded: (Boolean) -> Unit,
    isSaving: Boolean,
    errorMessage: String?,
    successMessage: String?,
    onSaveChanges: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(20.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = shape,
                ambientColor = ColorAmbientShadow,
                spotColor = ColorSpotShadow
            )
            .clip(shape)
            .background(ColorWarmCream)
            .border(BorderStroke(1.2.dp, ColorBorderWarm), shape)
            .padding(18.dp)
            .testTag("profile_edit_section")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "EDIT PROFILE",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.5.sp,
                    letterSpacing = 1.4.sp,
                    color = ColorDustyTeal
                )
            }

            // 1. Name input
            ProfileNameInput(
                value = name,
                onValueChange = onNameChange,
                modifier = Modifier.testTag("profile_edit_name_input")
            )

            // 2. Gender chips
            GenderSelectionChips(
                selectedGender = gender,
                onGenderSelected = onGenderChange,
                modifier = Modifier.testTag("profile_edit_gender_chips")
            )

            // 3. Age slider
            AgeSliderSection(
                age = age,
                onAgeChange = onAgeChange,
                modifier = Modifier.testTag("profile_edit_age_slider")
            )

            // 4. About Me (max 200 chars with live counter)
            AboutMeSection(
                isExpanded = isAboutMeExpanded,
                onExpandedChange = onToggleAboutMeExpanded,
                aboutMeText = aboutMe,
                onAboutMeChange = onAboutMeChange,
                modifier = Modifier.testTag("profile_edit_about_me")
            )

            // Inline Success banner
            AnimatedVisibility(
                visible = successMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                if (successMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(ColorPaleGreenBg)
                            .border(BorderStroke(1.dp, ColorSuccessGreen.copy(alpha = 0.3f)), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("profile_edit_success_banner")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = null,
                                tint = ColorSuccessGreen,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = successMessage,
                                fontFamily = SoraFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.5.sp,
                                color = ColorSuccessGreen
                            )
                        }
                    }
                }
            }

            // Inline Error banner (Terracotta #C8755D)
            AnimatedVisibility(
                visible = errorMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                if (errorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(ColorTerracotta.copy(alpha = 0.12f))
                            .border(BorderStroke(1.dp, ColorTerracotta.copy(alpha = 0.4f)), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("profile_edit_error_banner")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ErrorOutline,
                                contentDescription = null,
                                tint = ColorTerracotta,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = errorMessage,
                                fontFamily = SoraFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.5.sp,
                                color = ColorTerracotta
                            )
                        }
                    }
                }
            }

            // Action Buttons: Cancel and Save Changes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    enabled = !isSaving,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, ColorBorderWarm),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ColorWarmNeutral
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("profile_edit_cancel_button")
                ) {
                    Text(
                        text = "Cancel",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }

                Button(
                    onClick = onSaveChanges,
                    enabled = !isSaving && name.trim().isNotBlank(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorWarmAmber,
                        contentColor = ColorDarkWarmText,
                        disabledContainerColor = ColorWarmAmber.copy(alpha = 0.4f),
                        disabledContentColor = ColorDarkWarmText.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("profile_edit_save_button")
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            color = ColorDarkWarmText,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Text(
                            text = "Save Changes",
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
