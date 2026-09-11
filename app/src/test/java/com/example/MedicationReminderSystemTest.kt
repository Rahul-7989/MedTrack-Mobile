package com.example

import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.reminder.data.ReminderStorage
import com.example.reminder.model.MedicationOccurrence
import com.example.reminder.model.ReminderStage
import com.example.reminder.notification.MedicationNotificationHelper
import com.example.reminder.receiver.MedicationReminderReceiver
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

    @Test
    fun testScenario1And2And3_MedicationCreatedByRahulForAnanya() {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val shadowManager = shadowOf(notificationManager)

        // Setup Hub A with members Rahul, Ananya, Arjun, Priya
        val hubA = com.example.ui.hub.model.FamilyHubData(
            hubId = "hub_A",
            name = "Rahul's Fam",
            hiveCode = "FAM123",
            createdByUid = "rahul_uid"
        )
        com.example.ui.hub.data.FamilyHubRepository.registerLocalHub(hubA, listOf("rahul_uid", "ananya_uid", "arjun_uid", "priya_uid"))

        val intent = android.content.Intent(MedicationReminderScheduler.ACTION_TRIGGER_REMINDER).apply {
            putExtra(MedicationReminderScheduler.EXTRA_HUB_ID, "hub_A")
            putExtra(MedicationReminderScheduler.EXTRA_MEDICATION_ID, "med_cetirizine")
            putExtra(MedicationReminderScheduler.EXTRA_OCCURRENCE_ID, "occ_cetirizine_1")
            putExtra(MedicationReminderScheduler.EXTRA_DATE_KEY, "2026-09-11")
            putExtra(MedicationReminderScheduler.EXTRA_MED_NAME, "Cetirizine")
            putExtra(MedicationReminderScheduler.EXTRA_DOSAGE, "1 tablet")
            putExtra(MedicationReminderScheduler.EXTRA_RECIPIENT_ID, "ananya_uid")
            putExtra(MedicationReminderScheduler.EXTRA_RECIPIENT_NAME, "Ananya")
            putExtra(MedicationReminderScheduler.EXTRA_IS_CHILD, false)
        }

        val receiver = com.example.reminder.receiver.MedicationReminderReceiver()

        // TEST 1: At Initial reminder time, check Ananya's device vs Rahul's device vs Arjun's device
        intent.putExtra(MedicationReminderScheduler.EXTRA_STAGE, ReminderStage.INITIAL.name)

        // On Rahul's device (creator, but NOT recipient):
        MedicationReminderReceiver.testCurrentUserIdOverride = "rahul_uid"
        notificationManager.cancelAll()
        receiver.onReceive(context, intent)
        assertEquals(0, shadowManager.allNotifications.size)

        // On Arjun's device (hub member, but NOT recipient):
        MedicationReminderReceiver.testCurrentUserIdOverride = "arjun_uid"
        notificationManager.cancelAll()
        receiver.onReceive(context, intent)
        assertEquals(0, shadowManager.allNotifications.size)

        // On Ananya's device (assigned recipient):
        MedicationReminderReceiver.testCurrentUserIdOverride = "ananya_uid"
        notificationManager.cancelAll()
        receiver.onReceive(context, intent)
        assertEquals(1, shadowManager.allNotifications.size)
        val ananyaNotif = shadowOf(shadowManager.allNotifications.last())
        assertEquals("💊 Time for your medicine", ananyaNotif.contentTitle)
        assertTrue(ananyaNotif.contentText.toString().contains("It’s time to take your Cetirizine."))

        // TEST 2: At Missed-dose time, check Ananya's device vs others
        intent.putExtra(MedicationReminderScheduler.EXTRA_STAGE, ReminderStage.MISSED.name)

        // Rahul's device receives NOTHING
        MedicationReminderReceiver.testCurrentUserIdOverride = "rahul_uid"
        notificationManager.cancelAll()
        receiver.onReceive(context, intent)
        assertEquals(0, shadowManager.allNotifications.size)

        // Ananya's device receives Missed Dose alert
        MedicationReminderReceiver.testCurrentUserIdOverride = "ananya_uid"
        notificationManager.cancelAll()
        receiver.onReceive(context, intent)
        assertEquals(1, shadowManager.allNotifications.size)
        val ananyaMissed = shadowOf(shadowManager.allNotifications.last())
        assertEquals("⏰ Medicine reminder", ananyaMissed.contentTitle)
        assertTrue(ananyaMissed.contentText.toString().contains("Have you taken your Cetirizine?"))

        // TEST 3: At Family Escalation:
        intent.putExtra(MedicationReminderScheduler.EXTRA_STAGE, ReminderStage.FAMILY_ESCALATION.name)

        // Ananya receives personal "🔔 Your family has been notified"
        MedicationReminderReceiver.testCurrentUserIdOverride = "ananya_uid"
        notificationManager.cancelAll()
        ReminderStorage.clearAll(context)
        receiver.onReceive(context, intent)
        assertEquals(1, shadowManager.allNotifications.size)
        val ananyaPersonal = shadowOf(shadowManager.allNotifications.last())
        assertEquals("🔔 Your family has been notified", ananyaPersonal.contentTitle)
        assertTrue(ananyaPersonal.contentText.toString().contains("Your Cetirizine hasn’t been marked as taken yet."))

        // Rahul (hub member on his separate device) receives broad "👨👩👧 Family reminder", NOT personal notification
        MedicationReminderReceiver.testCurrentUserIdOverride = "rahul_uid"
        notificationManager.cancelAll()
        ReminderStorage.clearAll(context)
        receiver.onReceive(context, intent)
        assertEquals(1, shadowManager.allNotifications.size)
        val rahulEscalation = shadowOf(shadowManager.allNotifications.last())
        assertEquals("👨👩👧 Family reminder", rahulEscalation.contentTitle)
        assertTrue(rahulEscalation.contentText.toString().contains("Ananya hasn’t marked their Cetirizine as taken yet."))

        // Clean up
        MedicationReminderReceiver.testCurrentUserIdOverride = null
    }

    @Test
    fun testScenario4_MedicationCreatedByAnanyaForRahul() {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val shadowManager = shadowOf(notificationManager)

        val hubA = com.example.ui.hub.model.FamilyHubData(
            hubId = "hub_A",
            name = "Rahul's Fam",
            hiveCode = "FAM123",
            createdByUid = "ananya_uid"
        )
        com.example.ui.hub.data.FamilyHubRepository.registerLocalHub(hubA, listOf("rahul_uid", "ananya_uid"))

        val intent = android.content.Intent(MedicationReminderScheduler.ACTION_TRIGGER_REMINDER).apply {
            putExtra(MedicationReminderScheduler.EXTRA_HUB_ID, "hub_A")
            putExtra(MedicationReminderScheduler.EXTRA_MEDICATION_ID, "med_rahul_dose")
            putExtra(MedicationReminderScheduler.EXTRA_OCCURRENCE_ID, "occ_rahul_1")
            putExtra(MedicationReminderScheduler.EXTRA_DATE_KEY, "2026-09-11")
            putExtra(MedicationReminderScheduler.EXTRA_MED_NAME, "Cetirizine")
            putExtra(MedicationReminderScheduler.EXTRA_DOSAGE, "1 tablet")
            putExtra(MedicationReminderScheduler.EXTRA_RECIPIENT_ID, "rahul_uid")
            putExtra(MedicationReminderScheduler.EXTRA_RECIPIENT_NAME, "Rahul")
            putExtra(MedicationReminderScheduler.EXTRA_IS_CHILD, false)
        }

        val receiver = com.example.reminder.receiver.MedicationReminderReceiver()

        // 1. Initial reminder: Rahul receives, Ananya does NOT
        intent.putExtra(MedicationReminderScheduler.EXTRA_STAGE, ReminderStage.INITIAL.name)
        MedicationReminderReceiver.testCurrentUserIdOverride = "ananya_uid"
        notificationManager.cancelAll()
        receiver.onReceive(context, intent)
        assertEquals(0, shadowManager.allNotifications.size)

        MedicationReminderReceiver.testCurrentUserIdOverride = "rahul_uid"
        notificationManager.cancelAll()
        receiver.onReceive(context, intent)
        assertEquals(1, shadowManager.allNotifications.size)
        assertEquals("💊 Time for your medicine", shadowOf(shadowManager.allNotifications.last()).contentTitle)

        // 2. Missed dose: Rahul receives, Ananya does NOT
        intent.putExtra(MedicationReminderScheduler.EXTRA_STAGE, ReminderStage.MISSED.name)
        MedicationReminderReceiver.testCurrentUserIdOverride = "ananya_uid"
        notificationManager.cancelAll()
        receiver.onReceive(context, intent)
        assertEquals(0, shadowManager.allNotifications.size)

        MedicationReminderReceiver.testCurrentUserIdOverride = "rahul_uid"
        notificationManager.cancelAll()
        receiver.onReceive(context, intent)
        assertEquals(1, shadowManager.allNotifications.size)
        assertEquals("⏰ Medicine reminder", shadowOf(shadowManager.allNotifications.last()).contentTitle)

        // 3. Family escalation: Rahul receives personal alert, Ananya receives family alert
        intent.putExtra(MedicationReminderScheduler.EXTRA_STAGE, ReminderStage.FAMILY_ESCALATION.name)
        MedicationReminderReceiver.testCurrentUserIdOverride = "rahul_uid"
        notificationManager.cancelAll()
        ReminderStorage.clearAll(context)
        receiver.onReceive(context, intent)
        assertEquals(1, shadowManager.allNotifications.size)
        assertEquals("🔔 Your family has been notified", shadowOf(shadowManager.allNotifications.last()).contentTitle)

        MedicationReminderReceiver.testCurrentUserIdOverride = "ananya_uid"
        notificationManager.cancelAll()
        ReminderStorage.clearAll(context)
        receiver.onReceive(context, intent)
        assertEquals(1, shadowManager.allNotifications.size)
        assertEquals("👨👩👧 Family reminder", shadowOf(shadowManager.allNotifications.last()).contentTitle)

        MedicationReminderReceiver.testCurrentUserIdOverride = null
    }

    @Test
    fun testScenario5And6_ChildMedicationRecipientRouting() {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val shadowManager = shadowOf(notificationManager)

        val hubA = com.example.ui.hub.model.FamilyHubData(
            hubId = "hub_A",
            name = "Family Hub",
            hiveCode = "FAM999",
            createdByUid = "rahul_uid"
        )
        com.example.ui.hub.data.FamilyHubRepository.registerLocalHub(hubA, listOf("rahul_uid", "ananya_uid"))

        // Child medication with responsible member = Rahul
        val intentChildRahul = android.content.Intent(MedicationReminderScheduler.ACTION_TRIGGER_REMINDER).apply {
            putExtra(MedicationReminderScheduler.EXTRA_HUB_ID, "hub_A")
            putExtra(MedicationReminderScheduler.EXTRA_MEDICATION_ID, "med_aarav_syrup")
            putExtra(MedicationReminderScheduler.EXTRA_OCCURRENCE_ID, "occ_aarav_1")
            putExtra(MedicationReminderScheduler.EXTRA_DATE_KEY, "2026-09-11")
            putExtra(MedicationReminderScheduler.EXTRA_MED_NAME, "Cetirizine")
            putExtra(MedicationReminderScheduler.EXTRA_DOSAGE, "1 tablet")
            putExtra(MedicationReminderScheduler.EXTRA_RECIPIENT_ID, "aarav_child_id")
            putExtra(MedicationReminderScheduler.EXTRA_RECIPIENT_NAME, "Aarav")
            putExtra(MedicationReminderScheduler.EXTRA_RESPONSIBLE_UID, "rahul_uid")
            putExtra(MedicationReminderScheduler.EXTRA_IS_CHILD, true)
            putExtra(MedicationReminderScheduler.EXTRA_STAGE, ReminderStage.INITIAL.name)
        }

        val receiver = com.example.reminder.receiver.MedicationReminderReceiver()

        // TEST 5: Responsible member = Rahul
        // Initial reminder -> ONLY Rahul receives
        MedicationReminderReceiver.testCurrentUserIdOverride = "ananya_uid"
        notificationManager.cancelAll()
        receiver.onReceive(context, intentChildRahul)
        assertEquals(0, shadowManager.allNotifications.size)

        MedicationReminderReceiver.testCurrentUserIdOverride = "rahul_uid"
        notificationManager.cancelAll()
        receiver.onReceive(context, intentChildRahul)
        assertEquals(1, shadowManager.allNotifications.size)
        assertEquals("💊 Aarav’s medicine time", shadowOf(shadowManager.allNotifications.last()).contentTitle)

        // TEST 6: Child medication with responsible member = Ananya
        val intentChildAnanya = android.content.Intent(MedicationReminderScheduler.ACTION_TRIGGER_REMINDER).apply {
            putExtra(MedicationReminderScheduler.EXTRA_HUB_ID, "hub_A")
            putExtra(MedicationReminderScheduler.EXTRA_MEDICATION_ID, "med_aarav_syrup_2")
            putExtra(MedicationReminderScheduler.EXTRA_OCCURRENCE_ID, "occ_aarav_2")
            putExtra(MedicationReminderScheduler.EXTRA_DATE_KEY, "2026-09-11")
            putExtra(MedicationReminderScheduler.EXTRA_MED_NAME, "Cetirizine")
            putExtra(MedicationReminderScheduler.EXTRA_DOSAGE, "1 tablet")
            putExtra(MedicationReminderScheduler.EXTRA_RECIPIENT_ID, "aarav_child_id")
            putExtra(MedicationReminderScheduler.EXTRA_RECIPIENT_NAME, "Aarav")
            putExtra(MedicationReminderScheduler.EXTRA_RESPONSIBLE_UID, "ananya_uid")
            putExtra(MedicationReminderScheduler.EXTRA_IS_CHILD, true)
            putExtra(MedicationReminderScheduler.EXTRA_STAGE, ReminderStage.INITIAL.name)
        }

        // Rahul must NOT receive child notifications when Ananya is responsible member
        MedicationReminderReceiver.testCurrentUserIdOverride = "rahul_uid"
        notificationManager.cancelAll()
        receiver.onReceive(context, intentChildAnanya)
        assertEquals(0, shadowManager.allNotifications.size)

        MedicationReminderReceiver.testCurrentUserIdOverride = "ananya_uid"
        notificationManager.cancelAll()
        receiver.onReceive(context, intentChildAnanya)
        assertEquals(1, shadowManager.allNotifications.size)
        assertEquals("💊 Aarav’s medicine time", shadowOf(shadowManager.allNotifications.last()).contentTitle)

        MedicationReminderReceiver.testCurrentUserIdOverride = null
    }

    @Test
    fun testScenario7_MultipleHubsSeparation() {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val shadowManager = shadowOf(notificationManager)

        // Hub A has Rahul and Ananya. Hub B has Vikram and Sneha.
        val hubA = com.example.ui.hub.model.FamilyHubData(
            hubId = "hub_A",
            name = "Hub A",
            hiveCode = "HUBAAA",
            createdByUid = "rahul_uid"
        )
        val hubB = com.example.ui.hub.model.FamilyHubData(
            hubId = "hub_B",
            name = "Hub B",
            hiveCode = "HUBBBB",
            createdByUid = "vikram_uid"
        )
        com.example.ui.hub.data.FamilyHubRepository.registerLocalHub(hubA, listOf("rahul_uid", "ananya_uid"))
        com.example.ui.hub.data.FamilyHubRepository.registerLocalHub(hubB, listOf("vikram_uid", "sneha_uid"))

        // Medication belongs to Hub A
        val intent = android.content.Intent(MedicationReminderScheduler.ACTION_TRIGGER_REMINDER).apply {
            putExtra(MedicationReminderScheduler.EXTRA_HUB_ID, "hub_A")
            putExtra(MedicationReminderScheduler.EXTRA_MEDICATION_ID, "med_hub_a")
            putExtra(MedicationReminderScheduler.EXTRA_OCCURRENCE_ID, "occ_hub_a")
            putExtra(MedicationReminderScheduler.EXTRA_DATE_KEY, "2026-09-11")
            putExtra(MedicationReminderScheduler.EXTRA_MED_NAME, "Cetirizine")
            putExtra(MedicationReminderScheduler.EXTRA_DOSAGE, "1 tablet")
            putExtra(MedicationReminderScheduler.EXTRA_RECIPIENT_ID, "rahul_uid")
            putExtra(MedicationReminderScheduler.EXTRA_RECIPIENT_NAME, "Rahul")
            putExtra(MedicationReminderScheduler.EXTRA_IS_CHILD, false)
            putExtra(MedicationReminderScheduler.EXTRA_STAGE, ReminderStage.FAMILY_ESCALATION.name)
        }

        val receiver = com.example.reminder.receiver.MedicationReminderReceiver()

        // Vikram is in Hub B, NOT Hub A -> Vikram receives NOTHING
        MedicationReminderReceiver.testCurrentUserIdOverride = "vikram_uid"
        notificationManager.cancelAll()
        receiver.onReceive(context, intent)
        assertEquals(0, shadowManager.allNotifications.size)

        // Ananya is in Hub A -> Ananya receives family reminder
        MedicationReminderReceiver.testCurrentUserIdOverride = "ananya_uid"
        notificationManager.cancelAll()
        receiver.onReceive(context, intent)
        assertEquals(1, shadowManager.allNotifications.size)
        assertEquals("👨👩👧 Family reminder", shadowOf(shadowManager.allNotifications.last()).contentTitle)

        MedicationReminderReceiver.testCurrentUserIdOverride = null
    }

    @Test
    fun testScenario8And9_MultipleDevicesAndCreatorExclusion() {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val shadowManager = shadowOf(notificationManager)

        val hubA = com.example.ui.hub.model.FamilyHubData(
            hubId = "hub_A",
            name = "Hub A",
            hiveCode = "HUBAAA",
            createdByUid = "creator_uid"
        )
        com.example.ui.hub.data.FamilyHubRepository.registerLocalHub(hubA, listOf("creator_uid", "rahul_uid"))

        val intent = android.content.Intent(MedicationReminderScheduler.ACTION_TRIGGER_REMINDER).apply {
            putExtra(MedicationReminderScheduler.EXTRA_HUB_ID, "hub_A")
            putExtra(MedicationReminderScheduler.EXTRA_MEDICATION_ID, "med_test")
            putExtra(MedicationReminderScheduler.EXTRA_OCCURRENCE_ID, "occ_test_devices")
            putExtra(MedicationReminderScheduler.EXTRA_DATE_KEY, "2026-09-11")
            putExtra(MedicationReminderScheduler.EXTRA_MED_NAME, "Cetirizine")
            putExtra(MedicationReminderScheduler.EXTRA_DOSAGE, "1 tablet")
            putExtra(MedicationReminderScheduler.EXTRA_RECIPIENT_ID, "rahul_uid")
            putExtra(MedicationReminderScheduler.EXTRA_RECIPIENT_NAME, "Rahul")
            putExtra(MedicationReminderScheduler.EXTRA_IS_CHILD, false)
            putExtra(MedicationReminderScheduler.EXTRA_STAGE, ReminderStage.INITIAL.name)
        }

        val receiver = com.example.reminder.receiver.MedicationReminderReceiver()

        // Creator receives NOTHING
        MedicationReminderReceiver.testCurrentUserIdOverride = "creator_uid"
        notificationManager.cancelAll()
        receiver.onReceive(context, intent)
        assertEquals(0, shadowManager.allNotifications.size)

        // Rahul's Phone (logged in as rahul_uid) receives notification
        MedicationReminderReceiver.testCurrentUserIdOverride = "rahul_uid"
        notificationManager.cancelAll()
        ReminderStorage.clearAll(context)
        receiver.onReceive(context, intent)
        assertEquals(1, shadowManager.allNotifications.size)

        // Rahul's Tablet (also logged in as rahul_uid) receives notification
        MedicationReminderReceiver.testCurrentUserIdOverride = "rahul_uid"
        notificationManager.cancelAll()
        ReminderStorage.clearAll(context)
        receiver.onReceive(context, intent)
        assertEquals(1, shadowManager.allNotifications.size)

        MedicationReminderReceiver.testCurrentUserIdOverride = null
    }

    @Test
    fun testScenario10_MarkAsTakenSuppressesSubsequentNotifications() {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val shadowManager = shadowOf(notificationManager)

        val occurrence = MedicationOccurrence(
            occurrenceId = "med_taken_test_2026-09-11",
            medicationId = "med_taken_test",
            hubId = "hub_A",
            dateKey = "2026-09-11",
            medicationName = "Cetirizine",
            dosage = "1 tablet",
            recipientId = "rahul_uid",
            recipientName = "Rahul",
            isChildRecipient = false,
            scheduledTimeMillis = System.currentTimeMillis() - 10000,
            missedReminderMillis = System.currentTimeMillis() + 300000,
            familyEscalationMillis = System.currentTimeMillis() + 900000,
            isTaken = true,
            takenAtFormatted = "9:00 AM"
        )
        ReminderStorage.saveOccurrences(context, listOf(occurrence))

        val intent = android.content.Intent(MedicationReminderScheduler.ACTION_TRIGGER_REMINDER).apply {
            putExtra(MedicationReminderScheduler.EXTRA_HUB_ID, "hub_A")
            putExtra(MedicationReminderScheduler.EXTRA_MEDICATION_ID, "med_taken_test")
            putExtra(MedicationReminderScheduler.EXTRA_OCCURRENCE_ID, "med_taken_test_2026-09-11")
            putExtra(MedicationReminderScheduler.EXTRA_DATE_KEY, "2026-09-11")
            putExtra(MedicationReminderScheduler.EXTRA_MED_NAME, "Cetirizine")
            putExtra(MedicationReminderScheduler.EXTRA_DOSAGE, "1 tablet")
            putExtra(MedicationReminderScheduler.EXTRA_RECIPIENT_ID, "rahul_uid")
            putExtra(MedicationReminderScheduler.EXTRA_RECIPIENT_NAME, "Rahul")
            putExtra(MedicationReminderScheduler.EXTRA_IS_CHILD, false)
            putExtra(MedicationReminderScheduler.EXTRA_STAGE, ReminderStage.MISSED.name)
        }

        val receiver = com.example.reminder.receiver.MedicationReminderReceiver()
        MedicationReminderReceiver.testCurrentUserIdOverride = "rahul_uid"
        notificationManager.cancelAll()
        receiver.onReceive(context, intent)

        // Because medication is marked taken, 0 notifications should be posted
        assertEquals(0, shadowManager.allNotifications.size)

        MedicationReminderReceiver.testCurrentUserIdOverride = null
    }
}
