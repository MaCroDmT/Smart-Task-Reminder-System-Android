package com.ssl.smarttaskreminder.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ssl.smarttaskreminder.AppConstants
import com.ssl.smarttaskreminder.data.model.Task
import kotlinx.coroutines.tasks.await
import java.util.Date

/**
 * TaskRepository — CRUD and status management for the tasks collection.
 *
 * ⚠ CRITICAL: Every single query in this repository MUST include
 *   .whereEqualTo("companyId", companyId)
 * This is the software enforcement of multi-tenant isolation per SRS §4.3 & §9.4.
 * NO EXCEPTIONS.
 */
class TaskRepository {

    private val db    = FirebaseFirestore.getInstance()
    private val idGen = IdGeneratorRepository()

    /**
     * Creates a new task with auto-generated Tid.
     * [companyId] is MANDATORY — always passed from SessionManager.
     */
    suspend fun createTask(
        companyId: String,
        taskName: String,
        styleNo: String,
        details: String,
        importance: String,
        deadline: Date,
        createdBy: String,
        managerId: Int
    ): Task {
        val tid = idGen.nextTid(companyId)

        val task = Task(
            tid            = tid,
            companyId      = companyId,
            taskName       = taskName.trim(),
            styleNo        = styleNo.trim(),
            details        = details.trim(),
            importance     = importance,
            deadline       = Timestamp(deadline),
            createdBy      = createdBy,
            managerId      = managerId,
            status         = AppConstants.STATUS_PENDING,
            createdDate    = Timestamp.now(),
            completedAt    = null,
            completionType = null
        )

        // Auto-generate Firestore document ID
        val docRef = db.collection(AppConstants.COLLECTION_TASKS).document()
        val taskWithId = task.copy(documentId = docRef.id)
        docRef.set(taskWithId.toMap()).await()

        return taskWithId
    }

