package com.example.ui.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.auth.AuthErrorType
import com.example.data.auth.AuthResult
import com.example.data.auth.FirebaseAuthService
import com.example.ui.hub.data.FamilyHubRepository
import com.example.ui.profilesetup.data.UserProfileRepository
import com.example.ui.navigation.MedTrackDestinations
import com.example.ui.navigation.UserSessionRouter
import com.example.ui.login.components.ForgotPasswordLink
import com.example.ui.login.components.LoginBackgroundShapes
import com.example.ui.login.components.LoginEditorialArtwork
import com.example.ui.login.components.LoginEmailField
import com.example.ui.login.components.LoginGetStartedFooter
import com.example.ui.login.components.LoginPanel
import com.example.ui.login.components.LoginPasswordField
import com.example.ui.login.components.LoginPrimaryButton
import com.example.ui.login.components.ReturnHomeButton
import com.example.ui.theme.SoraFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Scoped page background color
private val ColorWarmIvory = Color(0xFFFAF4EC)
private val ColorTextPrimary = Color(0xFF232B2B)
private val ColorDustyTeal = Color(0xFF72B5BA)

/**
 * MedTrack Login Page.
 *
 * Designed as a completely isolated, modular screen for the family medication companion.
 * Features an editorial asymmetrical composition on desktop, warm geometric background art,
 * a tactile Warm Cream floating login panel, and staggered entrance animations.
 * Contains ZERO logos, clinical healthcare cliches, or marketing clutter.
 */
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    onNavigateToGetStarted: () -> Unit = {},
    onNavigateToForgotPassword: (String) -> Unit = {},
    onLoginSuccess: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    // Detect system preference for reduced motion
    val isReducedMotion = remember {
        try {
            val resolver = context.contentResolver
            val transitionScale = android.provider.Settings.Global.getFloat(
                resolver,
                android.provider.Settings.Global.TRANSITION_ANIMATION_SCALE,
                1.0f
            )
            val animatorScale = android.provider.Settings.Global.getFloat(
                resolver,
                android.provider.Settings.Global.ANIMATOR_DURATION_SCALE,
                1.0f
            )
            transitionScale == 0f || animatorScale == 0f
        } catch (_: Exception) {
            false
        }
    }

    // Isolated login state
    var uiState by remember { mutableStateOf(LoginUiState()) }

    // Staggered initial entrance animation triggers
    var startAnimations by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        startAnimations = true
    }

    val easeOutCurve = remember { CubicBezierEasing(0.22f, 0.1f, 0.25f, 1.0f) }

    // 1. Background shapes fade in (0ms)
    val bgAlpha by animateFloatAsState(
        targetValue = if (startAnimations) 1f else 0f,
        animationSpec = if (isReducedMotion) tween(0) else tween(durationMillis = 700, easing = easeOutCurve),
        label = "LoginBgAlpha"
    )

    // 2. Decorative artwork on opposite side (60ms delay)
    val artAlpha by animateFloatAsState(
        targetValue = if (startAnimations) 1f else 0f,
        animationSpec = if (isReducedMotion) tween(0) else tween(durationMillis = 650, delayMillis = 60, easing = easeOutCurve),
        label = "LoginArtAlpha"
    )

    // 3. Login panel fades in while moving upward ~12px (120ms delay)
    val panelAlpha by animateFloatAsState(
        targetValue = if (startAnimations) 1f else 0f,
        animationSpec = if (isReducedMotion) tween(0) else tween(durationMillis = 650, delayMillis = 120, easing = easeOutCurve),
        label = "LoginPanelAlpha"
    )
    val panelOffsetY by animateFloatAsState(
        targetValue = if (startAnimations) 0f else 12f,
        animationSpec = if (isReducedMotion) tween(0) else tween(durationMillis = 650, delayMillis = 120, easing = easeOutCurve),
        label = "LoginPanelOffset"
    )

    // 4. "Welcome back" & subtitle entrance animation (opacity 0 -> 1, translateY 8dp -> 0dp, ~400ms)
    val headerAlpha by animateFloatAsState(
        targetValue = if (startAnimations) 1f else 0f,
        animationSpec = if (isReducedMotion) tween(0) else tween(durationMillis = 400, delayMillis = 180, easing = easeOutCurve),
        label = "LoginHeaderAlpha"
    )
    val headerOffsetY by animateDpAsState(
        targetValue = if (startAnimations) 0.dp else 8.dp,
        animationSpec = if (isReducedMotion) tween(0) else tween(durationMillis = 400, delayMillis = 180, easing = easeOutCurve),
        label = "LoginHeaderOffsetY"
    )

    // 5. Email field (300ms delay)
    val emailAlpha by animateFloatAsState(
        targetValue = if (startAnimations) 1f else 0f,
        animationSpec = if (isReducedMotion) tween(0) else tween(durationMillis = 550, delayMillis = 300, easing = easeOutCurve),
        label = "LoginEmailAlpha"
    )

    // 6. Password field (360ms delay)
    val passwordAlpha by animateFloatAsState(
        targetValue = if (startAnimations) 1f else 0f,
        animationSpec = if (isReducedMotion) tween(0) else tween(durationMillis = 550, delayMillis = 360, easing = easeOutCurve),
        label = "LoginPwAlpha"
    )

    // 7. Forgot password link (420ms delay)
    val forgotAlpha by animateFloatAsState(
        targetValue = if (startAnimations) 1f else 0f,
        animationSpec = if (isReducedMotion) tween(0) else tween(durationMillis = 500, delayMillis = 420, easing = easeOutCurve),
        label = "LoginForgotAlpha"
    )

    // 8. Log in button (480ms delay)
    val buttonAlpha by animateFloatAsState(
        targetValue = if (startAnimations) 1f else 0f,
        animationSpec = if (isReducedMotion) tween(0) else tween(durationMillis = 500, delayMillis = 480, easing = easeOutCurve),
        label = "LoginBtnAlpha"
    )

    // 9. Get Started footer (540ms delay)
    val footerAlpha by animateFloatAsState(
        targetValue = if (startAnimations) 1f else 0f,
        animationSpec = if (isReducedMotion) tween(0) else tween(durationMillis = 500, delayMillis = 540, easing = easeOutCurve),
        label = "LoginFooterAlpha"
    )

    // Form submission validation logic
    fun submitLogin() {
        focusManager.clearFocus()
        var hasError = false
        var emailErr: String? = null
        var passwordErr: String? = null

        if (uiState.email.isBlank()) {
            emailErr = "Please enter your email"
            hasError = true
        } else if (!uiState.email.contains("@") || !uiState.email.contains(".")) {
            emailErr = "Please enter a valid email address"
            hasError = true
        }

        if (uiState.password.isBlank()) {
            passwordErr = "Please enter your password"
            hasError = true
        } else if (uiState.password.length < 6) {
            passwordErr = "Password must be at least 6 characters"
            hasError = true
        }

        if (hasError) {
            uiState = uiState.copy(
                emailError = emailErr,
                passwordError = passwordErr,
                generalError = null
            )
            return
        }

        // Clean valid state
        uiState = uiState.copy(
            emailError = null,
            passwordError = null,
            generalError = null,
            isLoading = true
        )

        coroutineScope.launch {
            val result = FirebaseAuthService.Instance.login(uiState.email, uiState.password)
            when (result) {
                is AuthResult.Success -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        isSuccess = true,
                        inlineError = null,
                        generalError = null
                    )
                    // Seamlessly show MedTrack splash screen + message to initialize and transition smoothly
                    onLoginSuccess(MedTrackDestinations.SPLASH)
                }
                is AuthResult.Error -> {
                    val inlineErr = when (result.errorType) {
                        AuthErrorType.EMAIL_NOT_REGISTERED -> LoginInlineError(
                            title = "Email isn't registered.",
                            supporting = "Please check your email or create a new account.",
                            type = LoginErrorType.EMAIL_NOT_REGISTERED
                        )
                        AuthErrorType.WRONG_PASSWORD -> LoginInlineError(
                            title = "Password is incorrect.",
                            supporting = "Please check your password and try again.",
                            type = LoginErrorType.WRONG_PASSWORD
                        )
                        else -> LoginInlineError(
                            title = result.message,
                            supporting = null,
                            type = LoginErrorType.GENERAL
                        )
                    }
                    uiState = uiState.copy(
                        isLoading = false,
                        inlineError = inlineErr,
                        generalError = null
                    )
                }
            }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(ColorWarmIvory),
        containerColor = ColorWarmIvory,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .systemBarsPadding()
                .imePadding()
        ) {
            val screenWidth = maxWidth
            val screenHeight = maxHeight
            val isDesktopOrWide = screenWidth >= 800.dp
            val isTablet = screenWidth in 600.dp..799.dp
            val isCompact = screenWidth < 600.dp

            // 1. Subtle, slow floating geometric background (respects reduced-motion)
            LoginBackgroundShapes(
                alpha = bgAlpha,
                isReducedMotion = isReducedMotion
            )

            // Main Content Area with responsive asymmetrical desktop vs centered mobile layout
            val scrollState = rememberScrollState()

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
                contentAlignment = Alignment.Center
            ) {
                if (isDesktopOrWide) {
                    // DESKTOP ASYMMETRICAL EDITORIAL COMPOSITION:
                    // Left: Abstract geometric visual elements (zero text, suggesting connection)
                    // Right: Soft tactile floating Login Panel
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 1100.dp)
                            .padding(horizontal = 48.dp, vertical = 32.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        ReturnHomeButton(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .alpha(panelAlpha)
                                .padding(bottom = 24.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left decorative art side (no text, pure geometry and warm palette)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(480.dp)
                                    .alpha(artAlpha),
                                contentAlignment = Alignment.Center
                            ) {
                                LoginEditorialArtwork(
                                    modifier = Modifier.fillMaxSize(),
                                    alpha = artAlpha,
                                    isReducedMotion = isReducedMotion
                                )
                            }

                            Spacer(modifier = Modifier.width(48.dp))

                            // Right floating login panel
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .alpha(panelAlpha)
                                    .offset(y = panelOffsetY.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                LoginFormContent(
                                    uiState = uiState,
                                    onEmailChange = {
                                        val shouldClearInline = uiState.inlineError?.type == LoginErrorType.EMAIL_NOT_REGISTERED || uiState.inlineError?.type == LoginErrorType.GENERAL
                                        uiState = uiState.copy(
                                            email = it,
                                            emailError = null,
                                            inlineError = if (shouldClearInline) null else uiState.inlineError,
                                            generalError = null
                                        )
                                    },
                                    onPasswordChange = {
                                        val shouldClearInline = uiState.inlineError?.type == LoginErrorType.WRONG_PASSWORD || uiState.inlineError?.type == LoginErrorType.GENERAL
                                        uiState = uiState.copy(
                                            password = it,
                                            passwordError = null,
                                            inlineError = if (shouldClearInline) null else uiState.inlineError,
                                            generalError = null
                                        )
                                    },
                                    onTogglePasswordVisibility = {
                                        uiState = uiState.copy(isPasswordVisible = !uiState.isPasswordVisible)
                                    },
                                    onForgotPasswordClick = {
                                        onNavigateToForgotPassword(uiState.email.trim())
                                    },
                                    onSubmit = ::submitLogin,
                                    onGetStartedClick = onNavigateToGetStarted,
                                    isCompact = false,
                                    headerAlpha = headerAlpha,
                                    headerOffsetY = headerOffsetY,
                                    emailAlpha = emailAlpha,
                                    passwordAlpha = passwordAlpha,
                                    forgotAlpha = forgotAlpha,
                                    buttonAlpha = buttonAlpha,
                                    footerAlpha = footerAlpha,
                                    onNextFocus = { focusManager.moveFocus(FocusDirection.Down) }
                                )
                            }
                        }
                    }
                } else {
                    // TABLET & MOBILE COMPOSITION:
                    // Clear vertical separation between top-left "← Home" button and Login card
                    val verticalPadding = if (isCompact) 20.dp else 28.dp
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = (screenHeight - verticalPadding * 2).coerceAtLeast(0.dp))
                            .padding(
                                horizontal = if (isCompact) 20.dp else 40.dp,
                                vertical = verticalPadding
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Column(
                            modifier = Modifier
                                .widthIn(max = 440.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.Start
                        ) {
                            ReturnHomeButton(
                                onClick = onNavigateBack,
                                modifier = Modifier
                                    .alpha(panelAlpha)
                                    .padding(bottom = if (isCompact) 18.dp else 24.dp)
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .alpha(panelAlpha)
                                    .offset(y = panelOffsetY.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                LoginFormContent(
                                    uiState = uiState,
                                    onEmailChange = {
                                        val shouldClearInline = uiState.inlineError?.type == LoginErrorType.EMAIL_NOT_REGISTERED || uiState.inlineError?.type == LoginErrorType.GENERAL
                                        uiState = uiState.copy(
                                            email = it,
                                            emailError = null,
                                            inlineError = if (shouldClearInline) null else uiState.inlineError,
                                            generalError = null
                                        )
                                    },
                                    onPasswordChange = {
                                        val shouldClearInline = uiState.inlineError?.type == LoginErrorType.WRONG_PASSWORD || uiState.inlineError?.type == LoginErrorType.GENERAL
                                        uiState = uiState.copy(
                                            password = it,
                                            passwordError = null,
                                            inlineError = if (shouldClearInline) null else uiState.inlineError,
                                            generalError = null
                                        )
                                    },
                                    onTogglePasswordVisibility = {
                                        uiState = uiState.copy(isPasswordVisible = !uiState.isPasswordVisible)
                                    },
                                    onForgotPasswordClick = {
                                        onNavigateToForgotPassword(uiState.email.trim())
                                    },
                                    onSubmit = ::submitLogin,
                                    onGetStartedClick = onNavigateToGetStarted,
                                    isCompact = isCompact,
                                    headerAlpha = headerAlpha,
                                    headerOffsetY = headerOffsetY,
                                    emailAlpha = emailAlpha,
                                    passwordAlpha = passwordAlpha,
                                    forgotAlpha = forgotAlpha,
                                    buttonAlpha = buttonAlpha,
                                    footerAlpha = footerAlpha,
                                    onNextFocus = { focusManager.moveFocus(FocusDirection.Down) }
                                )
                            }
                        }
                    }
                }
            }

            // Accessible "Forgot Password" Modal Dialog in Sora typography
            if (uiState.showForgotPasswordDialog) {
                var resetEmailInput by remember { mutableStateOf(uiState.email) }
                var resetSent by remember { mutableStateOf(false) }

                AlertDialog(
                    onDismissRequest = {
                        uiState = uiState.copy(showForgotPasswordDialog = false)
                    },
                    containerColor = Color(0xFFFAF6F0),
                    title = {
                        Text(
                            text = if (resetSent) "Check your inbox" else "Reset your password",
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = ColorTextPrimary
                        )
                    },
                    text = {
                        Column {
                            if (resetSent) {
                                Text(
                                    text = "We have sent password reset instructions to your family account email.",
                                    fontFamily = SoraFontFamily,
                                    fontSize = 14.sp,
                                    color = Color(0xFF5A6263)
                                )
                            } else {
                                Text(
                                    text = "Enter your family account email address and we'll send a link to reset your password.",
                                    fontFamily = SoraFontFamily,
                                    fontSize = 14.sp,
                                    color = Color(0xFF5A6263),
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                                LoginEmailField(
                                    value = resetEmailInput,
                                    onValueChange = { resetEmailInput = it },
                                    imeAction = ImeAction.Done,
                                    keyboardActions = KeyboardActions(onDone = {
                                        if (resetEmailInput.isNotBlank()) {
                                            resetSent = true
                                            coroutineScope.launch {
                                                FirebaseAuthService.Instance.sendPasswordReset(resetEmailInput)
                                            }
                                        }
                                    })
                                )
                            }
                        }
                    },
                    confirmButton = {
                        if (resetSent) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(0xFFE5A23C))
                                    .clickable {
                                        uiState = uiState.copy(showForgotPasswordDialog = false)
                                    }
                                    .padding(horizontal = 20.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = "Done",
                                    fontFamily = SoraFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(0xFFE5A23C))
                                    .clickable {
                                        if (resetEmailInput.isNotBlank()) {
                                            resetSent = true
                                            coroutineScope.launch {
                                                FirebaseAuthService.Instance.sendPasswordReset(resetEmailInput)
                                            }
                                        }
                                    }
                                    .padding(horizontal = 18.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = "Send Link",
                                    fontFamily = SoraFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                            }
                        }
                    },
                    dismissButton = {
                        if (!resetSent) {
                            Box(
                                modifier = Modifier
                                    .clickable {
                                        uiState = uiState.copy(showForgotPasswordDialog = false)
                                    }
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = "Cancel",
                                    fontFamily = SoraFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = ColorDustyTeal
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(22.dp)
                )
            }
        }
    }
}

