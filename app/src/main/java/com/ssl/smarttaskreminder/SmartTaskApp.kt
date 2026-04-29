package com.ssl.smarttaskreminder

import android.app.Application
import com.google.firebase.FirebaseApp
import com.ssl.smarttaskreminder.notifications.NotificationHelper

/**
 * Application class — initialises Firebase and notification channels on startup.
 */
class SmartTaskApp : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        NotificationHelper.createNotificationChannels(this)
    }
}
