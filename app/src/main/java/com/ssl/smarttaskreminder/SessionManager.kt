package com.ssl.smarttaskreminder

/**
 * SessionManager — holds the authenticated user's session data in memory.
 * Populated once after login, cleared on logout.
 * All screens read companyId from here for every Firestore query.
 *
 * This is an object (singleton) — always the same instance in one app process.
 */
object SessionManager {

    // Firebase Auth UID of the logged-in user
    var firebaseUid: String = ""

    // The role determines which dashboard to show
    var role: String = ""

    // companyId — MANDATORY for all Firestore queries (except companies collection)
    var companyId: String = ""

    // Display info
    var userName: String = ""
    var userEmail: String = ""
    var companyName: String = ""

    // Sequential IDs (loaded from Firestore profile after login)
    var adminId: Int = 0      // aid — if role == admin
    var managerId: Int = 0    // mid — if role == manager
    var userId: Int = 0       // uid — if role == user

    /**
     * Returns true if the session is valid (user is logged in).
     */
    fun isLoggedIn(): Boolean = firebaseUid.isNotEmpty() && role.isNotEmpty()

    /**
     * Returns true if this session belongs to the Super Admin.
     */
    fun isSuperAdmin(): Boolean = role == AppConstants.ROLE_SUPER_ADMIN

    /**
     * Returns true if this session belongs to an Admin.
     */
    fun isAdmin(): Boolean = role == AppConstants.ROLE_ADMIN

    /**
     * Returns true if this session belongs to a Manager.
     */
    fun isManager(): Boolean = role == AppConstants.ROLE_MANAGER

    /**
     * Returns true if this session belongs to a User.
     */
    fun isUser(): Boolean = role == AppConstants.ROLE_USER

    /**
     * Clears all session data (called on logout).
     */
    fun clear() {
        firebaseUid = ""
        role = ""
        companyId = ""
        userName = ""
        userEmail = ""
        companyName = ""
        adminId = 0
        managerId = 0
        userId = 0
    }
}
