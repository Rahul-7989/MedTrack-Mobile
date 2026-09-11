package com.example.ui.hub.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.auth.FirebaseAuthService
import com.example.ui.hub.data.FamilyHubRepository
import com.example.ui.profilesetup.components.ProfileAvatarView
import com.example.ui.profilesetup.data.UserProfileRepository
import com.example.ui.profilesetup.model.ProfileAvatarType
import com.example.ui.profilesetup.model.toAvatarType
import com.example.ui.theme.SoraFontFamily

// Scoped Top Bar & Menu Colors
private val ColorWarmIvory = Color(0xFFFAF4EC)
private val ColorWarmCream = Color(0xFFF3E6D5)
private val ColorWarmCreamHover = Color(0xFFEFE0CD)
private val ColorDarkWarmText = Color(0xFF514A44)
private val ColorTextMuted = Color(0xFF665F58)
private val ColorBorderWarm = Color(0xFFE4D5C2)
private val ColorDustyTeal = Color(0xFF72B5BA)
private val ColorBurntApricot = Color(0xFFD88B3D)
private val ColorAmbientShadow = Color(0x149C876E)
private val ColorSpotShadow = Color(0x1E786550)

/**
 * Clean, consistent top area for Hub Selection, Create Family Hub, and Join Family Hub.
 *
 * Top-left:
 * - Contextual Back button (when showBackButton is true) returning strictly to Hub Selection.
 *
 * Top-right:
 * - Compact Profile Avatar Icon opening a fast, subtle dropdown menu with "My profile" and "Logout".
 */
@Composable
fun HubTopBar(
    showBackButton: Boolean = false,
    onNavigateBack: () -> Unit = {},
    onMyProfileClick: (() -> Unit)? = null,
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val userProfile by UserProfileRepository.userProfile.collectAsState()
    val avatarType = userProfile?.avatarType
        ?: (userProfile?.gender?.toAvatarType() ?: ProfileAvatarType.ANONYMOUS)

    var isMenuExpanded by remember { mutableStateOf(false) }
    var isProfileDialogOpen by remember { mutableStateOf(false) }

    fun handleLogout() {
        isMenuExpanded = false
        // 1. Sign out from Firebase Auth
        FirebaseAuthService.Instance.signOut()
        // 2. Clear local profile state
        UserProfileRepository.clearProfile()
        // 3. Clear local hub state
        FamilyHubRepository.clearHub()
        // 4. Redirect to Home Page
        onLogout()
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("hub_top_bar"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. Top-Left: Contextual Back Button
        if (showBackButton) {
            HubBackButton(
                onClick = onNavigateBack,
                modifier = Modifier.testTag("hub_back_button")
            )
        } else {
            Spacer(modifier = Modifier.size(40.dp))
        }

        // 2. Top-Right: Profile Avatar Icon + Account Menu
        Box(contentAlignment = Alignment.TopEnd) {
            HubProfileAvatarButton(
                avatarType = avatarType,
                onClick = { isMenuExpanded = !isMenuExpanded },
                modifier = Modifier.testTag("hub_profile_icon_button")
            )

            // Dropdown Menu
            HubAccountMenu(
                expanded = isMenuExpanded,
                onDismiss = { isMenuExpanded = false },
                onMyProfileClick = {
                    isMenuExpanded = false
                    if (onMyProfileClick != null) {
                        onMyProfileClick()
                    } else {
                        isProfileDialogOpen = true
                    }
                },
                onLogoutClick = ::handleLogout
            )
        }
    }

    if (isProfileDialogOpen) {
        MyProfileDialog(
            userProfile = userProfile,
            onDismiss = { isProfileDialogOpen = false },
            onLogout = ::handleLogout
        )
    }
}

/**
 * Compact, tactile back button with soft elevated MedTrack style.
 * Contextual back action returning specifically to Hub Selection.
 */
@Composable
fun HubBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()

    val easeOutCurve = remember { CubicBezierEasing(0.25f, 0.1f, 0.25f, 1.0f) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = tween(150, easing = easeOutCurve),
        label = "HubBackScale"
    )

    val elevation by animateDpAsState(
        targetValue = if (isHovered) 4.dp else 2.dp,
        animationSpec = tween(200, easing = easeOutCurve),
        label = "HubBackElevation"
    )

    val shape = RoundedCornerShape(14.dp)

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = ColorAmbientShadow,
                spotColor = ColorSpotShadow
            )
            .clip(shape)
            .background(if (isHovered) ColorWarmCreamHover else ColorWarmCream)
            .border(BorderStroke(1.dp, ColorBorderWarm), shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .semantics {
                contentDescription = "Back"
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = null,
                tint = ColorDustyTeal,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Compact circular profile avatar button for the top-right corner.
 * Uses the user's gender-based avatar inside an elevated container.
 */
@Composable
fun HubProfileAvatarButton(
    avatarType: ProfileAvatarType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()

    val easeOutCurve = remember { CubicBezierEasing(0.25f, 0.1f, 0.25f, 1.0f) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = tween(150, easing = easeOutCurve),
        label = "HubProfileScale"
    )

    val elevation by animateDpAsState(
        targetValue = if (isHovered) 4.dp else 2.5.dp,
        animationSpec = tween(200, easing = easeOutCurve),
        label = "HubProfileElevation"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = elevation,
                shape = CircleShape,
                ambientColor = ColorAmbientShadow,
                spotColor = ColorSpotShadow
            )
            .clip(CircleShape)
            .background(ColorWarmCream)
            .border(BorderStroke(1.2.dp, ColorBorderWarm), CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick
            )
            .padding(2.dp)
            .semantics {
                contentDescription = "Account Profile Menu"
            },
        contentAlignment = Alignment.Center
    ) {
        ProfileAvatarView(
            avatarType = avatarType,
            size = 36.dp
        )
    }
}

/**
 * Dropdown Menu for Profile and Logout with subtle fade & scale animation (180-220ms).
 */
@Composable
fun HubAccountMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onMyProfileClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val menuShape = RoundedCornerShape(16.dp)

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        offset = DpOffset(x = 0.dp, y = 8.dp),
        shape = menuShape,
        containerColor = ColorWarmCream,
        border = BorderStroke(1.dp, ColorBorderWarm),
        shadowElevation = 8.dp,
        modifier = modifier
            .width(180.dp)
            .testTag("hub_profile_dropdown_menu")
    ) {
        // 1. "My profile" Option (Presently non-functional)
        DropdownMenuItem(
            text = {
                Text(
                    text = "My profile",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.5.sp,
                    color = ColorDarkWarmText
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    tint = ColorDustyTeal,
                    modifier = Modifier.size(18.dp)
                )
            },
            onClick = onMyProfileClick,
            colors = MenuDefaults.itemColors(
                textColor = ColorDarkWarmText,
                leadingIconColor = ColorDustyTeal
            ),
            modifier = Modifier.testTag("menu_item_my_profile")
        )

        HorizontalDivider(
            color = ColorBorderWarm.copy(alpha = 0.6f),
            thickness = 1.dp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        // 2. "Logout" Option (Functional -> signs out and navigates to Home)
        DropdownMenuItem(
            text = {
                Text(
                    text = "Logout",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.5.sp,
                    color = ColorBurntApricot
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Logout,
                    contentDescription = null,
                    tint = ColorBurntApricot,
                    modifier = Modifier.size(18.dp)
                )
            },
            onClick = onLogoutClick,
            colors = MenuDefaults.itemColors(
                textColor = ColorBurntApricot,
                leadingIconColor = ColorBurntApricot
            ),
            modifier = Modifier.testTag("menu_item_logout")
        )
    }
}
