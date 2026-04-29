package com.ssl.smarttaskreminder.data.model

import com.google.firebase.Timestamp

/**
 * Firestore Collection: managers
 * Document ID: Firebase Auth UID of the manager
 * companyId is MANDATORY — every query must filter by it.
 */
data class Manager(
    val documentId: String = "",   // = Firebase Auth UID
    val mid: Int = 0,              // Sequential within company
    val companyId: String = "",    // 🔑 Tenant isolation key
    val name: String = "",
    val email: String = "",
    val department: String = "General", // Default if not specified
    val role: String = "manager",
    val createdBy: String = "",    // Firebase UID of the Admin who created this manager
    val createdAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "mid"        to mid,
        "companyId"  to companyId,
        "name"       to name,
        "email"      to email,
        "department" to department,
        "role"       to role,
        "createdBy"  to createdBy,
        "createdAt"  to createdAt
    )
}
