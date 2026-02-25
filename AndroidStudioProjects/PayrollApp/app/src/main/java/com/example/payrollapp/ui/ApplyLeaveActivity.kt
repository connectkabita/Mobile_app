package com.example.payrollapp.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.payrollapp.databinding.ActivityApplyLeaveBinding
import com.example.payrollapp.model.EmployeeIdWrapper
import com.example.payrollapp.model.LeaveRequest
import com.example.payrollapp.model.LeaveTypeIdWrapper
import com.example.payrollapp.network.RetrofitClient
import com.example.payrollapp.utils.SessionManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ApplyLeaveActivity : AppCompatActivity() {

    private lateinit var binding: ActivityApplyLeaveBinding
    private lateinit var sessionManager: SessionManager
    private var selectedLeaveTypeId: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApplyLeaveBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        setupLeaveTypeSpinner()

        binding.etStartDate.setOnClickListener { showDatePicker { date -> binding.etStartDate.setText(date) } }
        binding.etEndDate.setOnClickListener { showDatePicker { date -> binding.etEndDate.setText(date) } }
        binding.btnSubmitLeave.setOnClickListener { submitApplication() }
    }

    private fun setupLeaveTypeSpinner() {
        val types = arrayOf(
            "Unpaid Leave", "Annual Leave", "Sick Leave",
            "Casual Leave", "Maternity Leave", "Paternity Leave", "Paid Leave"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, types)
        val dropdown = binding.spinnerLeaveType as? AutoCompleteTextView
        dropdown?.setAdapter(adapter)

        dropdown?.setOnItemClickListener { _, _, position, _ ->
            // Adjusting index to match typical DB IDs
            selectedLeaveTypeId = position + 1
        }
    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(this, { _, year, month, day ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(year, month, day)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            onDateSelected(sdf.format(selectedCalendar.time))
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

        datePicker.datePicker.minDate = System.currentTimeMillis()
        datePicker.show()
    }

    private fun submitApplication() {
        val startDate = binding.etStartDate.text.toString()
        val endDate = binding.etEndDate.text.toString()
        val reason = binding.etReason.text.toString()

        if (startDate.isEmpty() || endDate.isEmpty() || reason.isEmpty()) {
            Toast.makeText(this, "Please complete all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val empId = sessionManager.getEmployeeId()

        if (empId == -1) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_LONG).show()
            return
        }

        // Create nested request structure
        val request = LeaveRequest(
            employee = EmployeeIdWrapper(empId),
            leaveType = LeaveTypeIdWrapper(selectedLeaveTypeId),
            startDate = startDate,
            endDate = endDate,
            reason = reason
        )

        lifecycleScope.launch {
            try {
                // FIXED: Removed "Bearer $token" argument.
                // The token is now added automatically by the Retrofit Interceptor.
                val response = RetrofitClient.instance.applyForLeave(request)

                if (response.isSuccessful) {
                    Toast.makeText(this@ApplyLeaveActivity, "Applied Successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("LEAVE_ERROR", "Code: ${response.code()} Body: $errorMsg")
                    Toast.makeText(this@ApplyLeaveActivity, "Failed: $errorMsg", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("LEAVE_NETWORK", "Failed: ${e.message}")
                Toast.makeText(this@ApplyLeaveActivity, "Network Error", Toast.LENGTH_SHORT).show()
            }
        }
    }
}