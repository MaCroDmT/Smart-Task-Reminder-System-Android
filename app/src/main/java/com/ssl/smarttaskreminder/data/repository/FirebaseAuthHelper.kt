package com.ssl.smarttaskreminder.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Bypasses the native Firebase Android SDK auth caching behavior by using the
 * Google Identity Toolkit REST API. This allows creating new users without
 * logging out the currently authenticated user in the local Android SDK.
 */
object FirebaseAuthHelper {

    suspend fun createUserSilently(email: String, password: String, apiKey: String): String = withContext(Dispatchers.IO) {
        val url = URL("https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=$apiKey")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.doOutput = true

        val requestBody = JSONObject().apply {
            put("email", email)
            put("password", password)
            put("returnSecureToken", true)
        }

        conn.outputStream.write(requestBody.toString().toByteArray())

        if (conn.responseCode in 200..299) {
            val response = conn.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(response)
            return@withContext json.getString("localId")
        } else {
            val errorResponse = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: "Unknown error"
            val errorMessage = try {
                val errorJson = JSONObject(errorResponse).getJSONObject("error")
                errorJson.getString("message")
            } catch (e: Exception) {
                "Failed to create user: ${conn.responseCode}"
            }
            throw Exception(errorMessage)
        }
    }

    /**
     * Updates a user's password using the REST API.
     */
    suspend fun updateUserPassword(uid: String, newPassword: String, apiKey: String) = withContext(Dispatchers.IO) {
        val url = URL("https://identitytoolkit.googleapis.com/v1/accounts:update?key=$apiKey")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.doOutput = true

        val requestBody = JSONObject().apply {
            put("localId", uid)
            put("password", newPassword)
            put("returnSecureToken", true)
        }

        conn.outputStream.write(requestBody.toString().toByteArray())

        if (conn.responseCode !in 200..299) {
            val errorResponse = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: "Unknown error"
            val errorMessage = try {
                val errorJson = JSONObject(errorResponse).getJSONObject("error")
                errorJson.getString("message")
            } catch (e: Exception) {
                "Failed to update password: ${conn.responseCode}"
            }
            throw Exception(errorMessage)
        }
    }
}
