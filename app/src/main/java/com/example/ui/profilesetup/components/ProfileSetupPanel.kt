package com.example.ui.profilesetup.components

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.profilesetup.model.ProfileGender
import com.example.ui.profilesetup.model.toAvatarType
import com.example.ui.theme.SoraFontFamily
import kotlinx.coroutines.delay

// Scoped Panel Palette
private val PanelSurface = Color(0xFFF3E6D5)     // Warm Cream #F3E6D5
private val PanelBorder = Color(0xFFE4D5C2)      // Very subtle warm border
private val TextTitle = Color(0xFF72B5BA)        // Dusty Teal #72B5BA (Sora 800/900)
private val TextNeutral = Color(0xFF665F58)      // Warm Neutral #665F58
private val ShadowWarmAmbient = Color(0x189C876E)
private val ShadowWarmSpot = Color(0x22786550)

/**
 * Main Profile Setup elevated surface.
 *
 * Implements the tactile, dimensional MedTrack design language:
 * - 28dp rounded corners
 * - 1dp subtle warm border (#E4D5C2)
 * - Soft diffused ambient and spot shadows
 * - Clear vertical hierarchy with smooth entrance animations:
 *   1. Dynamic Profile Avatar (Male / Female / Anonymous)
 *   2. "Let's set up your profile" heading (Sora 800/900 Dusty Teal)
 *   3. "A few details so your family knows who's who." subheading
 *   4. "Your name" single-line field
 *   5. "Gender" compact chips
 *   6. "Your age" draggable slider with prominent display
 *   7. Optional About Me section
 *   8. "Continue" primary CTA
 */
@Composable
fun ProfileSetupPanel(
    name: String,
    onNameChange: (String) -> Unit,
    gender: ProfileGender?,
    onGenderChange: (ProfileGender) -> Unit,
    age: Int,
    onAgeChange: (Int) -> Unit,
    isAboutMeExpanded: Boolean,
    onAboutMeExpandedChange: (Boolean) -> Unit,
    aboutMeText: String,
    onAboutMeTextChange: (String) -> Unit,
    isFormValid: Boolean,
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false,
    isReducedMotion: Boolean = false
) {
    val cornerRadius = 28.dp
    val panelShape = RoundedCornerShape(cornerRadius)

    val horizontalPadding = if (isCompact) 20.dp else 28.dp
    val verticalPadding = if (isCompact) 22.dp else 30.dp

    // Staggered entrance animation steps
    var animStep1 by remember { mutableStateOf(isReducedMotion) }
    var animStep2 by remember { mutableStateOf(isReducedMotion) }
    var animStep3 by remember { mutableStateOf(isReducedMotion) }
    var animStep4 by remember { mutableStateOf(isReducedMotion) }

    LaunchedEffect(Unit) {
        if (!isReducedMotion) {
            animStep1 = true
            delay(80)
            animStep2 = true
            delay(90)
            animStep3 = true
            delay(90)
            animStep4 = true
        }
    }

    val easeOut = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)

    val alpha1 by animateFloatAsState(
        targetValue = if (animStep1) 1f else 0f,
        animationSpec = tween(durationMillis = 380, easing = easeOut),
        label = "AnimAlpha1"
    )
    val offsetY1 by animateFloatAsState(
        targetValue = if (animStep1) 0f else 12f,
        animationSpec = tween(durationMillis = 380, easing = easeOut),
        label = "AnimOffset1"
    )

    val alpha2 by animateFloatAsState(
        targetValue = if (animStep2) 1f else 0f,
        animationSpec = tween(durationMillis = 380, easing = easeOut),
        label = "AnimAlpha2"
    )
    val offsetY2 by animateFloatAsState(
        targetValue = if (animStep2) 0f else 12f,
        animationSpec = tween(durationMillis = 380, easing = easeOut),
        label = "AnimOffset2"
    )

    val alpha3 by animateFloatAsState(
        targetValue = if (animStep3) 1f else 0f,
        animationSpec = tween(durationMillis = 380, easing = easeOut),
        label = "AnimAlpha3"
    )
    val offsetY3 by animateFloatAsState(
        targetValue = if (animStep3) 0f else 10f,
        animationSpec = tween(durationMillis = 380, easing = easeOut),
        label = "AnimOffset3"
    )

    val alpha4 by animateFloatAsState(
        targetValue = if (animStep4) 1f else 0f,
        animationSpec = tween(durationMillis = 380, easing = easeOut),
        label = "AnimAlpha4"
    )
    val offsetY4 by animateFloatAsState(
        targetValue = if (animStep4) 0f else 8f,
        animationSpec = tween(durationMillis = 380, easing = easeOut),
        label = "AnimOffset4"
    )

    Box(
        modifier = modifier
            .widthIn(max = 460.dp)
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
                border = BorderStroke(1.dp, PanelBorder),
                shape = panelShape
            )
            .padding(horizontal = horizontalPadding, vertical = verticalPadding)
            .testTag("profile_setup_panel")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Dynamic Profile Avatar (Automatically determined by gender)
            Box(
                modifier = Modifier
                    .offset(y = offsetY1.dp)
                    .alpha(alpha1),
                contentAlignment = Alignment.Center
            ) {
                ProfileAvatarView(
                    avatarType = gender.toAvatarType()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Heading: "Let's set up your profile" (Sora 800/900 + Dusty Teal)
            Text(
                text = "Let's set up your profile",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Black,
                fontSize = if (isCompact) 21.sp else 23.sp,
                lineHeight = if (isCompact) 27.sp else 29.sp,
                letterSpacing = (-0.4).sp,
                color = TextTitle,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = offsetY2.dp)
                    .alpha(alpha2)
                    .testTag("profile_setup_heading")
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Subheading: "A few details so your family knows who's who."
            Text(
                text = "A few details so your family knows who's who.",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = if (isCompact) 12.5.sp else 13.sp,
                lineHeight = 18.sp,
                color = TextNeutral,
                textAlign = TextAlign.Center,
                letterSpacing = 0.1.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = offsetY2.dp)
                    .alpha(alpha2)
                    .testTag("profile_setup_subheading")
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Required Profile Form Fields
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = offsetY3.dp)
                    .alpha(alpha3)
            ) {
                // Name Field
                ProfileNameInput(
                    value = name,
                    onValueChange = onNameChange
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Gender Selection (Male / Female / Prefer not to say)
                GenderSelectionChips(
                    selectedGender = gender,
                    onGenderSelected = onGenderChange
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Draggable Age Slider
                AgeSliderSection(
                    age = age,
                    onAgeChange = onAgeChange
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 4. Optional About Me Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = offsetY4.dp)
                    .alpha(alpha4)
            ) {
                AboutMeSection(
                    isExpanded = isAboutMeExpanded,
                    onExpandedChange = onAboutMeExpandedChange,
                    aboutMeText = aboutMeText,
                    onAboutMeChange = onAboutMeTextChange
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            // 5. Primary "Continue" Action Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = offsetY4.dp)
                    .alpha(alpha4)
            ) {
                ProfileContinueButton(
                    onClick = onContinueClick,
                    isEnabled = isFormValid
                )
            }
        }
    }
}
