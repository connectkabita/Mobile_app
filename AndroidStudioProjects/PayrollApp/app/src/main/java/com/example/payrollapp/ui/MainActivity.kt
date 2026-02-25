package com.example.payrollapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.payrollapp.R
import com.example.payrollapp.model.LoginRequest
import com.example.payrollapp.network.RetrofitClient
import com.example.payrollapp.network.LoginRepository
import com.example.payrollapp.utils.SessionManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var loginRepository: LoginRepository
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // IMPORTANT: Initialize Retrofit with context so it can access saved tokens
        RetrofitClient.init(this)

        // 1. Initialize logic components
        loginRepository = LoginRepository(RetrofitClient.instance)
        sessionManager = SessionManager(this)

        val usernameField = findViewById<EditText>(R.id.etUsername)
        val passwordField = findViewById<EditText>(R.id.etPassword)
        val loginButton = findViewById<Button>(R.id.btnLogin)
        val signupText = findViewById<TextView>(R.id.tvSignup)

        loginButton.setOnClickListener {
            val user = usernameField.text.toString().trim()
            val pass = passwordField.text.toString().trim()

            if (user.isNotEmpty() && pass.isNotEmpty()) {
                performLogin(user, pass)
            } else {
                Toast.makeText(this, "Please enter all details", Toast.LENGTH_SHORT).show()
            }
        }

        signupText.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    private fun performLogin(user: String, pass: String) {
        val loginData = LoginRequest(user, pass)

        lifecycleScope.launch {
            try {
                val authResponse = loginRepository.login(loginData)

                if (authResponse != null && authResponse.token != null) {
                    // Save credentials to SessionManager
                    sessionManager.saveAuthToken(authResponse.token)
                    authResponse.empId?.let { sessionManager.saveEmployeeId(it) }

                    Toast.makeText(this@MainActivity, "Login Successful!", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this@MainActivity, DashboardActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@MainActivity, "Invalid Username or Password", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}