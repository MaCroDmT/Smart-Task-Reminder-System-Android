package com.ssl.smarttaskreminder.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ssl.smarttaskreminder.AppConstants
import com.ssl.smarttaskreminder.data.model.Manager
import kotlinx.coroutines.tasks.await

/**
 * ManagerRepository — CRUD for the managers collection.
 * ⚠ ALL queries filter by companyId — mandatory per SRS §4.3.
 */
class ManagerRepository {

    private val db    = FirebaseFirestore.getInstance()
    private val auth  = FirebaseAuth.getInstance()
    private val idGen = IdGeneratorRepository()

    /**
     * Creates a Manager: Firebase Auth account + Firestore document.
     * [createdByUid] = Firebase UID of the Admin creating this manager.
     */
    suspend fun createManager(
        companyId: String,
        name: String,
        email: String,
        department: String,
        tempPassword: String,
        createdByUid: String
    ): Manager {
        // Use REST API to create the user without logging out the current Company Admin
        val apiKey = FirebaseApp.getInstance().options.apiKey
        val uid = FirebaseAuthHelper.createUserSilently(email, tempPassword, apiKey)

        val mid = idGen.nextMid(companyId)

        val manager = Manager(
            documentId = uid,
            mid        = mid,
            companyId  = companyId,
            name       = name.trim(),
            email      = email.trim(),
            department = department.trim(),
            role       = AppConstants.ROLE_MANAGER,
            createdBy  = createdByUid,
            createdAt  = Timestamp.now()
        )

        db.collection(AppConstants.COLLECTION_MANAGERS)
            .document(uid)
            .set(manager.toMap())
            .await()

        return manager
    }

    /**
     * Returns all managers for a company.
     * ⚠ Always filters by companyId.
     */
    suspend fun getManagersByCompany(companyId: String): List<Manager> {
        val snapshot = db.collection(AppConstants.COLLECTION_MANAGERS)
            .whereEqualTo(AppConstants.FIELD_COMPANY_ID, companyId)
            .get().await()
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(Manager::class.java)?.copy(documentId = doc.id)
        }
    }

    /** Returns a single manager by their Firebase UID. */
    suspend fun getManagerByUid(uid: String): Manager? {
        val doc = db.collection(AppConstants.COLLECTION_MANAGERS)
            .document(uid).get().await()
        return doc.toObject(Manager::class.java)?.copy(documentId = doc.id)
    }

    /** Returns a single manager by their sequential mid and companyId. */
    suspend fun getManagerByMid(companyId: String, mid: Int): Manager? {
        val snapshot = db.collection(AppConstants.COLLECTION_MANAGERS)
            .whereEqualTo(AppConstants.FIELD_COMPANY_ID, companyId)
            .whereEqualTo("mid", mid)
            .get().await()
        return snapshot.documents.firstOrNull()?.let { doc ->
            doc.toObject(Manager::class.java)?.copy(documentId = doc.id)
        }
    }

    /** Returns manager count for a company. */
    suspend fun getManagerCount(companyId: String): Int {
        return db.collection(AppConstants.COLLECTION_MANAGERS)
            .whereEqualTo(AppConstants.FIELD_COMPANY_ID, companyId)
            .get().await().size()
    }

    /** Updates manager details. */
    suspend fun updateManager(managerUid: String, name: String, email: String, department: String) {
        val updates = mutableMapOf<String, Any>()
        if (name.isNotBlank()) updates["name"] = name.trim()
        if (email.isNotBlank()) updates["email"] = email.trim()
        if (department.isNotBlank()) updates["department"] = department.trim()
        
        if (updates.isNotEmpty()) {
            db.collection(AppConstants.COLLECTION_MANAGERS)
                .document(managerUid)
                .update(updates)
                .await()
        }
    }

    /** Deletes manager Firestore document and completely recycles their mid so it can be re-used. */
    suspend fun deleteManager(managerUid: String) {
        val doc = db.collection(AppConstants.COLLECTION_MANAGERS).document(managerUid).get().await()
        val manager = doc.toObject(Manager::class.java)
        if (manager != null) {
            idGen.recycleMid(manager.companyId, manager.mid)
        }
        db.collection(AppConstants.COLLECTION_MANAGERS).document(managerUid).delete().await()
    }
}
