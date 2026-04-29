package com.ssl.smarttaskreminder.notifications

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * FCMService — handles incoming Firebase Cloud Messaging push notifications.
 *
 * For now this app uses WorkManager-based local notifications.
 * This service is the entry point for server-pushed FCM messages (future Cloud Functions integration).
 */
class FCMService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title     = remoteMessage.notification?.title ?: "Smart Task Reminder"
        val body      = remoteMessage.notification?.body  ?: ""
        val companyId = remoteMessage.data["companyId"]   ?: ""
        val taskDocId = remoteMessage.data["taskDocId"]   ?: ""

        // Route notification based on data payload
        if (taskDocId.isNotEmpty()) {
            NotificationHelper.showTaskReminder(
                context           = applicationContext,
                taskDocId         = taskDocId,
                taskName          = title,
                styleNo           = remoteMessage.data["styleNo"] ?: "",
                deadlineFormatted = remoteMessage.data["deadline"] ?: "",
                daysLeft          = remoteMessage.data["daysLeft"]?.toLongOrNull() ?: 0
            )
        } else {
            // Generic admin notification
            NotificationHelper.showAdminOverdueAlert(
                context      = applicationContext,
                companyName  = remoteMessage.data["companyName"] ?: "",
                overdueCount = remoteMessage.data["overdueCount"]?.toIntOrNull() ?: 0,
                taskSummary  = body
            )
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Save FCM token to Firestore user profile for server-side push
        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection(com.ssl.smarttaskreminder.AppConstants.COLLECTION_USERS)
                .document(uid)
                .update("fcmToken", token)
            
            // Also update in admins and managers collections just in case
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection(com.ssl.smarttaskreminder.AppConstants.COLLECTION_ADMINS)
                .document(uid)
                .update("fcmToken", token)
            
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection(com.ssl.smarttaskreminder.AppConstants.COLLECTION_MANAGERS)
                .document(uid)
                .update("fcmToken", token)
        }
    }
}
