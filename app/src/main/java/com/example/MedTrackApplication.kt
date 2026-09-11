package com.example

import android.app.Application
import android.content.Context
import com.example.reminder.notification.MedicationNotificationHelper

/**
 * Custom Application class for MedTrack.
 * Initializes the notification channel and provides safe application context for background tasks.
 */
class MedTrackApplication : Application() {

    companion object {
        lateinit var appContext: Context
            private set
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        MedicationNotificationHelper.createNotificationChannel(this)
    }
}
