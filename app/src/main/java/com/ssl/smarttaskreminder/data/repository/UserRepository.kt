package com.ssl.smarttaskreminder.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ssl.smarttaskreminder.AppConstants
import com.ssl.smarttaskreminder.data.model.User
import kotlinx.coroutines.tasks.await

/**
 * UserRepository — CRUD for the users collection.
 * ⚠ ALL queries filter by companyId — mandatory per SRS §4.3.
 */
class UserRepository {

    private val db    = FirebaseFirestore.getInstance()
    private val auth  = FirebaseAuth.getInstance()
    private val idGen = IdGeneratorRepository()

    /** Creates a User with Firebase Auth account + Firestore document. */
    suspend fun createUser(
        companyId: String,
        name: String,
        email: String,
        tempPassword: String,
        managerId: Int
    ): User {
        // Use REST API to create the user without logging out the current Admin/Manager
        val apiKey = FirebaseApp.getInstance().options.apiKey
        val uid = FirebaseAuthHelper.createUserSilently(email, tempPassword, apiKey)

        val uid_seq = idGen.nextUid(companyId)

        val user = User(
            documentId = uid,
            uid        = uid_seq,
            companyId  = companyId,
            name       = name.trim(),
            email      = email.trim(),
            role       = AppConstants.ROLE_USER,
            managerId  = managerId,
            createdAt  = Timestamp.now()
        )

        db.collection(AppConstants.COLLECTION_USERS)
            .document(uid)
            .set(user.toMap())
            .await()

        return user
    }

    /**
     * Returns all users for a company.
     * ⚠ Always filters by companyId.
     */
    suspend fun getUsersByCompany(companyId: String): List<User> {
        val snapshot = db.collection(AppConstants.COLLECTION_USERS)
            .whereEqualTo(AppConstants.FIELD_COMPANY_ID, companyId)
            .get().await()
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(User::class.java)?.copy(documentId = doc.id)
        }
    }

    /**
     * Returns all users assigned to a specific manager in a company.
     * ⚠ Always filters by companyId.
     */
    suspend fun getUsersByManager(companyId: String, managerId: Int): List<User> {
        val snapshot = db.collection(AppConstants.COLLECTION_USERS)
            .whereEqualTo(AppConstants.FIELD_COMPANY_ID, companyId)
            .whereEqualTo(AppConstants.FIELD_MANAGER_ID, managerId)
            .get().await()
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(User::class.java)?.copy(documentId = doc.id)
        }
    }

    /** Returns a single user by Firebase UID. */
    suspend fun getUserByUid(uid: String): User? {
        val doc = db.collection(AppConstants.COLLECTION_USERS)
            .document(uid).get().await()
        return doc.toObject(User::class.java)?.copy(documentId = doc.id)
    }

    /** Returns user count for a company. */
    suspend fun getUserCount(companyId: String): Int {
        return db.collection(AppConstants.COLLECTION_USERS)
            .whereEqualTo(AppConstants.FIELD_COMPANY_ID, companyId)
            .get().await().size()
    }

    /** Updates user name, email, managerId. */
    suspend fun updateUser(userUid: String, name: String, email: String, managerId: Int) {
        val updates = mutableMapOf<String, Any>(
            "managerId" to managerId
        )
        if (name.isNotBlank()) updates["name"] = name.trim()
        if (email.isNotBlank()) updates["email"] = email.trim()
        
        db.collection(AppConstants.COLLECTION_USERS)
            .document(userUid)
            .update(updates)
            .await()
    }

    /** Deletes user Firestore document and completely recycles their uid so it can be re-used. */
    suspend fun deleteUser(userUid: String) {
        val doc = db.collection(AppConstants.COLLECTION_USERS).document(userUid).get().await()
        val user = doc.toObject(User::class.java)
        if (user != null) {
            idGen.recycleUid(user.companyId, user.uid)
        }
        db.collection(AppConstants.COLLECTION_USERS).document(userUid).delete().await()
    }
}
