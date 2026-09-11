package com.example.ui.login

enum class LoginErrorType {
    EMAIL_NOT_REGISTERED,
    WRONG_PASSWORD,
    GENERAL
}

data class LoginInlineError(
    val title: String,
    val supporting: String? = null,
    val type: LoginErrorType = LoginErrorType.GENERAL
)

/**
 * Isolated UI State for the MedTrack Login Screen.
 * Contains purely state logic needed for authentication and interaction,
 * decoupled from any other page in the application.
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val inlineError: LoginInlineError? = null,
    val generalError: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val showForgotPasswordDialog: Boolean = false,
    val forgotPasswordEmail: String = "",
    val forgotPasswordSent: Boolean = false
) {
    val isEmailHighlighted: Boolean
        get() = emailError != null || inlineError?.type == LoginErrorType.EMAIL_NOT_REGISTERED

    val isPasswordHighlighted: Boolean
        get() = passwordError != null || inlineError?.type == LoginErrorType.WRONG_PASSWORD

    val canSubmit: Boolean
        get() = email.isNotBlank() && password.isNotBlank() && !isLoading
}

