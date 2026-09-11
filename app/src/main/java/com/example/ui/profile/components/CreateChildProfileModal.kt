package com.example.ui.profile.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.hub.dashboard.model.HubMember
import com.example.ui.profilesetup.components.ProfileAvatarView
import com.example.ui.profilesetup.model.ProfileGender
import com.example.ui.profilesetup.model.toChildAvatarType
import com.example.ui.theme.DarkWarmText
import com.example.ui.theme.DustyTeal
import com.example.ui.theme.SoraFontFamily
import com.example.ui.theme.WarmAmber
import com.example.ui.theme.WarmCream
import com.example.ui.theme.WarmIvory
import com.example.ui.theme.WarmNeutral

private val ColorWarmCream = WarmCream
private val ColorWarmIvory = WarmIvory
private val ColorDarkWarmText = DarkWarmText
private val ColorWarmNeutral = WarmNeutral
private val ColorDustyTeal = DustyTeal
private val ColorWarmAmber = WarmAmber
private val ColorBorderWarm = Color(0xFFE4D5C2)
private val ColorAmbientShadow = Color(0x129C876E)
private val ColorSpotShadow = Color(0x18786550)

/**
 * Modal Bottom Sheet for creating a dependent Child Profile.
 * Children are dependents without independent authentication credentials.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateChildProfileModal(
    hubId: String,
    hubName: String,
    approvedMembers: List<HubMember>,
    onDismiss: () -> Unit,
    onCreateChild: (name: String, gender: ProfileGender, reminderMemberId: String, reminderMemberName: String) -> Unit,
    isSubmitting: Boolean = false,
    errorMessage: String? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var childName by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf(ProfileGender.MALE) }

    // "Who to remind" adult member selection
    val adultMembers = approvedMembers.filter { !it.isChild }
    var selectedReminderMember by remember {
        mutableStateOf(adultMembers.firstOrNull())
    }
    var isMemberDropdownOpen by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ColorWarmIvory,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.testTag("create_child_profile_modal")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Modal Header: Title + Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "NEW DEPENDENT",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.5.sp,
                        letterSpacing = 1.4.sp,
                        color = ColorDustyTeal
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Create Child Profile",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 19.sp,
                        color = ColorDarkWarmText
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(ColorWarmCream)
                        .border(BorderStroke(1.dp, ColorBorderWarm), CircleShape)
                        .testTag("close_child_modal_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Close",
                        tint = ColorDarkWarmText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Dynamic Child Avatar Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(ColorWarmCream)
                    .border(BorderStroke(1.2.dp, ColorBorderWarm), RoundedCornerShape(20.dp))
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    ProfileAvatarView(
                        avatarType = selectedGender.toChildAvatarType(),
                        size = 64.dp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (childName.isNotBlank()) childName else "Child Avatar Preview",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = ColorDarkWarmText
                    )
                    Text(
                        text = "Belongs to $hubName",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.sp,
                        color = ColorWarmNeutral
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Field 1: Child Name
            Text(
                text = "CHILD'S NAME",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 10.5.sp,
                letterSpacing = 1.2.sp,
                color = ColorWarmNeutral
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = childName,
                onValueChange = {
                    childName = it
                    if (validationError != null) validationError = null
                },
                placeholder = {
                    Text(
                        text = "e.g. Liam, Sophia",
                        fontFamily = SoraFontFamily,
                        fontSize = 14.sp,
                        color = ColorWarmNeutral.copy(alpha = 0.6f)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = ColorWarmIvory,
                    unfocusedContainerColor = ColorWarmIvory,
                    focusedBorderColor = ColorDustyTeal,
                    unfocusedBorderColor = ColorBorderWarm,
                    focusedTextColor = ColorDarkWarmText,
                    unfocusedTextColor = ColorDarkWarmText
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("child_name_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Field 2: Gender Selector (Male, Female, Prefer not to say)
            Text(
                text = "GENDER",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 10.5.sp,
                letterSpacing = 1.2.sp,
                color = ColorWarmNeutral
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProfileGender.entries.forEach { gender ->
                    val isSelected = selectedGender == gender
                    val label = when (gender) {
                        ProfileGender.MALE -> "Boy"
                        ProfileGender.FEMALE -> "Girl"
                        ProfileGender.PREFER_NOT_TO_SAY -> "Prefer not to say"
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) ColorDustyTeal else ColorWarmCream)
                            .border(
                                BorderStroke(
                                    1.2.dp,
                                    if (isSelected) ColorDustyTeal else ColorBorderWarm
                                ),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedGender = gender }
                            .padding(vertical = 10.dp, horizontal = 6.dp)
                            .semantics { contentDescription = "Gender $label" }
                            .testTag("child_gender_${gender.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontFamily = SoraFontFamily,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            fontSize = 11.5.sp,
                            color = if (isSelected) ColorWarmIvory else ColorDarkWarmText,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Field 3: "Who to remind" Dropdown (Approved Hub Members only)
            Text(
                text = "WHO TO REMIND",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 10.5.sp,
                letterSpacing = 1.2.sp,
                color = ColorWarmNeutral
            )
            Spacer(modifier = Modifier.height(6.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(ColorWarmIvory)
                        .border(BorderStroke(1.2.dp, ColorBorderWarm), RoundedCornerShape(14.dp))
                        .clickable(
                            role = Role.Button,
                            onClick = { isMemberDropdownOpen = true }
                        )
                        .padding(horizontal = 14.dp, vertical = 11.dp)
                        .testTag("child_who_to_remind_selector"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (selectedReminderMember != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ProfileAvatarView(
                                avatarType = selectedReminderMember!!.avatarType,
                                size = 28.dp
                            )
                            Column {
                                Text(
                                    text = selectedReminderMember!!.name,
                                    fontFamily = SoraFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    color = ColorDarkWarmText
                                )
                                Text(
                                    text = if (selectedReminderMember!!.isCreator) "Hub Creator" else "Adult Member",
                                    fontFamily = SoraFontFamily,
                                    fontSize = 10.sp,
                                    color = ColorWarmNeutral
                                )
                            }
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = null,
                                tint = ColorWarmNeutral,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Select adult member to remind",
                                fontFamily = SoraFontFamily,
                                fontSize = 13.5.sp,
                                color = ColorWarmNeutral
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Outlined.ArrowDropDown,
                        contentDescription = "Open dropdown",
                        tint = ColorDarkWarmText,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Dropdown of approved adult members
                DropdownMenu(
                    expanded = isMemberDropdownOpen,
                    onDismissRequest = { isMemberDropdownOpen = false },
                    shape = RoundedCornerShape(16.dp),
                    containerColor = ColorWarmIvory,
                    tonalElevation = 0.dp,
                    shadowElevation = 6.dp,
                    border = BorderStroke(1.2.dp, ColorBorderWarm),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    if (adultMembers.isEmpty()) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "No adult members found",
                                    fontFamily = SoraFontFamily,
                                    fontSize = 13.sp,
                                    color = ColorWarmNeutral
                                )
                            },
                            onClick = { isMemberDropdownOpen = false }
                        )
                    } else {
                        adultMembers.forEach { member ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        ProfileAvatarView(
                                            avatarType = member.avatarType,
                                            size = 28.dp
                                        )
                                        Column {
                                            Text(
                                                text = member.name,
                                                fontFamily = SoraFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.5.sp,
                                                color = ColorDarkWarmText
                                            )
                                            Text(
                                                text = if (member.isCreator) "Hub Creator" else "Adult Member",
                                                fontFamily = SoraFontFamily,
                                                fontSize = 10.sp,
                                                color = ColorWarmNeutral
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    selectedReminderMember = member
                                    isMemberDropdownOpen = false
                                },
                                modifier = Modifier.testTag("remind_member_option_${member.id}")
                            )
                        }
                    }
                }
            }

            // Validation or Server Error Message
            val activeError = validationError ?: errorMessage
            if (!activeError.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = activeError,
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = Color(0xFFC85A54),
                    modifier = Modifier.testTag("child_modal_error_text")
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Button: "Create Child User"
            Button(
                onClick = {
                    if (childName.isBlank()) {
                        validationError = "Please enter the child's name."
                        return@Button
                    }
                    if (selectedReminderMember == null) {
                        validationError = "Please select who should receive reminders for this child."
                        return@Button
                    }
                    onCreateChild(
                        childName.trim(),
                        selectedGender,
                        selectedReminderMember!!.id,
                        selectedReminderMember!!.name
                    )
                },
                enabled = !isSubmitting,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorWarmAmber,
                    contentColor = ColorDarkWarmText,
                    disabledContainerColor = ColorWarmAmber.copy(alpha = 0.6f),
                    disabledContentColor = ColorDarkWarmText.copy(alpha = 0.6f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .shadow(
                        elevation = 3.dp,
                        shape = RoundedCornerShape(16.dp),
                        ambientColor = ColorAmbientShadow,
                        spotColor = ColorSpotShadow
                    )
                    .testTag("create_child_user_submit_button")
            ) {
                if (isSubmitting) {
                    ThreeDotsLoadingAnimation()
                } else {
                    Text(
                        text = "Create Child User",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = ColorDarkWarmText
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

/**
 * Modern pulsing three-dots loading indicator.
 */
@Composable
private fun ThreeDotsLoadingAnimation() {
    val transition = rememberInfiniteTransition(label = "ThreeDots")
    val dot1Offset by transition.animateFloat(
        initialValue = 0f,
        targetValue = -5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 350),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Dot1"
    )
    val dot2Offset by transition.animateFloat(
        initialValue = 0f,
        targetValue = -5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 350, delayMillis = 120),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Dot2"
    )
    val dot3Offset by transition.animateFloat(
        initialValue = 0f,
        targetValue = -5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 350, delayMillis = 240),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Dot3"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .offset(y = dot1Offset.dp)
                .size(7.dp)
                .clip(CircleShape)
                .background(ColorDarkWarmText)
        )
        Box(
            modifier = Modifier
                .offset(y = dot2Offset.dp)
                .size(7.dp)
                .clip(CircleShape)
                .background(ColorDarkWarmText)
        )
        Box(
            modifier = Modifier
                .offset(y = dot3Offset.dp)
                .size(7.dp)
                .clip(CircleShape)
                .background(ColorDarkWarmText)
        )
    }
}
