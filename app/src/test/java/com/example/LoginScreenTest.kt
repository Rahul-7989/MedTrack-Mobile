package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.ui.login.LoginScreen
import com.example.ui.theme.MyApplicationTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LoginScreenTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun testLoginScreen_containsRequiredContent_andNoLogo() {
    composeTestRule.setContent {
      MyApplicationTheme {
        LoginScreen()
      }
    }

    // Advance clock past entrance animations (up to 600ms stagger)
    composeTestRule.mainClock.advanceTimeBy(1500)

    // 1. Verify "Welcome back" is displayed
    composeTestRule.onNodeWithText("Welcome back").assertIsDisplayed()

    // 2. Verify supporting line is displayed
    composeTestRule.onNodeWithText("Sign in to your family hub.").assertIsDisplayed()

    // 3. Verify Email input is displayed
    composeTestRule.onNodeWithTag("login_email_input").assertIsDisplayed()

    // 4. Verify Password input is displayed
    composeTestRule.onNodeWithTag("login_password_input").assertIsDisplayed()

    // 5. Verify Password visibility toggle is displayed
    composeTestRule.onNodeWithTag("toggle_password_visibility").assertIsDisplayed()

    // 6. Verify "Forgot password?" is displayed
    composeTestRule.onNodeWithText("Forgot password?").assertIsDisplayed()

    // 7. Verify "Log in" button is displayed
    composeTestRule.onNodeWithTag("login_submit_button").assertIsDisplayed()

    // 8. Verify "Don't have an account?" & "Get Started" exist in the hierarchy
    composeTestRule.onNodeWithText("Don't have an account?", substring = true).assertExists()
    composeTestRule.onNodeWithText("Get Started").assertExists()

    // 9. Verify Return to Home button is displayed
    composeTestRule.onNodeWithTag("return_home_button").assertIsDisplayed()

    // 10. Verify MedTrack Logo is NOT on the login screen
    composeTestRule.onNodeWithTag("medtrack_logo").assertDoesNotExist()
  }

  @Test
  fun testLoginScreen_returnHomeClick() {
    var returnedHome = false
    composeTestRule.setContent {
      MyApplicationTheme {
        LoginScreen(
          onNavigateBack = { returnedHome = true }
        )
      }
    }

    composeTestRule.mainClock.advanceTimeBy(1500)
    composeTestRule.onNodeWithTag("return_home_button").performClick()
    assert(returnedHome)
  }

  @Test
  fun testLoginScreen_validationErrors() {
    composeTestRule.setContent {
      MyApplicationTheme {
        LoginScreen()
      }
    }

    composeTestRule.mainClock.advanceTimeBy(1500)

    // Clicking Log in with empty inputs triggers validation
    composeTestRule.onNodeWithTag("login_submit_button").performClick()
    composeTestRule.onNodeWithText("Please enter your email").assertIsDisplayed()
  }

  @Test
  fun testLoginScreen_incorrectCredentialsError() {
    composeTestRule.setContent {
      MyApplicationTheme {
        LoginScreen()
      }
    }

    composeTestRule.mainClock.advanceTimeBy(1500)

    // Enter email and password
    composeTestRule.onNodeWithTag("login_email_input").performTextInput("test@example.com")
    composeTestRule.onNodeWithTag("login_password_input").performTextInput("wrongpassword")

    // Submit login
    composeTestRule.onNodeWithTag("login_submit_button").performClick()

    // Advance clock to allow async auth response
    composeTestRule.mainClock.advanceTimeBy(1000)

    // Verify "Password is incorrect." error is displayed
    composeTestRule.onNodeWithText("Password is incorrect.").assertIsDisplayed()
  }
}
