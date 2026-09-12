package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

object CurrentTimeService {
    private var serverTimeOffsetMs: Long = 0L

    fun setServerTimeOffset(offset: Long) {
        serverTimeOffsetMs = offset
    }

    fun getCurrentInstant(): Long {
        return System.currentTimeMillis() + serverTimeOffsetMs
    }

    fun getCurrentCalendar(): Calendar {
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        calendar.timeInMillis = getCurrentInstant()
        return calendar
    }

    fun getCurrentLocalDate(): Calendar {
        val calendar = getCurrentCalendar()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar
    }

    fun getCurrentLocalTime(): String {
        val calendar = getCurrentCalendar()
        val formatter = SimpleDateFormat("h:mm a", Locale.getDefault())
        return formatter.format(calendar.time)
    }

    private val MONTH_CODES = arrayOf(
        "JAN", "FEB", "MAR", "APR", "MAY", "JUN",
        "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"
    )

    fun getCurrentFormattedDate(): String {
        val calendar = getCurrentCalendar()
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val monthCode = MONTH_CODES.getOrElse(calendar.get(Calendar.MONTH)) { "SEP" }
        val year = calendar.get(Calendar.YEAR)
        return "$day $monthCode, $year"
    }

    fun formatTime(timeInMillis: Long): String {
        val calendar = Calendar.getInstance(TimeZone.getDefault()).apply {
            this.timeInMillis = timeInMillis
        }
        val formatter = SimpleDateFormat("h:mm a", Locale.getDefault())
        return formatter.format(calendar.time)
    }
}
