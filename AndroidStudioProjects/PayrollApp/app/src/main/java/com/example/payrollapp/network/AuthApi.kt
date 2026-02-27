package com.example.payrollapp.network

import com.example.payrollapp.model.* // Imports LoginRequest, AuthResponse, SignupRequest, etc.
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    // 1. Standard Login (Using suspend for Coroutines if needed)
    @POST("api/auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<AuthResponse>

    // 2. Signup (Using Call for standard enqueue logic)
    @POST("api/auth/signup")
    fun signup(@Body request: SignupRequest): Call<GeneralResponse>

    // 3. Forgot Password - Sends the OTP Email
    @POST("api/auth/forgot-password")
    fun forgotPassword(@Body request: ForgotPasswordRequest): Call<GeneralResponse>

    // 4. Reset Password - Updates the password in DB
    @POST("api/auth/reset-password")
    fun resetPassword(@Body request: ResetPasswordRequest): Call<GeneralResponse>
}