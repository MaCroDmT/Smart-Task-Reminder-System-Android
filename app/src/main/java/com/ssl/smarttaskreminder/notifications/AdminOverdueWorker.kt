package com.ssl.smarttaskreminder.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import com.ssl.smarttaskreminder.AppConstants
import com.ssl.smarttaskreminder.data.model.Task
import kotlinx.coroutines.tasks.await

/**
 * AdminOverdueWorker
 *
 * Runs daily at 11:00 AM (per SRS §6.2).
 * Queries all overdue tasks in the admin's company and sends a summary notification.
 * Notification payload includes companyId for routing (SRS §6.2 NEW requirement).
 */
class AdminOverdueWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val db = FirebaseFirestore.getInstance()

    override suspend fun doWork(): Result {
        val companyId = inputData.getString("companyId") ?: return Result.failure()

        return try {
            val now = com.google.firebase.Timestamp.now()
            
            // 1. Fetch all pending or overdue tasks for this company
            val snapshot = db.collection(AppConstants.COLLECTION_TASKS)
                .whereEqualTo(AppConstants.FIELD_COMPANY_ID, companyId)
                .whereIn(AppConstants.FIELD_TASK_STATUS, listOf(AppConstants.STATUS_PENDING, AppConstants.STATUS_OVERDUE))
                .get().await()

            val allActiveTasks = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Task::class.java)?.copy(documentId = doc.id)
            }

            // 2. Identify which ones ARE overdue (either already marked or past deadline)
            val overdueTasks = allActiveTasks.filter { task ->
                task.status == AppConstants.STATUS_OVERDUE || 
                (task.status == AppConstants.STATUS_PENDING && task.deadline != null && now.toDate().after(task.deadline.toDate()))
            }

            if (overdueTasks.isEmpty()) return Result.success()

            // 3. Update those that were pending to overdue in Firestore (optional but good for consistency)
            val batch = db.batch()
            var needsUpdate = false
            overdueTasks.forEach { task ->
                if (task.status == AppConstants.STATUS_PENDING) {
                    batch.update(db.collection(AppConstants.COLLECTION_TASKS).document(task.documentId), "status", AppConstants.STATUS_OVERDUE)
                    needsUpdate = true
                }
            }
            if (needsUpdate) batch.commit().await()

            // 4. Build summary text
            val summary = overdueTasks.take(5).joinToString("\n") { task ->
                "• ${task.taskName} (${task.styleNo})"
            }
            val moreText = if (overdueTasks.size > 5) "\n…and ${overdueTasks.size - 5} more" else ""

            // 5. Get company name
            val companyDoc = db.collection(AppConstants.COLLECTION_COMPANIES)
                .document(companyId).get().await()
            val companyName = companyDoc.getString("name") ?: "Your Company"

            NotificationHelper.showAdminOverdueAlert(
                context      = context,
                companyName  = companyName,
                overdueCount = overdueTasks.size,
                taskSummary  = summary + moreText
            )

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
