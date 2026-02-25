package com.example.payrollapp.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.payrollapp.databinding.ActivitySignupBinding
import com.example.payrollapp.model.SignupRequest
import com.example.payrollapp.network.RetrofitClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding

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
        val address = binding.etSignupAddress.text.toString().trim()
        val education = binding.etSignupEducation.text.toString().trim()
        val maritalStatus = binding.etSignupMaritalStatus.text.toString().trim()

        // Validation: Ensure mandatory fields aren't empty
        if (fullName.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty() || contact.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        // 1. Handle Name Splitting (DB expects first_name and last_name)
        val nameParts = fullName.split(" ")
        val fName = nameParts.getOrNull(0) ?: ""
        val lName = if (nameParts.size > 1) nameParts.drop(1).joinToString(" ") else "N/A"

        // 2. Create the Request Object with all fields required by your Model
        val signupReq = SignupRequest(
            username = username,
            email = email,
            password = password,
            firstName = fName,
            lastName = lName,
            address = address.ifEmpty { "Not Provided" },
            contact = contact,
            education = education.ifEmpty { "N/A" },
            maritalStatus = maritalStatus.ifEmpty { "Single" },
            // Default values for fields not in your UI form:
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
                // Use the Retrofit Logging Interceptor we set up earlier to see this in Logcat
                val response = RetrofitClient.instance.signup(signupReq)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        Toast.makeText(this@SignupActivity, "Registration Successful!", Toast.LENGTH_SHORT).show()
                        finish() // Go back to Login
                    } else {
                        Toast.makeText(this@SignupActivity, body?.message ?: "Signup Failed", Toast.LENGTH_LONG).show()
                    }
                } else {
                    // This catches the 500 error and displays it
                    val errorMsg = response.errorBody()?.string() ?: "Unknown Server Error"
                    android.util.Log.e("SignupError", "Error Code: ${response.code()} Body: $errorMsg")
                    Toast.makeText(this@SignupActivity, "Server Error (500): Check Logcat", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                android.util.Log.e("SignupError", "Exception: ${e.message}")
                Toast.makeText(this@SignupActivity, "Connection Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}