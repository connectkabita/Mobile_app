package com.example.payrollapp.network

import com.example.payrollapp.model.AuthResponse
import com.example.payrollapp.model.LoginRequest

class LoginRepository(private val apiService: ApiService) {
    // This must take LoginRequest to match your ApiService.kt
    suspend fun login(request: LoginRequest): AuthResponse? {
        val response = apiService.login(request)
        return if (response.isSuccessful) response.body() else null
    }
}