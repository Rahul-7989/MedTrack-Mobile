package com.example.ui.getstarted

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.auth.AuthErrorType
import com.example.data.auth.AuthResult
import com.example.data.auth.FirebaseAuthService
import com.example.ui.getstarted.components.AlreadyHaveAccountFooter
import com.example.ui.getstarted.components.CreateAccountButton
import com.example.ui.getstarted.components.GetStartedBackgroundShapes
import com.example.ui.getstarted.components.GetStartedConfirmPasswordField
import com.example.ui.getstarted.components.GetStartedEmailField
import com.example.ui.getstarted.components.GetStartedHomeButton
import com.example.ui.getstarted.components.GetStartedPanel
import com.example.ui.getstarted.components.GetStartedPasswordField
import com.example.ui.getstarted.components.PasswordMatchIndicator
import com.example.ui.getstarted.components.PasswordRequirementsState
import com.example.ui.getstarted.components.PasswordRequirementsView
import com.example.ui.theme.SoraFontFamily
import kotlinx.coroutines.launch

// Scoped page background
private val ColorWarmIvory = Color(0xFFFAF4EC)

private val EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$".toRegex()

/**
 * MedTrack Get Started / Account Creation Screen.
 *
 * Designed as a completely separate, independently modular onboarding page.
 * Keeps all styling, layout, state, and validation logic strictly scoped.
 * Contains live interactive password requirements checking, live password matching,
 * restrained email validation, and accessible navigation.
 */
