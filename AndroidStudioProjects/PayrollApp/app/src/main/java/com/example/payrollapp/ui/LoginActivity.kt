package com.example.payrollapp.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.payrollapp.databinding.ActivityLoginBinding
import com.example.payrollapp.model.*
import com.example.payrollapp.network.RetrofitClient
import com.example.payrollapp.utils.SessionManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Initialize View Binding
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2. Initialize Session and Network Client
        sessionManager = SessionManager(this)
        RetrofitClient.init(this) // CRITICAL: Initialize Retrofit with context

        // 3. Auto-Login Check
        if (sessionManager.isLoggedIn()) {
            navigateToDashboard()
            return
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                performLogin(username, password)
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    private fun performLogin(user: String, pass: String) {
        lifecycleScope.launch {
            try {
                // Show loading state (if you have a progress bar)
                binding.btnLogin.isEnabled = false

                val loginReq = LoginRequest(username = user, password = pass)
                val response = RetrofitClient.instance.login(loginReq)

                if (response.isSuccessful) {
                    val authResponse = response.body()

                    if (authResponse?.token != null) {
                        Log.d("LOGIN_SUCCESS", "EmpId: ${authResponse.empId}")

                        // Save credentials to SharedPreferences
                        sessionManager.saveAuthToken(authResponse.token)
                        sessionManager.saveEmployeeId(authResponse.empId)

                        Toast.makeText(this@LoginActivity, "Login Successful", Toast.LENGTH_SHORT).show()
                        navigateToDashboard()
                    } else {
                        Toast.makeText(this@LoginActivity, "Invalid server response", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    handleErrorResponse(response.code())
                }
            } catch (e: Exception) {
                Log.e("LOGIN_ERROR", "Error: ${e.message}")
                Toast.makeText(this@LoginActivity, "Connection Error: Is server running at 10.0.2.2?", Toast.LENGTH_LONG).show()
            } finally {
                binding.btnLogin.isEnabled = true
            }
        }
    }

    private fun handleErrorResponse(code: Int) {
        val msg = when (code) {
            401 -> "Invalid Username or Password"
            404 -> "Server endpoint not found"
            500 -> "Server is having trouble. Try later"
            else -> "Error code: $code"
        }
        Toast.makeText(this@LoginActivity, msg, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        startActivity(intent)
        finish() // Close LoginActivity so user can't "back" into it
    }
}