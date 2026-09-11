package com.example.reminder.data

import android.content.Context
import android.content.SharedPreferences
import com.example.reminder.model.MedicationOccurrence
import com.example.reminder.model.ReminderStage
import com.example.ui.profilesetup.model.ProfileAvatarType
import org.json.JSONArray
import org.json.JSONObject

/**
 * Local persistent store for medication occurrences, scheduled alarms, and taken statuses.
 * Enables instant retrieval for AlarmManager broadcasts and recovery on BOOT_COMPLETED.
 */
object ReminderStorage {

    private const val PREFS_NAME = "medtrack_reminders_store"
    private const val KEY_OCCURRENCES = "key_occurrences_json"
    private const val KEY_NOTIFIED_STAGES = "key_notified_stages"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Saves or replaces the list of active occurrences in storage.
     */
    fun saveOccurrences(context: Context, occurrences: List<MedicationOccurrence>) {
        val array = JSONArray()
        for (occ in occurrences) {
            val obj = JSONObject().apply {
                put("occurrenceId", occ.occurrenceId)
                put("medicationId", occ.medicationId)
                put("hubId", occ.hubId)
                put("dateKey", occ.dateKey)
                put("medicationName", occ.medicationName)
                put("dosage", occ.dosage)
                put("recipientId", occ.recipientId)
                put("recipientName", occ.recipientName)
                put("recipientAvatarType", occ.recipientAvatarType.name)
                put("reminderTimeFormatted", occ.reminderTimeFormatted)
                put("reminderHour", occ.reminderHour)
                put("reminderMinute", occ.reminderMinute)
                put("scheduledTimeMillis", occ.scheduledTimeMillis)
                put("missedReminderMillis", occ.missedReminderMillis)
                put("familyEscalationMillis", occ.familyEscalationMillis)
                put("reminderResponsibleUid", occ.reminderResponsibleUid ?: "")
                put("isChildRecipient", occ.isChildRecipient)
                put("isTaken", occ.isTaken)
                put("takenAtFormatted", occ.takenAtFormatted ?: "")
                put("takenAtMillis", occ.takenAtMillis ?: 0L)
                put("lastNotifiedStage", occ.lastNotifiedStage?.name ?: "")
            }
            array.put(obj)
        }
        getPrefs(context).edit().putString(KEY_OCCURRENCES, array.toString()).apply()
    }

    /**
     * Loads all stored occurrences.
     */
    fun loadOccurrences(context: Context): List<MedicationOccurrence> {
        val jsonStr = getPrefs(context).getString(KEY_OCCURRENCES, null) ?: return emptyList()
        val list = mutableListOf<MedicationOccurrence>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val avatarStr = obj.optString("recipientAvatarType", "MALE")
                val avatarType = try {
                    ProfileAvatarType.valueOf(avatarStr)
                } catch (_: Exception) {
                    ProfileAvatarType.MALE
                }
                val stageStr = obj.optString("lastNotifiedStage")
                val stage = try {
                    if (stageStr.isNotBlank()) ReminderStage.valueOf(stageStr) else null
                } catch (_: Exception) {
                    null
                }

                list.add(
                    MedicationOccurrence(
                        occurrenceId = obj.getString("occurrenceId"),
                        medicationId = obj.getString("medicationId"),
                        hubId = obj.getString("hubId"),
                        dateKey = obj.getString("dateKey"),
                        medicationName = obj.getString("medicationName"),
                        dosage = obj.getString("dosage"),
                        recipientId = obj.getString("recipientId"),
                        recipientName = obj.getString("recipientName"),
                        recipientAvatarType = avatarType,
                        reminderTimeFormatted = obj.optString("reminderTimeFormatted", "9:00 AM"),
                        reminderHour = obj.optInt("reminderHour", 9),
                        reminderMinute = obj.optInt("reminderMinute", 0),
                        scheduledTimeMillis = obj.getLong("scheduledTimeMillis"),
                        missedReminderMillis = obj.getLong("missedReminderMillis"),
                        familyEscalationMillis = obj.getLong("familyEscalationMillis"),
                        reminderResponsibleUid = obj.optString("reminderResponsibleUid").ifBlank { null },
                        isChildRecipient = obj.optBoolean("isChildRecipient", false),
                        isTaken = obj.optBoolean("isTaken", false),
                        takenAtFormatted = obj.optString("takenAtFormatted").ifBlank { null },
                        takenAtMillis = obj.optLong("takenAtMillis").takeIf { it > 0L },
                        lastNotifiedStage = stage
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    /**
     * Finds a specific occurrence by medication ID and date key.
     */
    fun getOccurrence(context: Context, medicationId: String, dateKey: String): MedicationOccurrence? {
        return loadOccurrences(context).find { it.medicationId == medicationId && it.dateKey == dateKey }
    }

    /**
     * Marks an occurrence as taken in local storage and records timestamp.
     */
    fun markOccurrenceAsTaken(context: Context, medicationId: String, dateKey: String, takenFormatted: String) {
        val occurrences = loadOccurrences(context).toMutableList()
        val index = occurrences.indexOfFirst { it.medicationId == medicationId && it.dateKey == dateKey }
        if (index != -1) {
            val current = occurrences[index]
            occurrences[index] = current.copy(
                isTaken = true,
                takenAtFormatted = takenFormatted,
                takenAtMillis = System.currentTimeMillis()
            )
            saveOccurrences(context, occurrences)
        }
    }

    /**
     * Checks if a notification stage has already been sent for an occurrence (idempotency check).
     */
    fun hasStageBeenNotified(context: Context, occurrenceId: String, stage: ReminderStage): Boolean {
        val key = "${occurrenceId}_${stage.name}"
        return getPrefs(context).getBoolean("${KEY_NOTIFIED_STAGES}_$key", false)
    }

    /**
     * Records that a notification stage has been sent for an occurrence.
     */
    fun markStageAsNotified(context: Context, occurrenceId: String, stage: ReminderStage) {
        val key = "${occurrenceId}_${stage.name}"
        getPrefs(context).edit().putBoolean("${KEY_NOTIFIED_STAGES}_$key", true).apply()
    }
}
