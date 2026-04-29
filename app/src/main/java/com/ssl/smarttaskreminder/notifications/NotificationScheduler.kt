package com.ssl.smarttaskreminder.notifications

import android.content.Context
import androidx.work.*
import com.ssl.smarttaskreminder.AppConstants
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * NotificationScheduler — schedules and cancels WorkManager periodic workers.
 *
 * Per SRS §6:
 * - Task reminders fire daily at 10:00 AM (user reminders)
 * - Admin overdue alerts fire daily at 11:00 AM
 */
object NotificationScheduler {

    /**
     * Schedules a daily reminder for a specific task.
     * Tagged with the task's document ID so it can be cancelled when task is completed.
     */
    fun scheduleTaskReminder(
        context: Context,
        taskDocId: String,
        companyId: String
    ) {
        val tag = AppConstants.WORK_TAG_TASK_REMINDER + taskDocId

        val inputData = workDataOf(
            "taskDocId"  to taskDocId,
            "companyId"  to companyId
        )

        val request = PeriodicWorkRequestBuilder<TaskReminderWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.DAYS
        )
            .setInitialDelay(calculateInitialDelay(AppConstants.REMINDER_HOUR), TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag(tag)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            tag,
            ExistingPeriodicWorkPolicy.REPLACE,
            request
        )
    }

    /**
     * Cancels the daily reminder for a specific task.
     * Called when task status = completed (SRS §9.3).
     */
    fun cancelTaskReminder(context: Context, taskDocId: String) {
        val tag = AppConstants.WORK_TAG_TASK_REMINDER + taskDocId
        WorkManager.getInstance(context).cancelUniqueWork(tag)
    }

    /**
     * Schedules the admin overdue summary notification at 11:00 AM daily.
     */
    fun scheduleAdminOverdueAlert(
        context: Context,
        adminUid: String,
        companyId: String
    ) {
        val inputData = workDataOf(
            "adminUid"  to adminUid,
            "companyId" to companyId
        )

        val request = PeriodicWorkRequestBuilder<AdminOverdueWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.DAYS
        )
            .setInitialDelay(calculateInitialDelay(AppConstants.ADMIN_NOTIF_HOUR), TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag(AppConstants.WORK_TAG_ADMIN_OVERDUE)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            AppConstants.WORK_TAG_ADMIN_OVERDUE,
            ExistingPeriodicWorkPolicy.REPLACE,
            request
        )
    }

    /**
     * Calculates milliseconds until the next occurrence of the target hour today (or tomorrow).
     */
    private fun calculateInitialDelay(targetHour: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, targetHour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        // If target time already passed today, schedule for tomorrow
        if (now.after(target)) {
            target.add(Calendar.DAY_OF_MONTH, 1)
        }
        return target.timeInMillis - now.timeInMillis
    }
}
