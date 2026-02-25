package com.example.payrollapp.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("payroll_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_EMP_ID = "emp_id"
        private const val TAG = "SESSION_MANAGER"
    }

    /**
     * Saves the JWT token.
     * Added a check to ensure we don't save empty or "null" literal strings.
     */
    fun saveAuthToken(token: String?) {
        if (!token.isNullOrBlank() && token != "null") {
            Log.d(TAG, "Saving Token: ${token.take(10)}...")
            prefs.edit().putString(KEY_AUTH_TOKEN, token).apply()
        } else {
            Log.e(TAG, "Attempted to save an invalid/null token!")
        }
    }

    /**
     * Retrieves the JWT token.
     */
    fun getAuthToken(): String? {
        val token = prefs.getString(KEY_AUTH_TOKEN, null)
        // Ensure we return null if the string is empty or literally "null"
        return if (token.isNullOrBlank() || token == "null") null else token
    }

    // Alias for compatibility with your existing RetrofitClient calls
    fun fetchAuthToken(): String? = getAuthToken()

    /**
     * Saves the Employee ID.
     */
    fun saveEmployeeId(empId: Int?) {
        val idToSave = empId ?: -1
        if (idToSave != -1) {
            Log.d(TAG, "Saving EmpID: $idToSave")
            prefs.edit().putInt(KEY_EMP_ID, idToSave).apply()
        }
    }

    /**
     * Retrieves the stored Employee ID. Returns -1 if not found.
     */
    fun getEmployeeId(): Int {
        val id = prefs.getInt(KEY_EMP_ID, -1)
        return id
    }

    /**
     * Checks if a valid session exists.
     */
    fun isLoggedIn(): Boolean {
        val token = getAuthToken()
        val hasId = getEmployeeId() != -1
        return !token.isNullOrBlank() && hasId
    }

    /**
     * Clears all session data on logout.
     */
    fun logout() {
        prefs.edit().clear().apply()
        Log.d(TAG, "Session cleared successfully.")
    }
}