package com.example.payrollapp.ui

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.payrollapp.databinding.ActivityForgotPasswordBinding
import com.example.payrollapp.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response // <--- REQUIRED: This fixes the 'code' and 'isSuccessful' errors
import okhttp3.ResponseBody // <--- REQUIRED

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityForgotPasswordBinding

    companion object {
        private const val TAG = "ForgotPasswordActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        RetrofitClient.init(this)

        binding.btnResetPassword.setOnClickListener {
            val email = binding.etForgotEmail.text.toString().trim()

            if (email.isEmpty()) {
                binding.etForgotEmail.error = "Email is required"
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.etForgotEmail.error = "Please enter a valid email address"
            } else {
                sendOtpRequest(email)
            }
        }
    }

    private fun sendOtpRequest(email: String) {
        binding.btnResetPassword.isEnabled = false

        lifecycleScope.launch {
            try {
                // Backend controller expects @RequestBody Map<String, String>
                val emailMap = mapOf("email" to email)

                // Specify the Response type explicitly to avoid compiler confusion
                val response: Response<ResponseBody> = withContext(Dispatchers.IO) {
                    RetrofitClient.instance.forgotPassword(emailMap)
                }

                binding.btnResetPassword.isEnabled = true

                if (response.isSuccessful) {
                    Log.d(TAG, "Success: OTP Sent")
                    Toast.makeText(this@ForgotPasswordActivity, "OTP sent to your email!", Toast.LENGTH_LONG).show()
                    // Stay on this screen as reset-password logic is removed
                } else {
                    val statusCode = response.code()
                    val errorMsg = when (statusCode) {
                        404 -> "Email not found."
                        else -> "Server error: $statusCode"
                    }
                    Toast.makeText(this@ForgotPasswordActivity, errorMsg, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.btnResetPassword.isEnabled = true
                Log.e(TAG, "Network error: ${e.message}")
                Toast.makeText(this@ForgotPasswordActivity, "Connection failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
}