package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import com.example.ui.getstarted.GetStartedScreen
import com.example.ui.theme.MyApplicationTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class GetStartedScreenTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun testGetStartedScreen_containsRequiredContent_andNoLogo() {
    composeTestRule.setContent {
      MyApplicationTheme {
        GetStartedScreen()
      }
    }

    composeTestRule.mainClock.advanceTimeBy(1500)

    // 1. Verify Header
    composeTestRule.onNodeWithText("Create your account").assertIsDisplayed()
    composeTestRule.onNodeWithText("Start your MedTrack journey.").assertIsDisplayed()

    // 2. Verify Email input
    composeTestRule.onNodeWithTag("get_started_email_input").assertIsDisplayed()

    // 3. Verify Password inputs and toggles
    composeTestRule.onNodeWithTag("get_started_password_input").assertIsDisplayed()
    composeTestRule.onNodeWithTag("toggle_get_started_password").assertIsDisplayed()
    composeTestRule.onNodeWithTag("get_started_confirm_password_input").assertExists()
    composeTestRule.onNodeWithTag("toggle_get_started_confirm_password").assertExists()

    // 4. Verify 4 password requirements are present
    composeTestRule.onNodeWithTag("req_min_length").assertExists()
    composeTestRule.onNodeWithTag("req_uppercase").assertExists()
    composeTestRule.onNodeWithTag("req_number").assertExists()
    composeTestRule.onNodeWithTag("req_special").assertExists()

    // 5. Verify Create my account button is initially disabled
    composeTestRule.onNodeWithTag("create_account_button").assertExists()
    composeTestRule.onNodeWithTag("create_account_button").assertIsNotEnabled()

    // 6. Verify "Already have an account? Login here" footer
    composeTestRule.onNodeWithText("Already have an account?", substring = true).assertExists()
    composeTestRule.onNodeWithTag("get_started_login_link").assertExists()

    // 7. Verify Home button
    composeTestRule.onNodeWithTag("get_started_home_button").assertIsDisplayed()
    composeTestRule.onNodeWithText("Home").assertIsDisplayed()

    // 8. Verify NO logo on Get Started screen
    composeTestRule.onNodeWithTag("medtrack_logo").assertDoesNotExist()
  }

  @Test
  fun testGetStartedScreen_homeButton_navigatesToHome() {
    var homeNavigated = false
    composeTestRule.setContent {
      MyApplicationTheme {
        GetStartedScreen(
          onNavigateHome = { homeNavigated = true }
        )
      }
    }

    composeTestRule.mainClock.advanceTimeBy(1500)
    composeTestRule.onNodeWithTag("get_started_home_button").performClick()
    assert(homeNavigated)
  }

  @Test
  fun testGetStartedScreen_loginLink_navigates() {
    var loggedIn = false
    composeTestRule.setContent {
      MyApplicationTheme {
        GetStartedScreen(
          onNavigateToLogin = { loggedIn = true }
        )
      }
    }

    composeTestRule.mainClock.advanceTimeBy(1500)
    composeTestRule.onNodeWithTag("get_started_login_link").performScrollTo().performClick()
    assert(loggedIn)
  }

  @Test
  fun testGetStartedScreen_livePasswordRequirements_andMatching() {
    var accountCreated = false
    composeTestRule.setContent {
      MyApplicationTheme {
        GetStartedScreen(
          onAccountCreated = { accountCreated = true }
        )
      }
    }

    composeTestRule.mainClock.advanceTimeBy(1500)

    // Enter email
    composeTestRule.onNodeWithTag("get_started_email_input").performTextInput("test@medtrack.com")

    // Enter password satisfying all 4 requirements
    composeTestRule.onNodeWithTag("get_started_password_input").performTextInput("Medtrack9!")

    // Enter mismatching confirm password
    composeTestRule.onNodeWithTag("get_started_confirm_password_input")
      .performScrollTo()
      .performTextInput("WrongPass")
    composeTestRule.onNodeWithText("Passwords don't match").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("create_account_button").performScrollTo().assertIsNotEnabled()

    // Fix confirm password to match
    composeTestRule.onNodeWithTag("get_started_confirm_password_input")
      .performTextReplacement("Medtrack9!")
    composeTestRule.onNodeWithText("Passwords match").performScrollTo().assertIsDisplayed()

    // Button should now be enabled
    composeTestRule.onNodeWithTag("create_account_button").performScrollTo().assertIsEnabled()
    composeTestRule.onNodeWithTag("create_account_button").performClick()
    composeTestRule.mainClock.advanceTimeBy(1000)
    assert(accountCreated)
  }
}
