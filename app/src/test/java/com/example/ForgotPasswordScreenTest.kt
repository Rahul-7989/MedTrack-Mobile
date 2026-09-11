package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.ui.forgotpassword.ForgotPasswordScreen
import com.example.ui.forgotpassword.ForgotPasswordUiState
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class ForgotPasswordScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testEmailValidationLogic() {
        assertFalse(ForgotPasswordUiState.isValidEmailFormat(""))
        assertFalse(ForgotPasswordUiState.isValidEmailFormat("invalid-email"))
        assertFalse(ForgotPasswordUiState.isValidEmailFormat("user@"))
        assertFalse(ForgotPasswordUiState.isValidEmailFormat("user@domain"))
        assertTrue(ForgotPasswordUiState.isValidEmailFormat("user@example.com"))
        assertTrue(ForgotPasswordUiState.isValidEmailFormat("alex.test+tag@medtrack.org"))
    }

    @Test
    fun testForgotPasswordScreen_initialRender_andPrefilledEmail() {
        var backClicked = false
        var loginClicked = false

        composeTestRule.setContent {
            MyApplicationTheme {
                ForgotPasswordScreen(
                    initialEmail = "alex@example.com",
                    onNavigateBack = { backClicked = true },
                    onNavigateToLogin = { loginClicked = true }
                )
            }
        }

        // Verify title & supporting text
        composeTestRule.onNodeWithText("Forgot your password?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Enter your email and we’ll send you a link to reset your password.").assertIsDisplayed()

        // Verify pre-filled email
        composeTestRule.onNodeWithText("alex@example.com").assertIsDisplayed()

        // Verify primary button
        composeTestRule.onNodeWithText("Get Password Reset Link").assertIsDisplayed()

        // Verify back button in top left
        composeTestRule.onNodeWithTag("forgot_password_back_button").performClick()
        assertTrue(backClicked)
    }

    @Test
    fun testForgotPasswordScreen_emptyEmail_showsValidationError() {
        composeTestRule.setContent {
            MyApplicationTheme {
                ForgotPasswordScreen(
                    initialEmail = "",
                    onNavigateBack = {},
                    onNavigateToLogin = {}
                )
            }
        }

        // Click submit without entering email
        composeTestRule.onNodeWithTag("forgot_password_submit_button").performClick()

        // Verify inline error appears
        composeTestRule.onNodeWithText("Please enter your email address.").assertIsDisplayed()
    }

    @Test
    fun testForgotPasswordScreen_invalidEmail_showsValidationError() {
        composeTestRule.setContent {
            MyApplicationTheme {
                ForgotPasswordScreen(
                    initialEmail = "",
                    onNavigateBack = {},
                    onNavigateToLogin = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("forgot_password_email_input").performTextInput("not-an-email")
        composeTestRule.onNodeWithTag("forgot_password_submit_button").performClick()

        // Verify inline error
        composeTestRule.onNodeWithText("Please enter a valid email address.").assertIsDisplayed()
    }

    @Test
    fun testForgotPasswordScreen_successState_displaysMessageAndLoginInButton() {
        var loginInClicked = false

        composeTestRule.setContent {
            MyApplicationTheme {
                ForgotPasswordScreen(
                    initialEmail = "user@example.com",
                    onNavigateBack = {},
                    onNavigateToLogin = { loginInClicked = true }
                )
            }
        }

        // Click submit
        composeTestRule.onNodeWithTag("forgot_password_submit_button").performClick()

        // Advance clock for coroutine to complete
        composeTestRule.mainClock.advanceTimeBy(1000)

        // Verify exact success state text: “We sent you a password change link to <email>”
        composeTestRule.onNodeWithText("We sent you a password change link to user@example.com").assertIsDisplayed()

        // Verify exact button text: “Log in”
        composeTestRule.onNodeWithText("Log in").assertIsDisplayed()

        // Click “Log in”
        composeTestRule.onNodeWithTag("forgot_password_login_in_button").performClick()
        assertTrue(loginInClicked)
    }
}
