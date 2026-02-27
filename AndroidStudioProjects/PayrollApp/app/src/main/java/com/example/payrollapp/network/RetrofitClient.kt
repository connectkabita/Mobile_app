package com.example.payrollapp.network

import android.content.Context
import android.util.Log
import com.example.payrollapp.utils.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // Emulator: 10.0.2.2 | Physical Device: Use your laptop's IP (e.g., 192.168.1.x)
    private const val BASE_URL = "http://10.0.2.2:8080/"

    private var sessionManager: SessionManager? = null

    fun init(context: Context) {
        if (sessionManager == null) {
            sessionManager = SessionManager(context.applicationContext)
        }
    }

    private val logger = HttpLoggingInterceptor { message ->
        Log.d("API_LOG", message)
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client by lazy {
        OkHttpClient.Builder()
            .addInterceptor(logger)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request()
                val requestBuilder = request.newBuilder()

                val token = sessionManager?.getToken()
                if (!token.isNullOrBlank()) {
                    val cleanToken = token.replace("Bearer ", "").trim()
                    requestBuilder.addHeader("Authorization", "Bearer $cleanToken")
                }

                requestBuilder.addHeader("Content-Type", "application/json")
                requestBuilder.addHeader("Accept", "application/json")

                val response = chain.proceed(requestBuilder.build())

                if (!response.isSuccessful) {
                    Log.e("API_LOG", "Error Code: ${response.code} for URL: ${request.url}")
                }
                response
            }
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    /**
     * Use this for all API interactions including Login, Signup, and Forgot Password OTP.
     */
    val instance: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}