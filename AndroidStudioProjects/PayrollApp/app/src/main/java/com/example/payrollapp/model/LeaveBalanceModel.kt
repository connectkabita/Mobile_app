package com.example.payrollapp.model

import com.google.gson.annotations.SerializedName

data class LeaveBalanceModel(
    @SerializedName("leaveBalanceId") val id: Int,
    @SerializedName("year") val year: Int,
    @SerializedName("currentBalanceDays") val currentBalanceDays: Double,
    @SerializedName("leaveType") val leaveType: LeaveTypeModel?
)

data class LeaveTypeModel(
    @SerializedName("leaveTypeId") val id: Int,
    @SerializedName("typeName") val typeName: String?
)