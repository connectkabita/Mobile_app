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
import com.example.payrollapp.model.LeaveTypeDetails
import com.example.payrollapp.network.RetrofitClient
import com.example.payrollapp.utils.SessionManager
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.*

class ApplyLeaveActivity : AppCompatActivity() {

    private lateinit var binding: ActivityApplyLeaveBinding
    private lateinit var sessionManager: SessionManager

    // Default to Annual Leave (ID 2) as seen in your DB screenshots
    private var selectedLeaveTypeId: Int = 2
    private var selectedLeaveTypeName: String = "Annual Leave"
    private var currentAvailableBalance: Double = 0.0
    private var isBalanceLoaded: Boolean = false

    private var startDateObj: LocalDate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApplyLeaveBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Apply Leave"

        sessionManager = SessionManager(this)
        RetrofitClient.init(this)

        setupLeaveTypeSpinner()

        binding.swipeRefreshLayout.setOnRefreshListener { fetchCurrentBalance() }

        // Initial fetch for the default selected type
        fetchCurrentBalance()

        binding.etStartDate.setOnClickListener { showStartDatePicker() }
        binding.etEndDate.setOnClickListener { showEndDatePicker() }
        binding.btnSubmitLeave.setOnClickListener { submitApplication() }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun setupLeaveTypeSpinner() {
        // Order MUST match your database leave_type_id (1 to 7)
        val types = arrayOf(
            "Unpaid Leave",    // ID 1
            "Annual Leave",    // ID 2
            "Sick Leave",      // ID 3
            "Casual Leave",    // ID 4
            "Maternity Leave", // ID 5
            "Paternity Leave", // ID 6
            "Paid Leave"       // ID 7
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, types)
        val dropdown = binding.spinnerLeaveType as? AutoCompleteTextView
        dropdown?.setAdapter(adapter)

        dropdown?.setOnItemClickListener { _, _, position, _ ->
            selectedLeaveTypeName = types[position]
            // database ID starts at 1, array index starts at 0
            selectedLeaveTypeId = position + 1

            isBalanceLoaded = false
            currentAvailableBalance = 0.0
            binding.tvBalanceIndicator.text = "Syncing balance..."

            fetchCurrentBalance()
        }
    }

    private fun fetchCurrentBalance() {
        val empId = sessionManager.getEmployeeId()
        if (empId <= 0) return

        binding.swipeRefreshLayout.isRefreshing = true
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getLeaveBalance(empId)
                if (response.isSuccessful) {
                    val balances = response.body() ?: emptyList()

                    // Match based on the ID we derived from the spinner position
                    val match = balances.find { it.leaveType?.leaveTypeId == selectedLeaveTypeId }

                    if (match != null) {
                        currentAvailableBalance = match.currentBalanceDays ?: 0.0
                        isBalanceLoaded = true
                        binding.tvBalanceIndicator.text = "Available: ${currentAvailableBalance.toInt()} days"
                    } else {
                        // If no record exists in leave_balance table for this ID
                        currentAvailableBalance = 0.0
                        isBalanceLoaded = true
                        binding.tvBalanceIndicator.text = "No balance found (0 days)"
                    }
                }
            } catch (e: Exception) {
                Log.e("LEAVE_DEBUG", "Balance fetch error: ${e.message}")
                binding.tvBalanceIndicator.text = "Error loading balance"
            } finally {
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun showStartDatePicker() {
        val calendar = Calendar.getInstance()
        val dialog = DatePickerDialog(this, { _, year, month, day ->
            val dateStr = String.format("%04d-%02d-%02d", year, month + 1, day)
            binding.etStartDate.setText(dateStr)
            startDateObj = LocalDate.of(year, month + 1, day)
            binding.etEndDate.setText("") // Reset end date to ensure valid range
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

        dialog.datePicker.minDate = System.currentTimeMillis()
        dialog.show()
    }

    private fun showEndDatePicker() {
        if (startDateObj == null) {
            Toast.makeText(this, "Select Start Date first", Toast.LENGTH_SHORT).show()
            return
        }

        val calendar = Calendar.getInstance()
        val dialog = DatePickerDialog(this, { _, year, month, day ->
            val dateStr = String.format("%04d-%02d-%02d", year, month + 1, day)
            binding.etEndDate.setText(dateStr)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

        val minCalendar = Calendar.getInstance()
        minCalendar.set(startDateObj!!.year, startDateObj!!.monthValue - 1, startDateObj!!.dayOfMonth)
        dialog.datePicker.minDate = minCalendar.timeInMillis

        dialog.show()
    }

    private fun submitApplication() {
        val startStr = binding.etStartDate.text.toString()
        val endStr = binding.etEndDate.text.toString()
        val reason = binding.etReason.text.toString()

        if (startStr.isEmpty() || endStr.isEmpty() || reason.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val start = LocalDate.parse(startStr)
            val end = LocalDate.parse(endStr)
            val requestedDays = ChronoUnit.DAYS.between(start, end) + 1

            // Validate balance for paid leave types
            if (!selectedLeaveTypeName.equals("Unpaid Leave", ignoreCase = true)) {
                if (!isBalanceLoaded) {
                    Toast.makeText(this, "Waiting for balance sync...", Toast.LENGTH_SHORT).show()
                    return
                }
                if (requestedDays > currentAvailableBalance) {
                    // This triggers if DB has no record or low balance
                    Toast.makeText(this, "Insufficient Balance! Required: $requestedDays, Available: ${currentAvailableBalance.toInt()}", Toast.LENGTH_LONG).show()
                    return
                }
            }

            performSubmission(startStr, endStr, reason, requestedDays.toInt())
        } catch (e: Exception) {
            Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show()
        }
    }

    private fun performSubmission(start: String, end: String, reason: String, days: Int) {
        val request = LeaveRequest(
            employee = EmployeeIdWrapper(sessionManager.getEmployeeId()),
            leaveType = LeaveTypeDetails(leaveTypeId = selectedLeaveTypeId),
            startDate = start,
            endDate = end,
            totalDays = days,
            reason = reason,
            status = "Pending"
        )

        lifecycleScope.launch {
            binding.btnSubmitLeave.isEnabled = false
            try {
                val response = RetrofitClient.instance.applyLeave(request)
                if (response.isSuccessful) {
                    Toast.makeText(this@ApplyLeaveActivity, "Application Submitted successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@ApplyLeaveActivity, "Failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ApplyLeaveActivity, "Network Error", Toast.LENGTH_SHORT).show()
            } finally {
                binding.btnSubmitLeave.isEnabled = true
            }
        }
    }
}