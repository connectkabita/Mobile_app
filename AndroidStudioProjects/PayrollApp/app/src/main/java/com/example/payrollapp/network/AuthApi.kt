package com.example.payrollapp.network

import com.example.payrollapp.model.AuthResponse
import com.example.payrollapp.model.LoginRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    // Renamed to 'login' to match your LoginActivity.kt call: RetrofitClient.instance.login(loginReq)
    @POST("login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<AuthResponse>
}