package com.example.data.auth

enum class AuthErrorType {
    GENERAL,
    EMAIL_NOT_REGISTERED,
    WRONG_PASSWORD,
    EMAIL_ALREADY_IN_USE,
    EMAIL_NOT_VERIFIED,
    NETWORK_ERROR
}

sealed class AuthResult<out T> {
    data class Success<out T>(val data: T) : AuthResult<T>()
    data class Error(
        val message: String,
        val cause: Throwable? = null,
        val errorType: AuthErrorType = AuthErrorType.GENERAL
    ) : AuthResult<Nothing>()
}

