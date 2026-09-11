package com.example

import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.reminder.data.ReminderStorage
import com.example.reminder.model.MedicationOccurrence
import com.example.reminder.model.ReminderStage
import com.example.reminder.notification.MedicationNotificationHelper
import com.example.reminder.scheduler.MedicationReminderScheduler
import com.example.ui.hub.dashboard.model.MedicationItem
import com.example.ui.hub.dashboard.model.ReminderCycle
import com.example.ui.profilesetup.model.ProfileAvatarType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MedicationReminderSystemTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testNotificationChannelCreatedWithHighImportance() {
        MedicationNotificationHelper.createNotificationChannel(context)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = manager.getNotificationChannel(MedicationNotificationHelper.CHANNEL_ID)

        assertNotNull(channel)
        assertEquals(NotificationManager.IMPORTANCE_HIGH, channel.importance)
        assertEquals("Medication Reminders", channel.name.toString())
    }

    @Test
    fun testOccurrenceStorageAndMarkTaken() {
        val testOcc = MedicationOccurrence(
            occurrenceId = "med_123_2026-09-11",
            medicationId = "med_123",
            hubId = "hub_abc",
            dateKey = "2026-09-11",
            medicationName = "Amoxicillin",
            dosage = "500 mg",
            recipientId = "user_1",
            recipientName = "Rahul",
            scheduledTimeMillis = 1000000L,
            missedReminderMillis = 1300000L,
            familyEscalationMillis = 1900000L,
            isTaken = false
        )

        ReminderStorage.saveOccurrences(context, listOf(testOcc))

        val loaded = ReminderStorage.loadOccurrences(context)
        assertEquals(1, loaded.size)
        assertEquals("Amoxicillin", loaded[0].medicationName)
        assertFalse(loaded[0].isTaken)

        ReminderStorage.markOccurrenceAsTaken(context, "med_123", "2026-09-11", "9:05 AM")
        val updated = ReminderStorage.getOccurrence(context, "med_123", "2026-09-11")
        assertNotNull(updated)
        assertTrue(updated!!.isTaken)
        assertEquals("9:05 AM", updated.takenAtFormatted)
    }

    @Test
    fun testAdultInitialNotificationDispatchesCorrectly() {
        val occurrence = MedicationOccurrence(
            occurrenceId = "med_adult_2026-09-11",
            medicationId = "med_adult",
            hubId = "hub_1",
            dateKey = "2026-09-11",
            medicationName = "Cetirizine",
            dosage = "1 tablet",
            recipientId = "user_1",
            recipientName = "Rahul",
            isChildRecipient = false,
            scheduledTimeMillis = System.currentTimeMillis() + 60000,
            missedReminderMillis = System.currentTimeMillis() + 360000,
            familyEscalationMillis = System.currentTimeMillis() + 960000
        )

        MedicationNotificationHelper.showInitialReminder(context, occurrence)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val shadowManager = shadowOf(notificationManager)
        val notifications = shadowManager.allNotifications

        assertTrue(notifications.isNotEmpty())
        val lastNotif = notifications.last()
        val shadowNotif = shadowOf(lastNotif)

        assertEquals("💊 Time for your medicine", shadowNotif.contentTitle)
        assertTrue(shadowNotif.contentText.toString().contains("It’s time to take your Cetirizine."))
        assertTrue(shadowNotif.contentText.toString().contains("Take 1 tablet and mark it as taken when you’re done."))
    }

    @Test
    fun testAdultMissedDosageAndEscalationCopy() {
        val occurrence = MedicationOccurrence(
            occurrenceId = "med_adult_2026-09-11",
            medicationId = "med_adult",
            hubId = "hub_1",
            dateKey = "2026-09-11",
            medicationName = "Cetirizine",
            dosage = "1 tablet",
            recipientId = "user_1",
            recipientName = "Rahul",
            isChildRecipient = false,
            scheduledTimeMillis = System.currentTimeMillis() + 60000,
            missedReminderMillis = System.currentTimeMillis() + 360000,
            familyEscalationMillis = System.currentTimeMillis() + 960000
        )

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val shadowManager = shadowOf(notificationManager)

        // Missed reminder test
        notificationManager.cancelAll()
        MedicationNotificationHelper.showMissedDosageReminder(context, occurrence)
        val missedNotif = shadowOf(shadowManager.allNotifications.last())
        assertEquals("⏰ Medicine reminder", missedNotif.contentTitle)
        assertTrue(missedNotif.contentText.toString().contains("Have you taken your Cetirizine?"))

        // Family escalation to self
        notificationManager.cancelAll()
        MedicationNotificationHelper.showFamilyEscalationToOriginalUser(context, occurrence)
        val selfEscalation = shadowOf(shadowManager.allNotifications.last())
        assertEquals("🔔 Your family has been notified", selfEscalation.contentTitle)
        assertTrue(selfEscalation.contentText.toString().contains("Your Cetirizine hasn’t been marked as taken yet."))

        // Family notification to members
        notificationManager.cancelAll()
        MedicationNotificationHelper.showFamilyEscalationToMembers(context, occurrence)
        val memberEscalation = shadowOf(shadowManager.allNotifications.last())
        assertEquals("👨👩👧 Family reminder", memberEscalation.contentTitle)
        assertTrue(memberEscalation.contentText.toString().contains("Rahul hasn’t marked their Cetirizine as taken yet."))
    }

    @Test
    fun testChildNotificationDispatchesChildSpecificCopy() {
        val occurrence = MedicationOccurrence(
            occurrenceId = "med_child_2026-09-11",
            medicationId = "med_child",
            hubId = "hub_1",
            dateKey = "2026-09-11",
            medicationName = "Cetirizine",
            dosage = "1 tablet",
            recipientId = "child_1",
            recipientName = "Aarav",
            isChildRecipient = true,
            reminderResponsibleUid = "parent_1",
            scheduledTimeMillis = System.currentTimeMillis() + 60000,
            missedReminderMillis = System.currentTimeMillis() + 360000,
            familyEscalationMillis = System.currentTimeMillis() + 960000
        )

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val shadowManager = shadowOf(notificationManager)

        // Child Initial Reminder
        notificationManager.cancelAll()
        MedicationNotificationHelper.showInitialReminder(context, occurrence)
        val initialNotif = shadowOf(shadowManager.allNotifications.last())
        assertEquals("💊 Aarav’s medicine time", initialNotif.contentTitle)
        assertTrue(initialNotif.contentText.toString().contains("It’s time for Aarav to take their Cetirizine."))
        assertTrue(initialNotif.contentText.toString().contains("Give them 1 tablet and mark it as taken when they’re done."))

        // Child Missed Reminder
        notificationManager.cancelAll()
        MedicationNotificationHelper.showMissedDosageReminder(context, occurrence)
        val missedNotif = shadowOf(shadowManager.allNotifications.last())
        assertEquals("⏰ Aarav’s medicine reminder", missedNotif.contentTitle)
        assertTrue(missedNotif.contentText.toString().contains("Aarav’s Cetirizine hasn’t been marked as taken yet."))

        // Child Responsible Member after Family Escalation
        notificationManager.cancelAll()
        MedicationNotificationHelper.showFamilyEscalationToOriginalUser(context, occurrence)
        val respNotif = shadowOf(shadowManager.allNotifications.last())
        assertEquals("🔔 Family has been notified", respNotif.contentTitle)
        assertTrue(respNotif.contentText.toString().contains("We’ve let your family know about Aarav’s Cetirizine."))

        // Child Family Notification to Members
        notificationManager.cancelAll()
        MedicationNotificationHelper.showFamilyEscalationToMembers(context, occurrence)
        val familyNotif = shadowOf(shadowManager.allNotifications.last())
        assertEquals("👨👩👧 Family reminder", familyNotif.contentTitle)
        assertTrue(familyNotif.contentText.toString().contains("Aarav hasn’t taken their Cetirizine yet."))
    }

    @Test
    fun testMedicationScheduleCalculation() {
        val med = MedicationItem(
            id = "med_vitamin",
            hubId = "hub_1",
            name = "Vitamin C",
            dosage = "1000 mg",
            recipientId = "user_1",
            recipientName = "Rahul",
            recipientAvatarType = ProfileAvatarType.MALE,
            reminderTime = "9:00 AM",
            reminderHour = 9,
            reminderMinute = 0,
            reminderCycle = ReminderCycle.EVERY_24_HOURS,
            createdByUid = "user_1"
        )

        val cal = Calendar.getInstance()
        val isScheduled = MedicationReminderScheduler.isMedicationScheduledForDate(med, cal)
        assertTrue(isScheduled)
    }
}
