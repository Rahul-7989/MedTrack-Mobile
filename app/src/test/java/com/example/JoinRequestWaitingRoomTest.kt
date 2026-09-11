package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.ui.hub.dashboard.components.HubJoinRequestsBanner
import com.example.ui.hub.dashboard.model.HubJoinRequest
import com.example.ui.hub.waitingroom.JoinRequestWaitingRoomScreen
import com.example.ui.hub.waitingroom.components.LeaveRequestConfirmDialog
import com.example.ui.hub.waitingroom.components.SequentialPulseDots
import com.example.ui.profilesetup.model.ProfileAvatarType
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class JoinRequestWaitingRoomTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testWaitingRoom_displaysPendingStateProperly() {
        composeTestRule.setContent {
            MyApplicationTheme {
                JoinRequestWaitingRoomScreen(
                    hubId = "test_hub_123",
                    hubName = "Johnson Family",
                    requestId = "req_1",
                    onNavigateToHubSelection = {},
                    onNavigateToHubDashboard = {}
                )
            }
        }

        // Verify Hub Name
        composeTestRule.onNodeWithTag("waiting_room_hub_name").assertIsDisplayed()
        composeTestRule.onNodeWithText("Johnson Family").assertIsDisplayed()

        // Verify Heading
        composeTestRule.onNodeWithTag("waiting_room_heading").assertIsDisplayed()
        composeTestRule.onNodeWithText("Waiting for approval").assertIsDisplayed()

        // Verify Supporting text
        composeTestRule.onNodeWithTag("waiting_room_supporting_text").assertIsDisplayed()
        composeTestRule.onNodeWithText("Please wait until the creator allows you to join this family hub.").assertIsDisplayed()

        // Verify Pulse dots and status caption
        composeTestRule.onNodeWithTag("waiting_room_sequential_pulse_dots").assertIsDisplayed()
        composeTestRule.onNodeWithTag("waiting_room_status_caption").assertIsDisplayed()
        composeTestRule.onNodeWithText("Your request has been sent.").assertIsDisplayed()
    }

    @Test
    fun testWaitingRoom_backButtonShowsLeaveConfirmDialog() {
        var leaveCancelled = false
        var leaveConfirmed = false

        composeTestRule.setContent {
            MyApplicationTheme {
                JoinRequestWaitingRoomScreen(
                    hubId = "test_hub_123",
                    hubName = "Johnson Family",
                    requestId = "req_1",
                    onNavigateToHubSelection = { leaveConfirmed = true },
                    onNavigateToHubDashboard = {}
                )
            }
        }

        // Tap Back button in Waiting Room
        composeTestRule.onNodeWithTag("waiting_room_back_button").performClick()

        // Confirm Dialog is displayed
        composeTestRule.onNodeWithTag("leave_request_confirm_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithText("Leave this request?").assertIsDisplayed()
        composeTestRule.onNodeWithText("If you go back, your request to join this family hub will be cancelled.").assertIsDisplayed()

        // Verify buttons
        composeTestRule.onNodeWithTag("leave_request_stay_button").assertIsDisplayed()
        composeTestRule.onNodeWithText("Stay here").assertIsDisplayed()
        composeTestRule.onNodeWithTag("leave_request_confirm_button").assertIsDisplayed()
        composeTestRule.onNodeWithText("Yes, take me back").assertIsDisplayed()

        // Tap Stay here
        composeTestRule.onNodeWithTag("leave_request_stay_button").performClick()
        // Dialog should be dismissed
        composeTestRule.onNodeWithTag("leave_request_confirm_dialog").assertDoesNotExist()
    }

    @Test
    fun testCreatorDashboard_joinRequestsBannerDisplaysAndExpands() {
        val testRequests = listOf(
            HubJoinRequest(
                id = "req_abc",
                userId = "user_456",
                userName = "Emily",
                avatarType = ProfileAvatarType.FEMALE
            )
        )

        var acceptedRequestId: String? = null
        var rejectedRequestId: String? = null

        composeTestRule.setContent {
            MyApplicationTheme {
                HubJoinRequestsBanner(
                    requests = testRequests,
                    onAccept = { acceptedRequestId = it },
                    onReject = { rejectedRequestId = it }
                )
            }
        }

        // Verify Header with dynamic count: "FAMILY REQUESTS · 1"
        composeTestRule.onNodeWithTag("hub_dashboard_join_requests_banner").assertIsDisplayed()
        composeTestRule.onNodeWithText("FAMILY REQUESTS · 1").assertIsDisplayed()

        // Verify compact request item
        composeTestRule.onNodeWithTag("join_request_item_req_abc").assertIsDisplayed()
        composeTestRule.onNodeWithText("Emily").assertIsDisplayed()
        composeTestRule.onNodeWithText("Wants to join your family hub").assertIsDisplayed()

        // Tap request row to expand
        composeTestRule.onNodeWithTag("join_request_item_req_abc").performClick()

        // Reject and Accept buttons should now be visible
        composeTestRule.onNodeWithTag("reject_request_button_req_abc").assertIsDisplayed()
        composeTestRule.onNodeWithText("Reject").assertIsDisplayed()

        composeTestRule.onNodeWithTag("accept_request_button_req_abc").assertIsDisplayed()
        composeTestRule.onNodeWithText("Accept").assertIsDisplayed()

        // Tap Accept
        composeTestRule.onNodeWithTag("accept_request_button_req_abc").performClick()
        assertEquals("req_abc", acceptedRequestId)
    }
}
