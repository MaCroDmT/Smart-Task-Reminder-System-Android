package com.ssl.smarttaskreminder.data.model

import com.google.firebase.Timestamp

/**
 * Firestore Collection: users
 * Document ID: Firebase Auth UID of the user
 * companyId is MANDATORY — every query must filter by it.
 */
data class User(
    val documentId: String = "",   // = Firebase Auth UID
    val uid: Int = 0,              // Sequential within company
    val companyId: String = "",    // 🔑 Tenant isolation key
    val name: String = "",
    val email: String = "",
    val role: String = "user",
    val managerId: Int = 0,        // mid of the assigned Manager
    val createdAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "uid"       to uid,
        "companyId" to companyId,
        "name"      to name,
        "email"     to email,
        "role"      to role,
        "managerId" to managerId,
        "createdAt" to createdAt
    )
}
