package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.ui.hub.components.MyProfileContent
import com.example.ui.hub.components.MyProfileDialog
import com.example.ui.hub.dashboard.components.HubDashboardHeader
import com.example.ui.profilesetup.model.ProfileAvatarType
import com.example.ui.profilesetup.model.ProfileGender
import com.example.ui.profilesetup.model.UserProfileData
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class HubDashboardProfileMenuTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testHubDashboardHeader_profileIcon_opensDropdownWithOptions() {
        var myProfileClicked = false
        var logoutClicked = false

        composeTestRule.setContent {
            MyApplicationTheme {
                HubDashboardHeader(
                    hubName = "Smith Family Hub",
                    hubCode = "SMTH24",
                    avatarType = ProfileAvatarType.MALE,
                    onMyProfileClick = { myProfileClicked = true },
                    onLogoutClick = { logoutClicked = true }
                )
            }
        }

        // Verify profile icon is displayed
        composeTestRule.onNodeWithTag("hub_dashboard_profile_icon").assertIsDisplayed()

        // Click the profile icon
        composeTestRule.onNodeWithTag("hub_dashboard_profile_icon").performClick()

        // Verify menu with "My profile" and "Logout" options is displayed
        composeTestRule.onNodeWithTag("hub_profile_dropdown_menu").assertIsDisplayed()
        composeTestRule.onNodeWithTag("menu_item_my_profile").assertIsDisplayed()
        composeTestRule.onNodeWithText("My profile").assertIsDisplayed()
        composeTestRule.onNodeWithTag("menu_item_logout").assertIsDisplayed()
        composeTestRule.onNodeWithText("Logout").assertIsDisplayed()

        // Click "My profile"
        composeTestRule.onNodeWithTag("menu_item_my_profile").performClick()
        assertTrue(myProfileClicked)
    }

    @Test
    fun testHubDashboardHeader_logoutOption_invokesLogout() {
        var logoutClicked = false

        composeTestRule.setContent {
            MyApplicationTheme {
                HubDashboardHeader(
                    hubName = "Smith Family Hub",
                    hubCode = "SMTH24",
                    avatarType = ProfileAvatarType.MALE,
                    onMyProfileClick = {},
                    onLogoutClick = { logoutClicked = true }
                )
            }
        }

        // Click the profile icon to open menu
        composeTestRule.onNodeWithTag("hub_dashboard_profile_icon").performClick()

        // Click "Logout"
        composeTestRule.onNodeWithTag("menu_item_logout").performClick()
        assertTrue(logoutClicked)
    }

    @Test
    fun testMyProfileDialog_displaysProfileDetailsAndActions() {
        var dismissed = false
        var logoutClicked = false

        val testProfile = UserProfileData(
            name = "Sarah Connor",
            gender = ProfileGender.FEMALE,
            age = 34,
            aboutMe = "Caring mom managing family prescriptions",
            avatarType = ProfileAvatarType.FEMALE,
            isCompleted = true
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                MyProfileContent(
                    userProfile = testProfile,
                    onDismiss = { dismissed = true },
                    onLogout = { logoutClicked = true }
                )
            }
        }

        // Verify dialog components
        composeTestRule.onNodeWithTag("my_profile_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithText("My Profile").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sarah Connor").assertIsDisplayed()
        composeTestRule.onNodeWithText("34 yrs").assertIsDisplayed()
        composeTestRule.onNodeWithText("Female").assertIsDisplayed()
        composeTestRule.onNodeWithText("Caring mom managing family prescriptions").assertIsDisplayed()

        // Test Close button
        composeTestRule.onNodeWithTag("my_profile_close_button").performClick()
        assertTrue(dismissed)
    }
}
