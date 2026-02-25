package com.example.payrollapp.model

import com.google.gson.annotations.SerializedName

data class LeaveRecord(
    @SerializedName("leaveId") val id: Int,
    @SerializedName("status") val status: String? = "Pending",
    @SerializedName("reason") val reason: String?,
    @SerializedName("startDate") val startDate: String?,
    @SerializedName("endDate") val endDate: String?,
    @SerializedName("totalDays") val totalDays: Int = 0,

    // Nested object - Now using the definition from LeaveBalanceModel.kt
    @SerializedName("leaveType") val leaveType: LeaveTypeModel?,

    @SerializedName("requestedAt") val requestedAt: String?,
    @SerializedName("approvedAt") val approvedAt: String?,
    @SerializedName("rejectionReason") val rejectionReason: String?
)