@Composable
fun GetStartedScreen(
    modifier: Modifier = Modifier,
    onNavigateHome: () -> Unit = {},
    onNavigateBack: () -> Unit = onNavigateHome,
    onNavigateToLogin: () -> Unit = {},
    onAccountCreated: (email: String) -> Unit = {}
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

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

    // Isolated Form State
    var email by remember { mutableStateOf("") }
    var emailInteracted by remember { mutableStateOf(false) }
    var emailAlreadyRegistered by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var confirmPassword by remember { mutableStateOf("") }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var registrationError by remember { mutableStateOf<String?>(null) }

    // Validation Computations
    val isEmailFormatValid by remember {
        derivedStateOf { EMAIL_REGEX.matches(email.trim()) }
    }

    val emailError by remember {
        derivedStateOf {
            if (emailInteracted && email.isNotEmpty() && !isEmailFormatValid) {
                "Please enter a valid email address"
            } else null
        }
    }

    val passwordReqs by remember {
        derivedStateOf { PasswordRequirementsState.from(password) }
    }

    val passwordsMatch by remember {
        derivedStateOf {
            confirmPassword.isNotEmpty() && password == confirmPassword
        }
    }

    val isFormValid by remember {
        derivedStateOf {
            isEmailFormatValid && passwordReqs.allSatisfied && passwordsMatch && !emailAlreadyRegistered
        }
    }

    // Staggered Entrance Animations
    var startEntrance by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        startEntrance = true
    }

    val easeOutCurve = remember { CubicBezierEasing(0.25f, 0.1f, 0.25f, 1.0f) }

    val panelAlpha by animateFloatAsState(
        targetValue = if (startEntrance) 1f else 0f,
        animationSpec = if (isReducedMotion) tween(0) else tween(durationMillis = 500, delayMillis = 100, easing = easeOutCurve),
        label = "PanelAlpha"
    )

    // "Create your account" entrance animation: opacity 0 -> 1, translateY 8dp -> 0dp, ~400ms
    val headerAlpha by animateFloatAsState(
        targetValue = if (startEntrance) 1f else 0f,
        animationSpec = if (isReducedMotion) tween(0) else tween(durationMillis = 400, delayMillis = 150, easing = easeOutCurve),
        label = "HeaderAlpha"
    )
    val headerOffsetY by animateDpAsState(
        targetValue = if (startEntrance) 0.dp else 8.dp,
        animationSpec = if (isReducedMotion) tween(0) else tween(durationMillis = 400, delayMillis = 150, easing = easeOutCurve),
        label = "HeaderOffsetY"
    )

    val formFieldsAlpha by animateFloatAsState(
        targetValue = if (startEntrance) 1f else 0f,
        animationSpec = if (isReducedMotion) tween(0) else tween(durationMillis = 500, delayMillis = 200, easing = easeOutCurve),
        label = "FieldsAlpha"
    )

    fun handleCreateAccount() {
        if (!isFormValid || isLoading) return
        focusManager.clearFocus()
        isLoading = true
        registrationError = null
        emailAlreadyRegistered = false
        coroutineScope.launch {
            val result = FirebaseAuthService.Instance.createAccount(email.trim(), password)
            isLoading = false
            when (result) {
                is AuthResult.Success -> {
                    onAccountCreated(email.trim())
                }
                is AuthResult.Error -> {
                    if (result.errorType == AuthErrorType.EMAIL_ALREADY_IN_USE) {
                        emailAlreadyRegistered = true
                        registrationError = null
                    } else {
                        registrationError = result.message
                        emailAlreadyRegistered = false
                    }
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
            val isCompact = maxWidth < 600.dp
            val isDesktopOrWide = maxWidth >= 800.dp

            // 1. Subtle, slow floating geometric background
            GetStartedBackgroundShapes(
                alpha = panelAlpha,
                isReducedMotion = isReducedMotion
            )

            // Main Content Area with natural vertical layout:
            // 1. [ ← Home ]
            // 2. [ Clear vertical breathing room ]
            // 3. [ Floating Account Creation Panel ]
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(
                        horizontal = if (isCompact) 18.dp else 32.dp,
                        vertical = if (isCompact) 20.dp else 28.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top area dedicated to the "← Home" button, sitting cleanly above the panel
                Box(
                    modifier = Modifier
                        .widthIn(max = 440.dp)
                        .fillMaxWidth()
                        .alpha(panelAlpha),
                    contentAlignment = Alignment.CenterStart
                ) {
                    GetStartedHomeButton(
                        onClick = onNavigateHome
                    )
                }

                // Clear vertical breathing space between the Home button and the account creation panel
                Spacer(modifier = Modifier.height(if (isCompact) 18.dp else 24.dp))

                GetStartedPanel(
                    isCompact = isCompact,
                    headerAlpha = headerAlpha,
                    headerOffsetY = headerOffsetY,
                    modifier = Modifier.alpha(panelAlpha)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(formFieldsAlpha)
                    ) {
                        // Error Banner (for server/general errors)
                        if (registrationError != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 14.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFFAF2EE))
                                    .border(BorderStroke(1.dp, Color(0xFFE8CBC2)), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                                    .testTag("getstarted_general_error"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = registrationError ?: "",
                                    fontFamily = SoraFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    lineHeight = 19.sp,
                                    color = Color(0xFFC8755D),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Email Field
                        GetStartedEmailField(
                            value = email,
                            onValueChange = {
                                email = it
                                emailInteracted = true
                                emailAlreadyRegistered = false
                                registrationError = null
                            },
                            isValid = isEmailFormatValid,
                            errorMessage = emailError,
                            errorTitle = if (emailAlreadyRegistered) "This email is already registered." else null,
                            errorSupporting = if (emailAlreadyRegistered) "Please check your email or log in to MedTrack." else null,
                            isError = emailAlreadyRegistered,
                            imeAction = ImeAction.Next,
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Password Field
                        GetStartedPasswordField(
                            value = password,
                            onValueChange = {
                                password = it
                                registrationError = null
                            },
                            isPasswordVisible = isPasswordVisible,
                            onTogglePasswordVisibility = { isPasswordVisible = !isPasswordVisible },
                            imeAction = ImeAction.Next,
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            )
                        )

                        // Live Dynamic Password Requirements List (○ to ✓)
                        PasswordRequirementsView(
                            state = passwordReqs
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Re-enter Password Field
                        GetStartedConfirmPasswordField(
                            value = confirmPassword,
                            onValueChange = {
                                confirmPassword = it
                                registrationError = null
                            },
                            isPasswordVisible = isConfirmPasswordVisible,
                            onTogglePasswordVisibility = { isConfirmPasswordVisible = !isConfirmPasswordVisible },
                            imeAction = ImeAction.Done,
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    if (isFormValid) handleCreateAccount()
                                }
                            )
                        )

                        // Live Dynamic Password Match Indicator (✕ to ✓)
                        PasswordMatchIndicator(
                            password = password,
                            confirmPassword = confirmPassword
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Primary "Create my account" Action Button
                        CreateAccountButton(
                            onClick = ::handleCreateAccount,
                            isEnabled = isFormValid,
                            isLoading = isLoading
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Bottom "Already have an account? Login here" Footer
                        AlreadyHaveAccountFooter(
                            onLoginClick = onNavigateToLogin
                        )
                    }
                }
            }
        }
    }
}
