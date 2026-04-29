package com.ssl.smarttaskreminder.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ssl.smarttaskreminder.AppConstants
import com.ssl.smarttaskreminder.R

/**
 * NotificationHelper — creates notification channels and builds/shows notifications.
 * Called from WorkManager workers and FCMService.
 */
object NotificationHelper {

    /**
     * Creates the required notification channels.
     * Must be called once at app startup (SmartTaskApp.onCreate).
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Task Reminders Channel
            val taskChannel = NotificationChannel(
                AppConstants.NOTIF_CHANNEL_TASKS,
                context.getString(R.string.notif_channel_tasks),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notif_channel_desc_tasks)
                enableVibration(true)
            }

            // Admin Overdue Alerts Channel
            val adminChannel = NotificationChannel(
                AppConstants.NOTIF_CHANNEL_ADMIN,
                context.getString(R.string.notif_channel_admin),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notif_channel_desc_admin)
                enableVibration(true)
            }

            manager.createNotificationChannel(taskChannel)
            manager.createNotificationChannel(adminChannel)
        }
    }

    /**
     * Shows a task reminder notification.
     * Format: "Reminder - [TaskName] - StyleNo: [XXX] - Deadline: [DD/MM/YYYY] - Left [X] Days - Please see into it!"
     */
    fun showTaskReminder(
        context: Context,
        taskDocId: String,
        taskName: String,
        styleNo: String,
        deadlineFormatted: String,
        daysLeft: Long
    ) {
        val id = taskDocId.hashCode()
        val daysText = when {
            daysLeft > 0  -> "Left $daysLeft Days"
            daysLeft == 0L -> "Due Today!"
            else           -> "Overdue by ${-daysLeft} Days"
        }

        val title = "⏰ Task Reminder"
        val body  = "Reminder - $taskName - StyleNo: $styleNo - Deadline: $deadlineFormatted - $daysText - Please see into it!"

        val notification = NotificationCompat.Builder(context, AppConstants.NOTIF_CHANNEL_TASKS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(id, notification)
        } catch (e: SecurityException) {
            // POST_NOTIFICATIONS permission not granted — silently ignore
        }
    }

    /**
     * Shows an admin overdue summary notification.
     */
    fun showAdminOverdueAlert(
        context: Context,
        companyName: String,
        overdueCount: Int,
        taskSummary: String
    ) {
        val title = "⚠ $companyName — Overdue Tasks"
        val body  = "You have $overdueCount overdue task(s) today.\n$taskSummary"

        val notification = NotificationCompat.Builder(context, AppConstants.NOTIF_CHANNEL_ADMIN)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify("admin_overdue".hashCode(), notification)
        } catch (e: SecurityException) {}
    }

    /** Cancels a specific task's notification. Called when a task is completed. */
    fun cancelTaskNotification(context: Context, taskDocId: String) {
        NotificationManagerCompat.from(context).cancel(taskDocId.hashCode())
    }

    /**
     * Shows an immediate notification when a task is assigned.
     */
    fun showTaskCreatedNotification(context: Context, taskName: String, styleNo: String) {
        val title = "🆕 New Task Assigned"
        val body  = "You have a new task: $taskName (Style: $styleNo)"

        val notification = NotificationCompat.Builder(context, AppConstants.NOTIF_CHANNEL_TASKS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), notification)
        } catch (e: SecurityException) {}
    }
}
