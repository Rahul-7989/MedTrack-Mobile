package com.example.ui.forgotpassword

import java.util.regex.Pattern

/**
 * UI State for the independent Forgot Password module.
 */
data class ForgotPasswordUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
    val submittedEmail: String = ""
) {
    companion object {
        private val EMAIL_PATTERN: Pattern = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        )

        fun isValidEmailFormat(email: String): Boolean {
            val trimmed = email.trim()
            return trimmed.isNotEmpty() && EMAIL_PATTERN.matcher(trimmed).matches()
        }
    }
}
