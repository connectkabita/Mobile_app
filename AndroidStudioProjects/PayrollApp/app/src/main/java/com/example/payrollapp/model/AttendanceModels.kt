package com.example.payrollapp.model

import com.google.gson.annotations.SerializedName

/**
 * 1. Data sent to the server when punching (Clock In/Out)
 * Updated to match AttendanceFragment.kt usage
 */
data class AttendanceRequest(
    val empId: Int, // Fixed: Changed from employeeId to empId
    val latitude: Double,
    val longitude: Double,
    val status: String, // Fixed: Changed from type to status
    val checkInTime: String, // Fixed: Changed from timestamp to checkInTime
    val attendanceDate: String // Added to fix "No value passed for parameter" error
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
 * Updated to match AttendanceAdapter.kt usage
 */
data class AttendanceLog(
    val attendanceId: Long?, // Fixed: Changed to Long to fix Type Mismatch error
    val attendanceDate: String, // Fixed: Changed from date to attendanceDate
    val checkInTime: String?, // Fixed: Changed from clockIn to checkInTime
    val checkOutTime: String?, // Fixed: Changed from clockOut to checkOutTime
    val totalHours: Double?,
    val status: String // e.g., "PRESENT", "LATE", "ABSENT"
)

/**
 * 4. Wrapper for the history list response
 */
data class AttendanceHistoryResponse(
    val logs: List<AttendanceLog>
)