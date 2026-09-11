package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.ui.MedTrackLandingScreen
import com.example.ui.emailverification.EmailVerificationScreen
import com.example.ui.hub.create.CreateFamilyHubScreen
import com.example.ui.hub.join.JoinFamilyHubScreen
import com.example.ui.theme.MyApplicationTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class TypographyRefinementsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testHomePage_loginButton_displaysLogInText() {
        composeTestRule.setContent {
            MyApplicationTheme {
                MedTrackLandingScreen()
            }
        }

        // Verify Home page button text is "Log in"
        composeTestRule.onNodeWithTag("login_button").assertIsDisplayed()
        composeTestRule.onNodeWithText("Log in").assertIsDisplayed()
    }

    @Test
    fun testCreateFamilyHubScreen_displaysRequiredHierarchy() {
        composeTestRule.setContent {
            MyApplicationTheme {
                CreateFamilyHubScreen(
                    onProceedToHub = {}
                )
            }
        }

        composeTestRule.mainClock.advanceTimeBy(500)

        // 1. Heading preserved
        composeTestRule.onNodeWithText("Create your family hub").assertIsDisplayed()

        // 2. Supporting text
        composeTestRule.onNodeWithText("Give your family a name and create a unique Hub Code to invite them.").assertIsDisplayed()

        // 3. Field label
        composeTestRule.onNodeWithText("Family hub name").assertIsDisplayed()

        // 4. Hub code section label
        composeTestRule.onNodeWithText("HUB CODE").assertIsDisplayed()

        // 5. Generate button
        composeTestRule.onNodeWithText("Generate Hub Code").assertIsDisplayed()
    }

    @Test
    fun testJoinFamilyHubScreen_displaysRequiredHierarchy() {
        composeTestRule.setContent {
            MyApplicationTheme {
                JoinFamilyHubScreen(
                    onJoinSuccess = {}
                )
            }
        }

        composeTestRule.mainClock.advanceTimeBy(500)

        // 1. Heading preserved
        composeTestRule.onNodeWithText("Join your family hub").assertIsDisplayed()

        // 2. Supporting text
        composeTestRule.onNodeWithText("Enter the Hub Code shared by your family member.").assertIsDisplayed()

        // 3. Hub code section label
        composeTestRule.onNodeWithText("HUB CODE").assertIsDisplayed()

        // 4. Primary Join button
        composeTestRule.onNodeWithText("Join the family hub").assertIsDisplayed()
    }

    @Test
    fun testEmailVerificationScreen_displaysRequiredHierarchy() {
        composeTestRule.setContent {
            MyApplicationTheme {
                EmailVerificationScreen(
                    email = "test@medtrack.org",
                    onVerified = {}
                )
            }
        }

        composeTestRule.mainClock.advanceTimeBy(500)

        // 1. Heading preserved
        composeTestRule.onNodeWithText("Check your email").assertIsDisplayed()

        // 2. Supporting text
        composeTestRule.onNodeWithText("We've sent a verification link to", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("test@medtrack.org").assertIsDisplayed()
        composeTestRule.onNodeWithText("Please verify your email to continue.").assertIsDisplayed()

        // 3. Button
        composeTestRule.onNodeWithText("I've verified my email").assertIsDisplayed()
    }
}
