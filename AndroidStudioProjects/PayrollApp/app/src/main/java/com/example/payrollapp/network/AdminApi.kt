package com.example.payrollapp.network

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header

interface AdminApi {
    @GET("admin/dashboard")
    fun getAdminStats(@Header("Authorization") token: String): Call<Map<String, Any>>
}