/**
 * Encapsulated Form Content rendered within the LoginPanel.
 */
@Composable
private fun LoginFormContent(
    uiState: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onSubmit: () -> Unit,
    onGetStartedClick: () -> Unit,
    isCompact: Boolean,
    headerAlpha: Float,
    headerOffsetY: Dp,
    emailAlpha: Float,
    passwordAlpha: Float,
    forgotAlpha: Float,
    buttonAlpha: Float,
    footerAlpha: Float,
    onNextFocus: () -> Unit
) {
    LoginPanel(
        isCompact = isCompact,
        headerAlpha = headerAlpha,
        headerOffsetY = headerOffsetY
    ) {
        // Inline Error Message placed directly ABOVE email/password fields
        AnimatedVisibility(
            visible = uiState.inlineError != null,
            enter = fadeIn(animationSpec = tween(200)) + slideInVertically(animationSpec = tween(200)) { -6 },
            exit = fadeOut(animationSpec = tween(150))
        ) {
            val error = uiState.inlineError
            if (error != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFAF2EE)) // Light warm tint background
                        .border(BorderStroke(1.dp, Color(0xFFE8CBC2)), RoundedCornerShape(12.dp)) // Soft warm border
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .testTag("login_inline_error"),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Column {
                        Text(
                            text = error.title,
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            lineHeight = 19.sp,
                            color = Color(0xFFC8755D), // Muted Terracotta #C8755D
                            modifier = Modifier.testTag("login_error_text")
                        )
                        if (!error.supporting.isNullOrBlank()) {
                            Text(
                                text = error.supporting,
                                fontFamily = SoraFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 12.5.sp,
                                lineHeight = 17.sp,
                                color = Color(0xFFC8755D).copy(alpha = 0.9f),
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .testTag("login_error_supporting_text")
                            )
                        }
                    }
                }
            }
        }

        // Email Input Field
        Box(modifier = Modifier.alpha(emailAlpha)) {
            LoginEmailField(
                value = uiState.email,
                onValueChange = onEmailChange,
                errorMessage = uiState.emailError,
                isError = uiState.isEmailHighlighted,
                keyboardActions = KeyboardActions(onNext = { onNextFocus() }),
                imeAction = ImeAction.Next
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Password Input Field
        Box(modifier = Modifier.alpha(passwordAlpha)) {
            LoginPasswordField(
                value = uiState.password,
                onValueChange = onPasswordChange,
                isPasswordVisible = uiState.isPasswordVisible,
                onTogglePasswordVisibility = onTogglePasswordVisibility,
                errorMessage = uiState.passwordError,
                isError = uiState.isPasswordHighlighted,
                keyboardActions = KeyboardActions(onDone = { onSubmit() }),
                imeAction = ImeAction.Done
            )
        }

        // Forgot password text link (aligned to the right under password field)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(forgotAlpha)
                .padding(top = 5.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            ForgotPasswordLink(onClick = onForgotPasswordClick)
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Primary "Log in" Button (Warm Amber #E5A23C)
        Box(modifier = Modifier.alpha(buttonAlpha)) {
            LoginPrimaryButton(
                onClick = onSubmit,
                isLoading = uiState.isLoading
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Bottom "Don't have an account? Get Started" footer
        Box(modifier = Modifier.alpha(footerAlpha)) {
            LoginGetStartedFooter(onGetStartedClick = onGetStartedClick)
        }
    }
}
