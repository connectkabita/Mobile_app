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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var loginRepository: LoginRepository
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Retrofit and SessionManager
        RetrofitClient.init(this)

        // FIXED: RetrofitClient.instance now returns ApiService,
        // which matches the updated LoginRepository constructor.
        loginRepository = LoginRepository(RetrofitClient.instance)
        sessionManager = SessionManager(this)

        // UI References
        val usernameField = findViewById<EditText>(R.id.etUsername)
        val passwordField = findViewById<EditText>(R.id.etPassword)
        val loginButton = findViewById<Button>(R.id.btnLogin)
        val signupText = findViewById<TextView>(R.id.tvSignup)
        val forgotPasswordText = findViewById<TextView>(R.id.tvForgotPassword)

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

        // OTP-only Forgot Password flow (No reset password logic)
        forgotPasswordText.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    private fun performLogin(user: String, pass: String) {
        val loginData = LoginRequest(user, pass)

        lifecycleScope.launch {
            try {
                // Execute network call on IO thread
                val authResponse = withContext(Dispatchers.IO) {
                    loginRepository.login(loginData)
                }

                if (authResponse != null && authResponse.token != null) {
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
                Toast.makeText(this@MainActivity, "Connection Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}