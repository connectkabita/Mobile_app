package com.example.payrollapp.model

import com.google.gson.annotations.SerializedName

data class LeaveRequest(
    @SerializedName("employee")
    val employee: EmployeeIdWrapper,

    @SerializedName("leaveType")
    val leaveType: LeaveTypeIdWrapper,

    @SerializedName("startDate")
    val startDate: String,

    @SerializedName("endDate")
    val endDate: String,

    @SerializedName("reason")
    val reason: String
)

data class EmployeeIdWrapper(
    @SerializedName("empId")
    val empId: Int
)

data class LeaveTypeIdWrapper(
    @SerializedName("leaveTypeId")
    val leaveTypeId: Int
)