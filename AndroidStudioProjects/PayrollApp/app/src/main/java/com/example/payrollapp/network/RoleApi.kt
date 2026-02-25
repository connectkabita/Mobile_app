package com.example.payrollapp.network

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface RoleApi {
    @GET("user/role/{username}")
    fun getUserRole(@Path("username") username: String): Call<Map<String, String>>
}