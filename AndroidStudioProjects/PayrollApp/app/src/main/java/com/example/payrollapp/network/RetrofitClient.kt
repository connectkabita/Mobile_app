package com.example.payrollapp.network

import android.content.Context
import com.example.payrollapp.utils.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // USE "http://10.0.2.2:8080/" for Android Emulator
    // USE "http://192.168.x.x:8080/" for Physical Device (your PC's IP)
    private const val BASE_URL = "http://10.0.2.2:8080/"
    private var sessionManager: SessionManager? = null

    /**
     * Initialize this in your LoginActivity or Application class
     * before making any API calls.
     */
    fun init(context: Context) {
        if (sessionManager == null) {
            sessionManager = SessionManager(context)
        }
    }

    private val logger = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logger)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val requestBuilder = chain.request().newBuilder()

            // Automatically attach the JWT token if it exists in SessionManager
            val token = sessionManager?.getAuthToken()
            if (!token.isNullOrEmpty()) {
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }

            chain.proceed(requestBuilder.build())
        }
        .build()

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(ApiService::class.java)
    }
}