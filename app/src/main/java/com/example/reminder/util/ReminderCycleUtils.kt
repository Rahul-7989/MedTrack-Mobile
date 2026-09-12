package com.example.reminder.util

import com.example.ui.hub.dashboard.model.ReminderCycle
import java.util.Calendar
import java.util.TimeZone

/**
 * Utility for calculating medication reminder cycle boundaries anchored at 12:00 AM (midnight).
 */
object ReminderCycleUtils {

    fun getCycleStartAndEnd(createdAt: Long, cycle: ReminderCycle, customIntervalDays: Int, now: Long): Pair<Long, Long> {
        val cal = Calendar.getInstance(TimeZone.getDefault()).apply {
            timeInMillis = createdAt
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val baseMidnightMs = cal.timeInMillis

        val cycleDurationMs = when (cycle) {
            ReminderCycle.EVERY_24_HOURS -> 24L * 60L * 60L * 1000L
            ReminderCycle.EVERY_48_HOURS -> 48L * 60L * 60L * 1000L
            ReminderCycle.CUSTOM -> (customIntervalDays.toLong().coerceAtLeast(1L)) * 24L * 60L * 60L * 1000L
        }

        val elapsedMs = now - baseMidnightMs
        val cycleIndex = if (elapsedMs >= 0) elapsedMs / cycleDurationMs else 0L
        val cycleStartMs = baseMidnightMs + (cycleIndex * cycleDurationMs)
        val cycleEndMs = cycleStartMs + cycleDurationMs

        return Pair(cycleStartMs, cycleEndMs)
    }
}
