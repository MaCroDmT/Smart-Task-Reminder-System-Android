package com.ssl.smarttaskreminder.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ssl.smarttaskreminder.AppConstants
import com.ssl.smarttaskreminder.SessionManager
import com.ssl.smarttaskreminder.data.model.Admin
import com.ssl.smarttaskreminder.data.model.Manager
import com.ssl.smarttaskreminder.data.model.User
import kotlinx.coroutines.tasks.await

/**
 * AuthRepository — handles Firebase Auth login and post-login role resolution.
 *
 * After Firebase Auth succeeds, this repo queries Firestore to find the user's
 * role (admin / manager / user) and populates SessionManager with companyId.
 *
 * ⚠ Super Admin is identified by email match only — they have no Firestore profile.
 */
class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    /**
     * Logs in with email/password. On success, resolves the role from Firestore
     * and fills SessionManager. Returns the resolved role string, or throws an exception.
     */
    suspend fun login(email: String, password: String): String {
        // Step 1 — Firebase Auth
        val result = auth.signInWithEmailAndPassword(email, password).await()
        val uid = result.user?.uid ?: throw Exception("Authentication failed")

        // Step 2 — Super Admin check (by email — no Firestore profile needed)
        if (email.trim().lowercase() == AppConstants.SUPER_ADMIN_EMAIL.lowercase()) {
            SessionManager.firebaseUid = uid
            SessionManager.role        = AppConstants.ROLE_SUPER_ADMIN
            SessionManager.userEmail   = email
            SessionManager.userName    = "Super Admin"
            SessionManager.companyId   = ""  // Super Admin has no single companyId
            return AppConstants.ROLE_SUPER_ADMIN
        }

        // Step 3 — Check admins collection
        val adminDoc = db.collection(AppConstants.COLLECTION_ADMINS).document(uid).get().await()
        if (adminDoc.exists()) {
            val admin = adminDoc.toObject(Admin::class.java)!!.copy(documentId = uid)
            SessionManager.firebaseUid = uid
            SessionManager.role        = AppConstants.ROLE_ADMIN
            SessionManager.userEmail   = admin.email
            SessionManager.userName    = admin.name
            SessionManager.companyId   = admin.companyId
            SessionManager.adminId     = admin.aid
            loadCompanyName(admin.companyId)
            return AppConstants.ROLE_ADMIN
        }

        // Step 4 — Check managers collection
        val managerDoc = db.collection(AppConstants.COLLECTION_MANAGERS).document(uid).get().await()
        if (managerDoc.exists()) {
            val manager = managerDoc.toObject(Manager::class.java)!!.copy(documentId = uid)
            SessionManager.firebaseUid = uid
            SessionManager.role        = AppConstants.ROLE_MANAGER
            SessionManager.userEmail   = manager.email
            SessionManager.userName    = manager.name
            SessionManager.companyId   = manager.companyId
            SessionManager.managerId   = manager.mid
            loadCompanyName(manager.companyId)
            updateFcmToken(uid)
            return AppConstants.ROLE_MANAGER
        }

        // Step 5 — Check users collection
        val userDoc = db.collection(AppConstants.COLLECTION_USERS).document(uid).get().await()
        if (userDoc.exists()) {
            val user = userDoc.toObject(User::class.java)!!.copy(documentId = uid)
            SessionManager.firebaseUid = uid
            SessionManager.role        = AppConstants.ROLE_USER
            SessionManager.userEmail   = user.email
            SessionManager.userName    = user.name
            SessionManager.companyId   = user.companyId
            SessionManager.userId      = user.uid
            SessionManager.managerId   = user.managerId
            loadCompanyName(user.companyId)
            updateFcmToken(uid)
            return AppConstants.ROLE_USER
        }

        // No profile found — sign out and throw
        auth.signOut()
        throw Exception("No user profile found. Contact your administrator.")
    }

    private fun updateFcmToken(uid: String) {
        com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                db.collection(AppConstants.COLLECTION_USERS).document(uid).update("fcmToken", token)
                db.collection(AppConstants.COLLECTION_ADMINS).document(uid).update("fcmToken", token)
                db.collection(AppConstants.COLLECTION_MANAGERS).document(uid).update("fcmToken", token)
            }
        }
    }

    /**
     * Checks if a user is already signed in and resolves their role.
     * Used by SplashActivity to skip the login screen.
     * Returns the resolved role, or null if not signed in.
     */
    suspend fun resolveCurrentUser(): String? {
        val user = auth.currentUser ?: return null
        val role = try {
            login(user.email ?: return null, "") // won't re-authenticate — just resolves profile
        } catch (e: Exception) {
            // If re-auth fails (expected with empty password), just re-resolve profile
            resolveProfileOnly(user.uid, user.email ?: "")
        }
        if (role != null) updateFcmToken(user.uid)
        return role
    }

    private suspend fun resolveProfileOnly(uid: String, email: String): String? {
        if (email.trim().lowercase() == AppConstants.SUPER_ADMIN_EMAIL.lowercase()) {
            SessionManager.firebaseUid = uid
            SessionManager.role        = AppConstants.ROLE_SUPER_ADMIN
            SessionManager.userEmail   = email
            SessionManager.userName    = "Super Admin"
            SessionManager.companyId   = ""
            return AppConstants.ROLE_SUPER_ADMIN
        }

        val adminDoc = db.collection(AppConstants.COLLECTION_ADMINS).document(uid).get().await()
        if (adminDoc.exists()) {
            val admin = adminDoc.toObject(Admin::class.java)!!.copy(documentId = uid)
            SessionManager.firebaseUid = uid; SessionManager.role = AppConstants.ROLE_ADMIN
            SessionManager.userEmail = admin.email; SessionManager.userName = admin.name
            SessionManager.companyId = admin.companyId; SessionManager.adminId = admin.aid
            loadCompanyName(admin.companyId)
            return AppConstants.ROLE_ADMIN
        }

        val managerDoc = db.collection(AppConstants.COLLECTION_MANAGERS).document(uid).get().await()
        if (managerDoc.exists()) {
            val manager = managerDoc.toObject(Manager::class.java)!!.copy(documentId = uid)
            SessionManager.firebaseUid = uid; SessionManager.role = AppConstants.ROLE_MANAGER
            SessionManager.userEmail = manager.email; SessionManager.userName = manager.name
            SessionManager.companyId = manager.companyId; SessionManager.managerId = manager.mid
            loadCompanyName(manager.companyId)
            return AppConstants.ROLE_MANAGER
        }

        val userDoc = db.collection(AppConstants.COLLECTION_USERS).document(uid).get().await()
        if (userDoc.exists()) {
            val user = userDoc.toObject(User::class.java)!!.copy(documentId = uid)
            SessionManager.firebaseUid = uid; SessionManager.role = AppConstants.ROLE_USER
            SessionManager.userEmail = user.email; SessionManager.userName = user.name
            SessionManager.companyId = user.companyId; SessionManager.userId = user.uid
            SessionManager.managerId = user.managerId
            loadCompanyName(user.companyId)
            return AppConstants.ROLE_USER
        }
        return null
    }

    private suspend fun loadCompanyName(companyId: String) {
        try {
            val companyDoc = db.collection(AppConstants.COLLECTION_COMPANIES)
                .document(companyId).get().await()
            SessionManager.companyName = companyDoc.getString("name") ?: ""
        } catch (_: Exception) {}
    }

    fun logout() {
        auth.signOut()
        SessionManager.clear()
    }

    fun isSignedIn(): Boolean = auth.currentUser != null
}
