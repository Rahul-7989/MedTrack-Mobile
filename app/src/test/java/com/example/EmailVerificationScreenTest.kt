package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.ui.emailverification.EmailVerificationScreen
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class EmailVerificationScreenTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun testEmailVerificationScreen_containsRequiredContent() {
    composeTestRule.setContent {
      MyApplicationTheme {
        EmailVerificationScreen(
          onVerified = {}
        )
      }
    }

    composeTestRule.mainClock.advanceTimeBy(1000)

    // 1. Verify illustration is displayed
    composeTestRule.onNodeWithTag("email_verification_illustration").assertIsDisplayed()

    // 2. Verify heading: "Check your email"
    composeTestRule.onNodeWithText("Check your email").assertIsDisplayed()

    // 3. Verify supporting message
    composeTestRule.onNodeWithText("We've sent a verification link to your email address.").assertIsDisplayed()

    // 4. Verify secondary supporting message
    composeTestRule.onNodeWithText("Please verify your email to continue.").assertIsDisplayed()

    // 5. Verify primary button: "I've verified my email"
    composeTestRule.onNodeWithTag("verify_email_primary_button").assertIsDisplayed()
    composeTestRule.onNodeWithText("I've verified my email").assertIsDisplayed()

    // 6. Verify resend section
    composeTestRule.onNodeWithText("Didn't receive the email?", substring = true).assertIsDisplayed()
    composeTestRule.onNodeWithTag("resend_verification_link").assertIsDisplayed()
  }

  @Test
  fun testEmailVerificationScreen_withEmailPassed_displaysEmailAddress() {
    val testEmail = "patient.smith@medtrack.org"

    composeTestRule.setContent {
      MyApplicationTheme {
        EmailVerificationScreen(
          email = testEmail,
          onVerified = {}
        )
      }
    }

    composeTestRule.mainClock.advanceTimeBy(1000)

    composeTestRule.onNodeWithText("We've sent a verification link to", substring = true).assertIsDisplayed()
    composeTestRule.onNodeWithText(testEmail).assertIsDisplayed()
    composeTestRule.onNodeWithText("Please verify your email to continue.").assertIsDisplayed()
  }

  @Test
  fun testEmailVerificationScreen_primaryButtonClick_triggersCallback() {
    var verifiedClicked = false

    composeTestRule.setContent {
      MyApplicationTheme {
        EmailVerificationScreen(
          onVerified = { verifiedClicked = true }
        )
      }
    }

    composeTestRule.mainClock.advanceTimeBy(1000)

    composeTestRule.onNodeWithTag("verify_email_primary_button").performClick()
    assertTrue("onVerified callback should be called", verifiedClicked)
  }

  @Test
  fun testEmailVerificationScreen_resendClick_showsConfirmation_andStartsCooldown() {
    var resendTriggered = false

    composeTestRule.setContent {
      MyApplicationTheme {
        EmailVerificationScreen(
          onVerified = {},
          onResendVerification = { resendTriggered = true }
        )
      }
    }

    composeTestRule.mainClock.advanceTimeBy(1000)

    // Initial state: link is active
    composeTestRule.onNodeWithTag("resend_verification_link").assertIsEnabled()

    // Click Resend
    composeTestRule.onNodeWithTag("resend_verification_link").performClick()
    assertTrue("onResendVerification callback should be called", resendTriggered)

    // Advance clock slightly for state update
    composeTestRule.mainClock.advanceTimeBy(200)

    // Confirmation message appears
    composeTestRule.onNodeWithTag("resend_confirmation_message").assertIsDisplayed()
    composeTestRule.onNodeWithText("Verification email sent again.").assertIsDisplayed()

    // Cooldown is active, link shows countdown and is disabled
    composeTestRule.onNodeWithTag("resend_verification_link").assertIsNotEnabled()
  }
}
