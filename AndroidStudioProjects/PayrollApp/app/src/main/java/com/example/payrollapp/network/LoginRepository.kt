package com.example.payrollapp.network

import com.example.payrollapp.model.LoginRequest
import com.example.payrollapp.model.AuthResponse
import android.util.Log

class LoginRepository(private val apiService: ApiService) { // Changed from AuthApi to ApiService

    suspend fun login(loginRequest: LoginRequest): AuthResponse? {
        return try {
            val response = apiService.login(loginRequest)
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e("LoginRepo", "Error: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e("LoginRepo", "Exception: ${e.message}")
            null
        }
    }
}