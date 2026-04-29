package com.ssl.smarttaskreminder.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ssl.smarttaskreminder.AppConstants
import com.ssl.smarttaskreminder.data.model.Admin
import kotlinx.coroutines.tasks.await

/**
 * AdminRepository — CRUD for the admins collection.
 * Creating an admin also creates a Firebase Auth account for them.
 *
 * ⚠ All queries MUST filter by companyId.
 */
class AdminRepository {

    private val db    = FirebaseFirestore.getInstance()
    private val auth  = FirebaseAuth.getInstance()
    private val idGen = IdGeneratorRepository()

    /**
     * Creates an Admin: creates Firebase Auth account + Firestore document.
     * Returns the created Admin object.
     *
     * ⚠ This method temporarily signs in as the new user and then re-signs in as Super Admin.
     * In production, use Cloud Functions for proper admin account creation.
     */
    suspend fun createAdmin(
        companyId: String,
        name: String,
        email: String,
        tempPassword: String
    ): Admin {
        // Use REST API to create the user without logging out the current Super Admin
        val apiKey = FirebaseApp.getInstance().options.apiKey
        val uid = FirebaseAuthHelper.createUserSilently(email, tempPassword, apiKey)

        // Generate sequential Aid
        val aid = idGen.nextAid(companyId)

        val admin = Admin(
            documentId = uid,
            aid        = aid,
            companyId  = companyId,
            name       = name.trim(),
            email      = email.trim(),
            role       = AppConstants.ROLE_ADMIN,
            createdAt  = Timestamp.now()
        )

        db.collection(AppConstants.COLLECTION_ADMINS)
            .document(uid)
            .set(admin.toMap())
            .await()

        return admin
    }

    /**
     * Returns all admins for a given company.
     * ⚠ Always filters by companyId.
     */
    suspend fun getAdminsByCompany(companyId: String): List<Admin> {
        val snapshot = db.collection(AppConstants.COLLECTION_ADMINS)
            .whereEqualTo(AppConstants.FIELD_COMPANY_ID, companyId)
            .get().await()
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(Admin::class.java)?.copy(documentId = doc.id)
        }
    }

    /** Returns the admin count for a company. */
    suspend fun getAdminCount(companyId: String): Int {
        return db.collection(AppConstants.COLLECTION_ADMINS)
            .whereEqualTo(AppConstants.FIELD_COMPANY_ID, companyId)
            .get().await().size()
    }

    /** Updates an admin profile. */
    suspend fun updateAdmin(adminUid: String, name: String, email: String) {
        val updates = mutableMapOf<String, Any>()
        if (name.isNotBlank()) updates["name"] = name.trim()
        if (email.isNotBlank()) updates["email"] = email.trim()
        
        if (updates.isNotEmpty()) {
            db.collection(AppConstants.COLLECTION_ADMINS)
                .document(adminUid)
                .update(updates)
                .await()
        }
    }

    /** Deletes an admin and completely recycles their aid so it can be re-used. */
    suspend fun deleteAdmin(adminUid: String) {
        val doc = db.collection(AppConstants.COLLECTION_ADMINS).document(adminUid).get().await()
        val admin = doc.toObject(Admin::class.java)
        if (admin != null) {
            idGen.recycleAid(admin.companyId, admin.aid)
        }
        db.collection(AppConstants.COLLECTION_ADMINS).document(adminUid).delete().await()
    }
}
