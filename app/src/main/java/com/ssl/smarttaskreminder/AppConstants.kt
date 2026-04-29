package com.ssl.smarttaskreminder

/**
 * SSL-SE-UI-001 App Constants
 * Central place for all string keys, Firestore collection names, and role constants.
 * NEVER hardcode these values elsewhere — always reference from here.
 */
object AppConstants {

    // =========================================================
    // SUPER ADMIN — Hardcoded credential (one per platform)
    // =========================================================
    const val SUPER_ADMIN_EMAIL = "prottoy.saha@soniagroup.com"

    // =========================================================
    // FIRESTORE COLLECTION NAMES
    // =========================================================
    const val COLLECTION_COMPANIES = "companies"
    const val COLLECTION_ADMINS    = "admins"
    const val COLLECTION_MANAGERS  = "managers"
    const val COLLECTION_USERS     = "users"
    const val COLLECTION_TASKS     = "tasks"
    const val COLLECTION_COUNTERS  = "counters"   // For auto-increment IDs

    // Counter document IDs
    const val COUNTER_GLOBAL       = "global"     // Contains nextCid

    // =========================================================
    // DOCUMENT FIELD NAMES
    // =========================================================
    const val FIELD_COMPANY_ID     = "companyId"
    const val FIELD_ROLE           = "role"
    const val FIELD_EMAIL          = "email"
    const val FIELD_STATUS         = "status"
    const val FIELD_DEADLINE       = "deadline"
    const val FIELD_MANAGER_ID     = "managerId"
    const val FIELD_CREATED_BY     = "createdBy"
    const val FIELD_TASK_STATUS    = "status"
    const val FIELD_COMPLETED_AT   = "completedAt"

    // =========================================================
    // ROLE VALUES
    // =========================================================
    const val ROLE_SUPER_ADMIN = "super_admin"
    const val ROLE_ADMIN       = "admin"
    const val ROLE_MANAGER     = "manager"
    const val ROLE_USER        = "user"

    // =========================================================
    // TASK STATUS VALUES
    // =========================================================
    const val STATUS_PENDING   = "pending"
    const val STATUS_OVERDUE   = "overdue"
    const val STATUS_COMPLETED = "completed"

    // =========================================================
    // IMPORTANCE VALUES
    // =========================================================
    const val IMPORTANCE_HIGH   = "high"
    const val IMPORTANCE_MEDIUM = "medium"
    const val IMPORTANCE_LOW    = "low"

    // =========================================================
    // COMPLETION TYPE VALUES
    // =========================================================
    const val COMPLETION_ON_TIME = "on-time"
    const val COMPLETION_LATE    = "late"

    // =========================================================
    // COMPANY STATUS VALUES
    // =========================================================
    const val COMPANY_ACTIVE   = "active"
    const val COMPANY_INACTIVE = "inactive"

    // =========================================================
    // INTENT EXTRA KEYS (for Activity navigation)
    // =========================================================
    const val EXTRA_COMPANY_ID     = "EXTRA_COMPANY_ID"
    const val EXTRA_COMPANY_NAME   = "EXTRA_COMPANY_NAME"
    const val EXTRA_TASK_ID        = "EXTRA_TASK_ID"        // Firestore doc ID
    const val EXTRA_MANAGER_ID     = "EXTRA_MANAGER_ID"     // Firestore doc ID
    const val EXTRA_USER_ID        = "EXTRA_USER_ID"        // Firestore doc ID
    const val EXTRA_ADMIN_DOC_ID   = "EXTRA_ADMIN_DOC_ID"
    const val EXTRA_IS_EDIT_MODE   = "EXTRA_IS_EDIT_MODE"
    const val EXTRA_TASK_STATUS    = "EXTRA_TASK_STATUS"    // filter for TaskListActivity

    // =========================================================
    // NOTIFICATION
    // =========================================================
    const val NOTIF_CHANNEL_TASKS = "channel_task_reminders"
    const val NOTIF_CHANNEL_ADMIN = "channel_admin_alerts"
    const val WORK_TAG_TASK_REMINDER   = "work_task_reminder_"
    const val WORK_TAG_ADMIN_OVERDUE   = "work_admin_overdue"

    // =========================================================
    // TIME CONSTANTS
    // =========================================================
    const val REMINDER_HOUR   = 10   // 10:00 AM — user task reminders
    const val ADMIN_NOTIF_HOUR = 11  // 11:00 AM — admin overdue summary
}
