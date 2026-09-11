package com.example.ui.forgotpassword

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.auth.AuthResult
import com.example.data.auth.FirebaseAuthService
import com.example.ui.forgotpassword.components.ForgotPasswordBackButton
import com.example.ui.forgotpassword.components.ForgotPasswordEmailField
import com.example.ui.forgotpassword.components.ForgotPasswordPrimaryButton
import com.example.ui.login.components.LoginBackgroundShapes
import com.example.ui.theme.DarkWarmText
import com.example.ui.theme.DustyTeal
import com.example.ui.theme.SoraFontFamily
import com.example.ui.theme.WarmCream
import com.example.ui.theme.WarmIvory
import com.example.ui.theme.WarmNeutral
import kotlinx.coroutines.launch

private val PanelSurface = WarmCream // #F3E6D5
private val PanelBorder = Color(0xFFE4D5C2)
private val ShadowWarmAmbient = Color(0x189C876E)
private val ShadowWarmSpot = Color(0x22786550)

/**
 * Independent modular Forgot Password Screen for MedTrack.
 *
 * Allows an existing user to request a Firebase Authentication password-reset email.
 * Adheres strictly to MedTrack design language, Sora typography, and warm palette.
 */
@Composable
fun ForgotPasswordScreen(
    initialEmail: String = "",
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var uiState by remember {
        mutableStateOf(
            ForgotPasswordUiState(
                email = initialEmail.trim()
            )
        )
    }

    fun submitResetRequest() {
        focusManager.clearFocus()
        val emailInput = uiState.email.trim()

        if (emailInput.isEmpty()) {
            uiState = uiState.copy(errorMessage = "Please enter your email address.")
            return
        }

        if (!ForgotPasswordUiState.isValidEmailFormat(emailInput)) {
            uiState = uiState.copy(errorMessage = "Please enter a valid email address.")
            return
        }

        uiState = uiState.copy(
            isLoading = true,
            errorMessage = null
        )

        coroutineScope.launch {
            val result = FirebaseAuthService.Instance.sendPasswordReset(emailInput)
            when (result) {
                is AuthResult.Success -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        isSuccess = true,
                        submittedEmail = emailInput,
                        errorMessage = null
                    )
                }
                is AuthResult.Error -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(WarmIvory)
            .systemBarsPadding()
            .imePadding()
            .testTag("forgot_password_screen")
    ) {
        val screenHeight = maxHeight
        val isCompact = maxWidth < 600.dp
        val isDesktopOrWide = maxWidth >= 840.dp
        val scrollState = rememberScrollState()

        val horizontalPadding = if (isDesktopOrWide) 32.dp else if (isCompact) 20.dp else 24.dp
        val verticalPadding = if (isCompact) 20.dp else 32.dp

        // Atmospheric background geometric art consistent with login
        LoginBackgroundShapes(
            modifier = Modifier.fillMaxSize(),
            alpha = 0.85f,
            isReducedMotion = false
        )

        // Main scrollable container properly centering the content card
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(
                    horizontal = horizontalPadding,
                    vertical = verticalPadding
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .defaultMinSize(minHeight = (screenHeight - verticalPadding * 2).coerceAtLeast(0.dp))
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Section container: "← Login" button sits alongside / just above the card
                Column(
                    modifier = Modifier
                        .widthIn(max = 440.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    ForgotPasswordBackButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.padding(bottom = if (isCompact) 14.dp else 18.dp)
                    )

                    // Centered Floating Card Panel
                    val cornerRadius = 28.dp
                    val panelShape = RoundedCornerShape(cornerRadius)
                    val cardHorizontalPadding = if (isCompact) 22.dp else 30.dp
                    val cardVerticalPadding = if (isCompact) 26.dp else 34.dp

                    Box(
                        modifier = Modifier
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
                                width = 1.dp,
                                color = PanelBorder,
                                shape = panelShape
                            )
                            .padding(horizontal = cardHorizontalPadding, vertical = cardVerticalPadding)
                            .testTag("forgot_password_panel"),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(
                            targetState = uiState.isSuccess,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(300)) togetherWith
                                    fadeOut(animationSpec = tween(200))
                            },
                            label = "ForgotPasswordContentTransition"
                        ) { isSuccess ->
                            if (!isSuccess) {
                                // Form Content
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Heading: “Forgot your password?”
                                    Text(
                                        text = "Forgot your password?",
                                        fontFamily = SoraFontFamily,
                                        fontWeight = FontWeight.Black,
                                        fontSize = if (isCompact) 21.sp else 23.sp,
                                        lineHeight = if (isCompact) 27.sp else 29.sp,
                                        letterSpacing = (-0.45).sp,
                                        color = DustyTeal,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("forgot_password_heading")
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Supporting text: “Enter your email and we’ll send you a link to reset your password.”
                                    Text(
                                        text = "Enter your email and we’ll send you a link to reset your password.",
                                        fontFamily = SoraFontFamily,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = if (isCompact) 12.5.sp else 13.sp,
                                        lineHeight = 18.sp,
                                        color = WarmNeutral,
                                        textAlign = TextAlign.Center,
                                        letterSpacing = 0.1.sp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("forgot_password_supporting_text")
                                    )

                                    Spacer(modifier = Modifier.height(20.dp))

                                    // Warning / Error Banner placed directly ABOVE email field
                                    AnimatedVisibility(
                                        visible = uiState.errorMessage != null,
                                        enter = fadeIn(animationSpec = tween(200)),
                                        exit = fadeOut(animationSpec = tween(150))
                                    ) {
                                        val errorMsg = uiState.errorMessage
                                        if (!errorMsg.isNullOrBlank()) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(bottom = 14.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(Color(0xFFFAF2EE)) // Light warm tint background
                                                    .border(BorderStroke(1.dp, Color(0xFFE8CBC2)), RoundedCornerShape(12.dp)) // Soft warm border
                                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                                                    .testTag("forgot_password_inline_error"),
                                                contentAlignment = Alignment.CenterStart
                                            ) {
                                                Text(
                                                    text = errorMsg,
                                                    fontFamily = SoraFontFamily,
                                                    fontWeight = FontWeight.Medium,
                                                    fontSize = 14.sp,
                                                    lineHeight = 19.sp,
                                                    color = Color(0xFFC8755D) // Muted Terracotta #C8755D
                                                )
                                            }
                                        }
                                    }

                                    // Email input field with pre-fill and live inline error clearing
                                    ForgotPasswordEmailField(
                                        value = uiState.email,
                                        onValueChange = { newEmail ->
                                            uiState = uiState.copy(
                                                email = newEmail,
                                                errorMessage = null
                                            )
                                        },
                                        errorMessage = null,
                                        onDone = { submitResetRequest() }
                                    )

                                    Spacer(modifier = Modifier.height(22.dp))

                                    // Primary button: “Get Password Reset Link”
                                    ForgotPasswordPrimaryButton(
                                        text = "Get Password Reset Link",
                                        onClick = { submitResetRequest() },
                                        isLoading = uiState.isLoading,
                                        enabled = !uiState.isLoading,
                                        testTag = "forgot_password_submit_button"
                                    )
                                }
                            } else {
                                // Success State
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("forgot_password_success_view"),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Exact message: “We sent you a password change link to <email>”
                                    Text(
                                        text = "We sent you a password change link to ${uiState.submittedEmail}",
                                        fontFamily = SoraFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = if (isCompact) 15.sp else 16.sp,
                                        lineHeight = 23.sp,
                                        color = DarkWarmText,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 4.dp)
                                            .testTag("forgot_password_success_message")
                                    )

                                    Spacer(modifier = Modifier.height(26.dp))

                                    // Button: “Log in” (exact wording as requested)
                                    ForgotPasswordPrimaryButton(
                                        text = "Log in",
                                        onClick = onNavigateToLogin,
                                        isLoading = false,
                                        enabled = true,
                                        testTag = "forgot_password_login_in_button"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
