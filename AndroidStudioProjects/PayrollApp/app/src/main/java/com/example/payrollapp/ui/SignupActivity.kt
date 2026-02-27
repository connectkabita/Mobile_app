package com.example.payrollapp.ui

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.payrollapp.databinding.ActivitySignupBinding
import com.example.payrollapp.model.SignupRequest
import com.example.payrollapp.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response // <--- CRITICAL: This was likely missing
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding

    companion object {
        private const val TAG = "SignupActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnSignup.setOnClickListener { performSignup() }
    }

    private fun performSignup() {
        val fullName = binding.etSignupName.text.toString().trim()
        val email = binding.etSignupEmail.text.toString().trim()
        val username = binding.etSignupUsername.text.toString().trim()
        val password = binding.etSignupPassword.text.toString().trim()
        val contact = binding.etSignupContact.text.toString().trim()

        if (fullName.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val nameParts = fullName.split(" ")
        val fName = nameParts.getOrNull(0) ?: ""
        val lName = if (nameParts.size > 1) nameParts.drop(1).joinToString(" ") else "N/A"

        val signupReq = SignupRequest(
            username = username,
            email = email,
            password = password,
            firstName = fName,
            lastName = lName,
            address = binding.etSignupAddress.text.toString().ifEmpty { "N/A" },
            contact = contact,
            education = "N/A",
            maritalStatus = "Single",
            roleId = 2,
            basicSalary = 0.0,
            employmentStatus = "ACTIVE",
            joiningDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
            isActive = true,
            positionId = 1,
            departmentId = 1
        )

        lifecycleScope.launch {
            try {
                // We use withContext to ensure the network call happens on the IO thread
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.instance.signup(signupReq)
                }

                // Check isSuccessful on the Retrofit Response object
                if (response.isSuccessful) {
                    Toast.makeText(this@SignupActivity, "Registration Successful!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    // response.code() is a function in Retrofit, calling it explicitly here
                    val statusCode = response.code()
                    val errorBodyString = response.errorBody()?.string() ?: "Unknown error"

                    Log.e(TAG, "Signup failed with code: $statusCode, error: $errorBodyString")
                    Toast.makeText(this@SignupActivity, "Failed: $statusCode", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during signup: ${e.message}")
                Toast.makeText(this@SignupActivity, "Network Error", Toast.LENGTH_SHORT).show()
            }
        }
    }
}