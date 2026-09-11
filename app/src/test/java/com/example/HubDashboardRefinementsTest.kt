package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.ui.hub.dashboard.components.AddEditMedicationModal
import com.example.ui.hub.dashboard.components.HubDashboardActions
import com.example.ui.hub.dashboard.components.HubDashboardHeader
import com.example.ui.hub.dashboard.components.HubDashboardTimeBar
import com.example.ui.hub.dashboard.components.MedicationBoard
import com.example.ui.hub.dashboard.components.MedicationCard
import com.example.ui.hub.dashboard.data.HubDashboardRepository
import com.example.ui.hub.dashboard.model.HubMember
import com.example.ui.hub.dashboard.model.MedicationItem
import com.example.ui.profilesetup.model.ProfileAvatarType
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class HubDashboardRefinementsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testHubName_isDisplayedInHeader() {
        composeTestRule.setContent {
            MyApplicationTheme {
                HubDashboardHeader(
                    hubName = "Rahul's Fam",
                    hubCode = "RHUL24",
                    avatarType = ProfileAvatarType.MALE,
                    onMyProfileClick = {},
                    onLogoutClick = {}
                )
            }
        }

        // Verify Hub Name and kicker are displayed properly
        composeTestRule.onNodeWithTag("hub_dashboard_title").assertIsDisplayed()
        composeTestRule.onNodeWithText("Rahul's Fam").assertIsDisplayed()
        composeTestRule.onNodeWithTag("hub_dashboard_kicker").assertIsDisplayed()
        composeTestRule.onNodeWithText("FAMILY HUB").assertIsDisplayed()
    }

    @Test
    fun testDynamicDate_matchesExactFormat() {
        val formattedDate = HubDashboardRepository.getCurrentFormattedDate()
        // Format MUST match: <DAY> <MONTH>, <YEAR> (e.g., "11 SEP, 2026")
        val regex = Regex("^\\d{1,2} (JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC), \\d{4}\$")
        assertTrue("Date '$formattedDate' should match pattern '<DATE> <MONTH>, <YEAR>'", regex.matches(formattedDate))

        composeTestRule.setContent {
            MyApplicationTheme {
                HubDashboardTimeBar(
                    dateLabel = formattedDate,
                    formattedTime = "9:42 AM"
                )
            }
        }

        composeTestRule.onNodeWithTag("hub_dashboard_date_label").assertIsDisplayed()
        composeTestRule.onNodeWithText(formattedDate).assertIsDisplayed()
        composeTestRule.onNodeWithTag("hub_dashboard_current_time_text").assertIsDisplayed()
        composeTestRule.onNodeWithText("9:42 AM").assertIsDisplayed()
    }

    @Test
    fun testActionButtons_areDisplayedAndClickable() {
        var addClicked = false
        var memoClicked = false

        composeTestRule.setContent {
            MyApplicationTheme {
                HubDashboardActions(
                    onAddMedicationClick = { addClicked = true },
                    onSmartVoiceMemoClick = { memoClicked = true }
                )
            }
        }

        // Verify icon-only buttons are rendered with accessible labels
        composeTestRule.onNodeWithTag("hub_dashboard_add_medication_button").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Add medication").assertIsDisplayed()
        composeTestRule.onNodeWithText("Add Medication").assertDoesNotExist()

        composeTestRule.onNodeWithTag("hub_dashboard_smart_voice_memo_button").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Smart Voice Memo").assertIsDisplayed()
        composeTestRule.onNodeWithText("Smart Voice Memo").assertDoesNotExist()

        // Perform clicks
        composeTestRule.onNodeWithTag("hub_dashboard_add_medication_button").performClick()
        assertTrue(addClicked)

        composeTestRule.onNodeWithTag("hub_dashboard_smart_voice_memo_button").performClick()
        assertTrue(memoClicked)
    }

    @Test
    fun testTimeBar_rendersDateAndQuickActionsSideBySide() {
        var addClicked = false
        var memoClicked = false

        composeTestRule.setContent {
            MyApplicationTheme {
                HubDashboardTimeBar(
                    dateLabel = "11 SEP, 2026",
                    formattedTime = "2:04 PM",
                    onAddMedicationClick = { addClicked = true },
                    onSmartVoiceMemoClick = { memoClicked = true }
                )
            }
        }

        // Date and time on the left
        composeTestRule.onNodeWithTag("hub_dashboard_time_bar").assertIsDisplayed()
        composeTestRule.onNodeWithTag("hub_dashboard_date_label").assertIsDisplayed()
        composeTestRule.onNodeWithText("11 SEP, 2026").assertIsDisplayed()
        composeTestRule.onNodeWithTag("hub_dashboard_current_time_text").assertIsDisplayed()
        composeTestRule.onNodeWithText("2:04 PM").assertIsDisplayed()

        // Quick action circular buttons on the right
        composeTestRule.onNodeWithTag("hub_dashboard_add_medication_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("hub_dashboard_smart_voice_memo_button").assertIsDisplayed()

        composeTestRule.onNodeWithTag("hub_dashboard_add_medication_button").performClick()
        assertTrue(addClicked)

        composeTestRule.onNodeWithTag("hub_dashboard_smart_voice_memo_button").performClick()
        assertTrue(memoClicked)
    }

    @Test
    fun testScheduleEmptyState_displaysReducedCopy() {
        var addMedClicked = false

        composeTestRule.setContent {
            MyApplicationTheme {
                MedicationBoard(
                    medications = emptyList(),
                    currentUserId = "user_123",
                    onToggleTaken = {},
                    onEditMedication = {},
                    onDeleteMedication = {},
                    onAddMedicationClick = { addMedClicked = true }
                )
            }
        }

        // Verify Schedule header
        composeTestRule.onNodeWithText("TODAY'S SCHEDULE").assertIsDisplayed()
        composeTestRule.onNodeWithText("MEDICATIONS").assertIsDisplayed()

        // Verify empty state messages
        composeTestRule.onNodeWithText("Your family's schedule\nstarts here.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Add a medication to\nget everyone on track.").assertIsDisplayed()

        // Verify in-card add button
        composeTestRule.onNodeWithTag("empty_state_add_medication_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("empty_state_add_medication_button").performClick()
        assertTrue(addMedClicked)
    }

    @Test
    fun testMedicationCard_takenStateAndMenuPopup() {
        var editClicked = false
        var deleteClicked = false
        var toggleTakenClicked = false

        val testMed = MedicationItem(
            id = "med_test_1",
            hubId = "hub_123",
            name = "Vitamin D3",
            dosage = "1 capsule",
            recipientId = "member_1",
            recipientName = "Sarah",
            recipientAvatarType = ProfileAvatarType.FEMALE,
            reminderTime = "9:00 AM",
            createdByUid = "user_creator",
            isTakenToday = true,
            takenAtTime = "9:15 AM"
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                MedicationCard(
                    medication = testMed,
                    isCreator = true,
                    onToggleTaken = { toggleTakenClicked = true },
                    onEditClick = { editClicked = true },
                    onDeleteClick = { deleteClicked = true }
                )
            }
        }

        // Verify Taken button shows "Taken" and time
        composeTestRule.onNodeWithTag("mark_as_taken_button_med_test_1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Taken").assertIsDisplayed()
        composeTestRule.onNodeWithText("9:15 AM").assertIsDisplayed()

        // Click Taken button
        composeTestRule.onNodeWithTag("mark_as_taken_button_med_test_1").performClick()
        assertTrue(toggleTakenClicked)

        // Open 3-dot popup
        composeTestRule.onNodeWithTag("medication_menu_button_med_test_1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("medication_menu_button_med_test_1").performClick()

        // Verify clean popup items
        composeTestRule.onNodeWithTag("edit_medication_menu_item").assertIsDisplayed()
        composeTestRule.onNodeWithText("Edit medication").assertIsDisplayed()
        composeTestRule.onNodeWithTag("delete_medication_menu_item").assertIsDisplayed()
        composeTestRule.onNodeWithText("Delete medication").assertIsDisplayed()

        // Click Edit
        composeTestRule.onNodeWithTag("edit_medication_menu_item").performClick()
        assertTrue(editClicked)
    }

    @Test
    fun testAddMedicationModal_compactTypographyAndCTA() {
        val testMember = HubMember(
            id = "m1",
            name = "Alice",
            avatarType = ProfileAvatarType.FEMALE
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                AddEditMedicationModal(
                    existingMedication = null,
                    approvedMembers = listOf(testMember),
                    currentUserId = "user_creator",
                    onDismiss = {},
                    onSaveMedication = { _, _, _, _, _, _, _, _, _, _ -> }
                )
            }
        }

        // Verify Heading is preserved
        composeTestRule.onNodeWithText("Add Medication").assertExists()

        // Verify reduced helper text
        composeTestRule.onNodeWithText("Keep your family's schedule on track.").assertExists()

        // Verify field labels
        composeTestRule.onNodeWithText("Medicine photo (optional)").assertExists()
        composeTestRule.onNodeWithText("+ Add medicine photo").assertExists()
        composeTestRule.onNodeWithText("Medicine name").assertExists()
        composeTestRule.onNodeWithText("For whom?").assertExists()
        composeTestRule.onNodeWithText("Dosage").assertExists()
        composeTestRule.onNodeWithText("Reminder time").assertExists()
        composeTestRule.onNodeWithText("Change").assertExists()
        composeTestRule.onNodeWithText("Reminder cycle").assertExists()
        composeTestRule.onNodeWithText("Notes (optional)").assertExists()

        // Verify Create Medication compact CTA
        composeTestRule.onNodeWithTag("create_medication_submit_button").assertExists()
        composeTestRule.onNodeWithText("Create Medication").assertExists()
    }
}
