package com.example.payrollapp.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "payroll_prefs"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_EMP_ID = "emp_id"
        private const val TAG = "SESSION_MANAGER"
    }

    /**
     * Saves the JWT token.
     * Handles the "Bearer " prefix logic here to simplify the Interceptor.
     */
    fun saveAuthToken(token: String?) {
        if (!token.isNullOrBlank() && token != "null") {
            // Ensure token is stored consistently
            val formattedToken = if (token.startsWith("Bearer ")) token else "Bearer $token"
            Log.d(TAG, "Saving Token: ${formattedToken.take(15)}...")
            prefs.edit().putString(KEY_AUTH_TOKEN, formattedToken).apply()
        } else {
            Log.e(TAG, "Attempted to save an invalid/null token!")
        }
    }

    fun getToken(): String? {
        val token = prefs.getString(KEY_AUTH_TOKEN, null)
        return if (token.isNullOrBlank() || token == "null") null else token
    }

    fun saveEmployeeId(empId: Int?) {
        if (empId != null && empId > 0) {
            Log.d(TAG, "Saving EmpID: $empId")
            prefs.edit().putInt(KEY_EMP_ID, empId).apply()
        } else {
            Log.w(TAG, "Invalid EmpID provided: $empId")
        }
    }

    fun getEmployeeId(): Int {
        val id = prefs.getInt(KEY_EMP_ID, -1)
        Log.d(TAG, "Retrieving EmpID: $id")
        return id
    }

    fun isLoggedIn(): Boolean {
        return !getToken().isNullOrBlank() && getEmployeeId() != -1
    }

    fun logout() {
        prefs.edit().clear().apply()
        Log.i(TAG, "User session cleared.")
    }
}