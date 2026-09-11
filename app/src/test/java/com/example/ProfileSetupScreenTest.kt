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
import com.example.ui.profilesetup.ProfileSetupScreen
import com.example.ui.profilesetup.data.UserProfileRepository
import com.example.ui.profilesetup.model.ProfileAvatarType
import com.example.ui.profilesetup.model.ProfileGender
import com.example.ui.profilesetup.model.UserProfileData
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ProfileSetupScreenTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    UserProfileRepository.clearProfile()
  }

  @Test
  fun testProfileSetupScreen_containsRequiredContent_andInitialState() {
    composeTestRule.setContent {
      MyApplicationTheme {
        ProfileSetupScreen(
          onProfileSaved = {}
        )
      }
    }

    composeTestRule.mainClock.advanceTimeBy(1000)

    // 1. Avatar container exists
    composeTestRule.onNodeWithTag("profile_avatar_container").assertIsDisplayed()

    // 2. Heading and Subheading
    composeTestRule.onNodeWithText("Let's set up your profile").assertIsDisplayed()
    composeTestRule.onNodeWithText("A few details so your family knows who's who.").assertIsDisplayed()

    // 3. Name field
    composeTestRule.onNodeWithText("Your name").assertIsDisplayed()
    composeTestRule.onNodeWithTag("profile_name_input").assertIsDisplayed()

    // 4. Gender selection chips
    composeTestRule.onNodeWithText("Gender").assertIsDisplayed()
    composeTestRule.onNodeWithTag("gender_chip_male").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("gender_chip_female").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("gender_chip_prefer_not_to_say").performScrollTo().assertIsDisplayed()

    // 5. Age slider and display
    composeTestRule.onNodeWithText("Your age").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("profile_age_display").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("profile_age_slider").performScrollTo().assertIsDisplayed()

    // 6. Optional About Me trigger
    composeTestRule.onNodeWithTag("add_about_me_trigger").performScrollTo().assertIsDisplayed()

    // 7. Continue button is initially disabled
    composeTestRule.onNodeWithTag("profile_continue_button").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("profile_continue_button").assertIsNotEnabled()
  }

  @Test
  fun testProfileSetupScreen_dynamicAvatar_updatesWithGenderSelection() {
    composeTestRule.setContent {
      MyApplicationTheme {
        ProfileSetupScreen(
          onProfileSaved = {}
        )
      }
    }

    composeTestRule.mainClock.advanceTimeBy(1000)

    // 1. Select Male -> Male Avatar displayed
    composeTestRule.onNodeWithTag("gender_chip_male").performClick()
    composeTestRule.mainClock.advanceTimeBy(500)
    composeTestRule.onNodeWithTag("profile_avatar_male").assertIsDisplayed()

    // 2. Select Female -> Female Avatar displayed
    composeTestRule.onNodeWithTag("gender_chip_female").performClick()
    composeTestRule.mainClock.advanceTimeBy(500)
    composeTestRule.onNodeWithTag("profile_avatar_female").assertIsDisplayed()

    // 3. Select Prefer not to say -> Anonymous Avatar displayed
    composeTestRule.onNodeWithTag("gender_chip_prefer_not_to_say").performScrollTo().performClick()
    composeTestRule.mainClock.advanceTimeBy(500)
    composeTestRule.onNodeWithTag("profile_avatar_anonymous").performScrollTo().assertIsDisplayed()
  }

  @Test
  fun testProfileSetupScreen_validation_enablesContinueButton() {
    composeTestRule.setContent {
      MyApplicationTheme {
        ProfileSetupScreen(
          onProfileSaved = {}
        )
      }
    }

    composeTestRule.mainClock.advanceTimeBy(1000)

    // Initially disabled
    composeTestRule.onNodeWithTag("profile_continue_button").performScrollTo().assertIsNotEnabled()

    // Enter name only -> still disabled
    composeTestRule.onNodeWithTag("profile_name_input").performScrollTo().performTextInput("Morgan Vance")
    composeTestRule.onNodeWithTag("profile_continue_button").performScrollTo().assertIsNotEnabled()

    // Select gender -> becomes enabled
    composeTestRule.onNodeWithTag("gender_chip_female").performScrollTo().performClick()
    composeTestRule.mainClock.advanceTimeBy(300)
    composeTestRule.onNodeWithTag("profile_continue_button").performScrollTo().assertIsEnabled()
  }

  @Test
  fun testProfileSetupScreen_aboutMe_expandCollapseAndCharacterLimit() {
    composeTestRule.setContent {
      MyApplicationTheme {
        ProfileSetupScreen(
          onProfileSaved = {}
        )
      }
    }

    composeTestRule.mainClock.advanceTimeBy(1000)

    // Click trigger to expand About Me
    composeTestRule.onNodeWithTag("add_about_me_trigger").performScrollTo().performClick()
    composeTestRule.mainClock.advanceTimeBy(400)

    // Verify expanded fields
    composeTestRule.onNodeWithTag("about_me_input").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("about_me_char_counter").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithText("0 / 200").performScrollTo().assertIsDisplayed()

    // Enter text
    composeTestRule.onNodeWithTag("about_me_input").performTextInput("Loving dad of two and fitness enthusiast.")
    composeTestRule.mainClock.advanceTimeBy(200)

    // Character counter updates
    composeTestRule.onNodeWithText("41 / 200").performScrollTo().assertIsDisplayed()

    // Click remove
    composeTestRule.onNodeWithTag("about_me_remove_button").performScrollTo().performClick()
    composeTestRule.mainClock.advanceTimeBy(400)

    // Reverts to trigger
    composeTestRule.onNodeWithTag("add_about_me_trigger").performScrollTo().assertIsDisplayed()
  }

  @Test
  fun testProfileSetupScreen_continueClick_savesProfileAndCallsCallback() {
    var savedProfile: UserProfileData? = null

    composeTestRule.setContent {
      MyApplicationTheme {
        ProfileSetupScreen(
          initialAge = 28,
          onProfileSaved = { savedProfile = it }
        )
      }
    }

    composeTestRule.mainClock.advanceTimeBy(1000)

    // Fill form
    composeTestRule.onNodeWithTag("profile_name_input").performScrollTo().performTextInput("Taylor Bennett")
    composeTestRule.onNodeWithTag("gender_chip_male").performScrollTo().performClick()
    composeTestRule.mainClock.advanceTimeBy(300)

    // Click continue
    composeTestRule.onNodeWithTag("profile_continue_button").performScrollTo().performClick()

    assertNotNull("Saved profile should not be null", savedProfile)
    assertEquals("Taylor Bennett", savedProfile?.name)
    assertEquals(ProfileGender.MALE, savedProfile?.gender)
    assertEquals(ProfileAvatarType.MALE, savedProfile?.avatarType)
    assertEquals(28, savedProfile?.age)
    assertTrue("Profile should be marked completed", savedProfile?.isCompleted == true)
    assertTrue("Repository should be complete", UserProfileRepository.isProfileComplete)
  }
}
