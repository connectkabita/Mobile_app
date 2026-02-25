package com.example.payrollapp.model

import com.google.gson.annotations.SerializedName

/**
 * 1. Data sent to the server when punching (Clock In/Out)
 */
data class AttendanceRequest(
    @SerializedName("empId")
    val employeeId: Int,
    val latitude: Double,
    val longitude: Double,
    val type: String, // Expected: "CLOCK_IN" or "CLOCK_OUT"
    val timestamp: String
)

/**
 * 2. Response from the server after a single punch action
 */
data class AttendanceResponse(
    val status: String,
    val message: String,
    val punchTime: String?,
    @SerializedName("success")
    val isSuccess: Boolean
)

/**
 * 3. Individual record for the Attendance History list
 */
data class AttendanceLog(
    val attendanceId: Int?,
    val date: String,
    val clockIn: String?,
    val clockOut: String?,
    val totalHours: Double?,
    val status: String // e.g., "PRESENT", "LATE", "ABSENT"
)

/**
 * 4. Wrapper for the history list response
 * Use this if your backend returns: { "logs": [...] }
 */
data class AttendanceHistoryResponse(
    val logs: List<AttendanceLog>
)