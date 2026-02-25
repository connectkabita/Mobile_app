package com.example.payrollapp.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val username: String,
    val password: String
)

data class AuthResponse(
    val success: Boolean,
    val message: String?,
    val token: String?,
    @SerializedName("userId") val userId: Int?,
    @SerializedName("empId") val empId: Int?,
    val username: String?,
    val role: String?
)

data class SignupRequest(
    val username: String,
    val email: String,
    val password: String,
    @SerializedName("roleId") val roleId: Int = 2,
    @SerializedName("firstName") val firstName: String,
    @SerializedName("lastName") val lastName: String = "",
    val address: String,
    @SerializedName("basicSalary") val basicSalary: Double = 0.0,
    val contact: String,
    val education: String,
    @SerializedName("employmentStatus") val employmentStatus: String = "ACTIVE",
    @SerializedName("joiningDate") val joiningDate: String = "2024-01-01",
    @SerializedName("isActive") val isActive: Boolean = true,
    val maritalStatus: String,
    @SerializedName("positionId") val positionId: Int = 1,
    @SerializedName("departmentId") val departmentId: Int = 1
)