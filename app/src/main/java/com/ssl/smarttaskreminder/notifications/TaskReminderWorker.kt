package com.ssl.smarttaskreminder.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import com.ssl.smarttaskreminder.AppConstants
import com.ssl.smarttaskreminder.data.model.Task
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

/**
 * TaskReminderWorker
 *
 * Runs daily at 10:00 AM (per SRS §6.1).
 * Checks if the task is still pending/overdue.
 * If so, shows a reminder notification.
 * If task is completed, cancels itself.
 */
class TaskReminderWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val db = FirebaseFirestore.getInstance()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override suspend fun doWork(): Result {
        val taskDocId = inputData.getString("taskDocId") ?: return Result.failure()
        val companyId = inputData.getString("companyId") ?: return Result.failure()

        return try {
            // Fetch the task from Firestore
            val doc = db.collection(AppConstants.COLLECTION_TASKS)
                .document(taskDocId).get().await()

            if (!doc.exists()) return Result.success()

            val task = doc.toObject(Task::class.java)?.copy(documentId = doc.id)
                ?: return Result.success()

            // SRS §9.3 — Stop if task is completed
            if (task.status == AppConstants.STATUS_COMPLETED) {
                NotificationHelper.cancelTaskNotification(context, taskDocId)
                return Result.success()
            }

            // Verify companyId matches (security check)
            if (task.companyId != companyId) return Result.failure()

            // Build notification
            val deadlineDate = task.deadline?.toDate()
            val deadlineFormatted = if (deadlineDate != null) dateFormat.format(deadlineDate) else "N/A"
            val daysLeft = task.daysLeft()

            NotificationHelper.showTaskReminder(
                context          = context,
                taskDocId        = taskDocId,
                taskName         = task.taskName,
                styleNo          = task.styleNo,
                deadlineFormatted = deadlineFormatted,
                daysLeft         = daysLeft
            )

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
