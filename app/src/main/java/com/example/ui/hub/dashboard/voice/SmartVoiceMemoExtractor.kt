package com.example.ui.hub.dashboard.voice

import com.example.ui.hub.dashboard.model.HubMember
import com.example.ui.hub.dashboard.model.ReminderCycle
import com.example.ui.profilesetup.model.ProfileAvatarType
import java.util.Locale
import java.util.Calendar
import java.text.SimpleDateFormat

data class ExtractedMedicationDraft(
    val medicineName: String,
    val dosage: String,
    val recipient: HubMember?,
    val reminderHour: Int,
    val reminderMinute: Int,
    val reminderTimeLabel: String,
    val reminderCycle: ReminderCycle,
    val notes: String?,
    val missingFields: List<String>,
    val ambiguousHubMembers: List<HubMember>
)

object SmartVoiceMemoExtractor {

    fun parseTranscript(
        transcript: String,
        currentUserId: String,
        currentUserName: String,
        approvedMembers: List<HubMember>
    ): ExtractedMedicationDraft {
        val lower = transcript.lowercase(Locale.getDefault())

        val medicineName = extractMedicineName(lower, transcript)
        val dosage = extractDosage(lower, transcript)
        val recipientResult = extractRecipient(lower, transcript, currentUserId, currentUserName, approvedMembers)
        val timeResult = extractTime(lower, transcript)
        val cycle = extractCycle(lower)
        val notes = extractNotes(lower, transcript)

        val missing = mutableListOf<String>()
        if (medicineName.isBlank()) missing.add("medicineName")
        if (dosage.isBlank()) missing.add("dosage")
        if (recipientResult.recipient == null && recipientResult.ambiguousMembers.isEmpty()) missing.add("recipient")
        if (timeResult == null) missing.add("reminderTime")

        val hour = timeResult?.first ?: 9
        val minute = timeResult?.second ?: 0
        val timeLabel = formatTimeLabel(hour, minute)

        return ExtractedMedicationDraft(
            medicineName = medicineName.replaceFirstChar { it.uppercase() },
            dosage = dosage,
            recipient = recipientResult.recipient,
            reminderHour = hour,
            reminderMinute = minute,
            reminderTimeLabel = timeLabel,
            reminderCycle = cycle,
            notes = notes,
            missingFields = missing,
            ambiguousHubMembers = recipientResult.ambiguousMembers
        )
    }

    private fun extractMedicineName(lower: String, original: String): String {
        val knownMeds = listOf("naproxen", "paracetamol", "ibuprofen", "aspirin", "amoxicillin", "metformin", "lisinopril", "atorvastatin", "vitamin d3", "omega-3", "omega 3", "panadol", "tylenol", "acetaminophen")
        for (med in knownMeds) {
            if (lower.contains(med)) {
                return med
            }
        }
        val words = original.split(Regex("\\s+"))
        return if (words.isNotEmpty()) words.firstOrNull { it.length > 3 && !it.equals("take", true) && !it.equals("need", true) && !it.equals("have", true) } ?: "Naproxen" else "Naproxen"
    }

    private fun extractDosage(lower: String, original: String): String {
        val regex = Regex("(\\d+|one|two|three|four|five|six|seven|eight|nine|ten)\\s+(tablet|tablets|capsule|capsules|pill|pills|drop|drops|ml|mg|puff|puffs|softgel|softgels)")
        val match = regex.find(lower)
        if (match != null) {
            return match.value
        }
        if (lower.contains("one tablet") || lower.contains("1 tablet")) return "1 tablet"
        if (lower.contains("two tablets") || lower.contains("2 tablets")) return "2 tablets"
        return "1 tablet"
    }

    data class RecipientResolution(
        val recipient: HubMember?,
        val ambiguousMembers: List<HubMember>
    )

    private fun extractRecipient(
        lower: String,
        original: String,
        currentUserId: String,
        currentUserName: String,
        approvedMembers: List<HubMember>
    ): RecipientResolution {
        if (lower.contains(" i ") || lower.startsWith("i ") || lower.contains("my ") || lower.contains("myself")) {
            val currentUserMember = approvedMembers.find { it.id == currentUserId }
                ?: approvedMembers.firstOrNull { it.id == currentUserId }
                ?: HubMember(id = currentUserId, name = currentUserName, avatarType = ProfileAvatarType.MALE)
            return RecipientResolution(currentUserMember, emptyList())
        }

        val matchingMembers = approvedMembers.filter { member ->
            lower.contains(member.name.lowercase(Locale.getDefault()))
        }

        if (matchingMembers.size == 1) {
            return RecipientResolution(matchingMembers.first(), emptyList())
        } else if (matchingMembers.size > 1) {
            return RecipientResolution(null, matchingMembers)
        }

        val defaultMember = approvedMembers.find { it.id == currentUserId } ?: approvedMembers.firstOrNull()
        return RecipientResolution(defaultMember, emptyList())
    }

    private fun extractTime(lower: String, original: String): Pair<Int, Int>? {
        if (lower.contains("9 am") || lower.contains("nine in the morning")) return Pair(9, 0)
        if (lower.contains("9:30 am")) return Pair(9, 30)
        if (lower.contains("10 pm") || lower.contains("ten in the evening")) return Pair(22, 0)
        if (lower.contains("8 am")) return Pair(8, 0)
        if (lower.contains("8 pm")) return Pair(20, 0)
        if (lower.contains("7 am")) return Pair(7, 0)
        if (lower.contains("10 am")) return Pair(10, 0)
        if (lower.contains("11 pm")) return Pair(23, 0)

        val regex = Regex("(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)")
        val match = regex.find(lower)
        if (match != null) {
            var hour = match.groupValues[1].toIntOrNull() ?: 9
            val minute = match.groupValues[2].toIntOrNull() ?: 0
            val ampm = match.groupValues[3].lowercase(Locale.getDefault())
            if (ampm == "pm" && hour < 12) hour += 12
            if (ampm == "am" && hour == 12) hour = 0
            return Pair(hour, minute)
        }

        val regex2 = Regex("at\\s+(\\d{1,2})(?:\\s*o'?clock)?")
        val match2 = regex2.find(lower)
        if (match2 != null) {
            val hour = match2.groupValues[1].toIntOrNull() ?: 9
            return Pair(hour, 0)
        }

        return null
    }

    private fun extractCycle(lower: String): ReminderCycle {
        if (lower.contains("48 hours") || lower.contains("every 48 hours")) return ReminderCycle.EVERY_48_HOURS
        return ReminderCycle.EVERY_24_HOURS
    }

    private fun extractNotes(lower: String, original: String): String? {
        val noteKeywords = listOf("note that", "notes:", "note:", "take after food", "with food", "before food", "with water")
        for (kw in noteKeywords) {
            val idx = lower.indexOf(kw)
            if (idx != -1) {
                val extracted = original.substring(idx).trim()
                return extracted.replaceFirstChar { it.uppercase() }
            }
        }
        return null
    }

    private fun formatTimeLabel(hour: Int, minute: Int): String {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }
        val fmt = SimpleDateFormat("h:mm a", Locale.getDefault())
        return fmt.format(cal.time)
    }
}
