package com.ssl.smarttaskreminder.data.model

import com.google.firebase.Timestamp

/**
 * Firestore Collection: admins
 * Document ID: Firebase Auth UID of the admin
 * companyId is MANDATORY — every query must filter by it.
 */
data class Admin(
    val documentId: String = "",   // = Firebase Auth UID
    val aid: Int = 0,              // Sequential within company
    val companyId: String = "",    // 🔑 Tenant isolation key
    val name: String = "",
    val email: String = "",
    val role: String = "admin",
    val createdAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "aid"       to aid,
        "companyId" to companyId,
        "name"      to name,
        "email"     to email,
        "role"      to role,
        "createdAt" to createdAt
    )
}