    /**
     * Returns all tasks for a company (all statuses).
     * ⚠ Always filters by companyId.
     */
    suspend fun getAllTasksByCompany(companyId: String): List<Task> {
        val snapshot = db.collection(AppConstants.COLLECTION_TASKS)
            .whereEqualTo(AppConstants.FIELD_COMPANY_ID, companyId)
            .get().await()
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(Task::class.java)?.copy(documentId = doc.id)
        }.sortedByDescending { it.createdDate }.also { updateOverdueStatus(it) }
    }

    /**
     * Returns tasks for a specific user within a company.
     * ⚠ Always filters by companyId.
     */
    suspend fun getTasksByUser(companyId: String, createdBy: String): List<Task> {
        val snapshot = db.collection(AppConstants.COLLECTION_TASKS)
            .whereEqualTo(AppConstants.FIELD_COMPANY_ID, companyId)
            .whereEqualTo(AppConstants.FIELD_CREATED_BY, createdBy)
            .get().await()
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(Task::class.java)?.copy(documentId = doc.id)
        }.sortedByDescending { it.createdDate }.also { updateOverdueStatus(it) }
    }

    /**
     * Returns tasks for all users under a specific manager within a company.
     * ⚠ Always filters by companyId.
     */
    suspend fun getTasksByManager(companyId: String, managerId: Int): List<Task> {
        val snapshot = db.collection(AppConstants.COLLECTION_TASKS)
            .whereEqualTo(AppConstants.FIELD_COMPANY_ID, companyId)
            .whereEqualTo(AppConstants.FIELD_MANAGER_ID, managerId)
            .get().await()
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(Task::class.java)?.copy(documentId = doc.id)
        }.sortedByDescending { it.createdDate }.also { updateOverdueStatus(it) }
    }

    /**
     * Returns tasks filtered by status for a company.
     * ⚠ Always filters by companyId.
     */
    suspend fun getTasksByStatus(companyId: String, status: String): List<Task> {
        return getAllTasksByCompany(companyId).filter { it.status == status }
    }

    /** Returns a single task by Firestore document ID. */
    suspend fun getTask(documentId: String): Task? {
        val doc = db.collection(AppConstants.COLLECTION_TASKS)
            .document(documentId).get().await()
        return doc.toObject(Task::class.java)?.copy(documentId = doc.id)
    }

    /**
     * Marks a task as COMPLETED.
     * Business logic: if completedAt <= deadline → on-time, else → late.
     */
    suspend fun completeTask(documentId: String, deadline: Timestamp?) {
        val now = Timestamp.now()
        val completionType = if (deadline != null && now.toDate() <= deadline.toDate()) {
            AppConstants.COMPLETION_ON_TIME
        } else {
            AppConstants.COMPLETION_LATE
        }

        db.collection(AppConstants.COLLECTION_TASKS)
            .document(documentId)
            .update(mapOf(
                "status"         to AppConstants.STATUS_COMPLETED,
                "completedAt"    to now,
                "completionType" to completionType
            ))
            .await()
    }

    /** Deletes a task and completely recycles its tid so it can be re-used. */
    suspend fun deleteTask(documentId: String) {
        val doc = db.collection(AppConstants.COLLECTION_TASKS).document(documentId).get().await()
        val task = doc.toObject(Task::class.java)
        if (task != null) {
            idGen.recycleTid(task.companyId, task.tid)
        }
        db.collection(AppConstants.COLLECTION_TASKS).document(documentId).delete().await()
    }

    /**
     * Business Rule §9.1 — Updates overdue status for tasks whose deadline has passed.
     * Called every time we fetch tasks — keeps status in sync without Cloud Functions.
     */
    private suspend fun updateOverdueStatus(tasks: List<Task>) {
        val now = Date()
        val batch = db.batch()
        var hasUpdates = false

        tasks.forEach { task ->
            if (task.status == AppConstants.STATUS_PENDING &&
                task.deadline != null &&
                now > task.deadline.toDate()
            ) {
                val ref = db.collection(AppConstants.COLLECTION_TASKS).document(task.documentId)
                batch.update(ref, "status", AppConstants.STATUS_OVERDUE)
                hasUpdates = true
            }
        }

        if (hasUpdates) batch.commit().await()
    }

    // =========================================================
    // ANALYTICS QUERIES
    // =========================================================

    /** Returns overdue task count per manager (for Admin analytics). */
    suspend fun getOverdueCountByManager(companyId: String): Map<Int, Int> {
        val tasks = getAllTasksByCompany(companyId)
            .filter { it.status == AppConstants.STATUS_OVERDUE }
        return tasks.groupBy { it.managerId }.mapValues { it.value.size }
    }

    /** Returns on-time completion count per user (for best performers). */
    suspend fun getOnTimeCountByUser(companyId: String): Map<String, Int> {
        val tasks = getAllTasksByCompany(companyId)
            .filter { it.status == AppConstants.STATUS_COMPLETED && it.completionType == AppConstants.COMPLETION_ON_TIME }
        return tasks.groupBy { it.createdBy }.mapValues { it.value.size }
    }

    /** Returns late completion + overdue count per user (for worst performers). */
    suspend fun getLateCountByUser(companyId: String): Map<String, Int> {
        val tasks = getAllTasksByCompany(companyId)
            .filter { it.status == AppConstants.STATUS_OVERDUE ||
                      (it.status == AppConstants.STATUS_COMPLETED && it.completionType == AppConstants.COMPLETION_LATE) }
        return tasks.groupBy { it.createdBy }.mapValues { it.value.size }
    }

    /** Returns counts by importance and status for the company. */
    suspend fun getTaskCountsByImportance(companyId: String): Map<String, Map<String, Int>> {
        val tasks = getAllTasksByCompany(companyId)
        val importanceLevels = listOf(AppConstants.IMPORTANCE_HIGH, AppConstants.IMPORTANCE_MEDIUM, AppConstants.IMPORTANCE_LOW)
        val statuses = listOf(AppConstants.STATUS_PENDING, AppConstants.STATUS_OVERDUE, AppConstants.STATUS_COMPLETED)

        return importanceLevels.associateWith { imp ->
            statuses.associateWith { st ->
                tasks.count { it.importance == imp && it.status == st }
            }
        }
    }

    /** Returns overall completion percentage for the company. */
    suspend fun getCompletionPercentage(companyId: String): Float {
        val tasks = getAllTasksByCompany(companyId)
        if (tasks.isEmpty()) return 0f
        val completed = tasks.count { it.status == AppConstants.STATUS_COMPLETED }
        return (completed.toFloat() / tasks.size.toFloat()) * 100f
    }
}
