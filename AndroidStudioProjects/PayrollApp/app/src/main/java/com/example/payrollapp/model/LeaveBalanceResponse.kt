package com.example.payrollapp.model

import com.google.gson.annotations.SerializedName

/**
 * Main Leave Request Model
 */
data class LeaveRequest(
    @SerializedName("leaveId")
    val leaveId: Int? = null,

    @SerializedName("employee")
    val employee: EmployeeIdWrapper?,

    @SerializedName("leaveType")
    val leaveType: LeaveTypeDetails?,

    @SerializedName("startDate")
    val startDate: String?,

    @SerializedName("endDate")
    val endDate: String?,

    @SerializedName("totalDays")
    val totalDays: Int? = 0,

    @SerializedName("reason")
    val reason: String? = "",

    @SerializedName("status")
    val status: String? = "Pending"
)

/**
 * Matches your DB: balance_id, current_balance_days, etc.
 */
data class LeaveBalanceResponse(
    @SerializedName("balanceId")
    val balanceId: Int?,

    @SerializedName("year")
    val year: Int?,

    // Changed to Number to handle both 10 and 10.0 safely
    @SerializedName("currentBalanceDays")
    val currentBalanceDays: Double? = 0.0,

    @SerializedName("leaveType")
    val leaveType: LeaveTypeDetails?,

    @SerializedName("employee")
    val employee: EmployeeIdWrapper? = null
)

data class LeaveTypeDetails(
    @SerializedName("leaveTypeId")
    val leaveTypeId: Int,

    @SerializedName("typeName")
    val typeName: String? = "Unknown",

    @SerializedName("description")
    val description: String? = null
)

data class EmployeeIdWrapper(
    @SerializedName("empId")
    val empId: Int
)