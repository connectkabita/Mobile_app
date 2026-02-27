package com.example.payrollapp.network

import com.example.payrollapp.model.*
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Interface defining the API endpoints for the Payroll and Leave Management System.
 */
interface ApiService {

    /**
     * AUTHENTICATION
     */
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/signup")
    suspend fun signup(@Body request: SignupRequest): Response<AuthResponse>

    /**
     * FORGOT PASSWORD (OTP ONLY)
     * Matches the permitAll() path in SecurityConfig.java.
     * Logic: Backend sends an email with OTP; no database reset record is created.
     */
    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body email: Map<String, String>): Response<ResponseBody>

    /**
     * PAYROLL
     */
    @GET("api/payrolls/command-center")
    suspend fun getPayrollCommandCenter(
        @Query("month") month: Int,
        @Query("year") year: Int
    ): Response<CommandCenterResponse>

    /**
     * LEAVE MANAGEMENT
     */
    @GET("api/leave/my-history")
    suspend fun getMyLeaveHistory(): Response<List<LeaveRequest>>

    @POST("api/leave/apply")
    suspend fun applyLeave(@Body request: LeaveRequest): Response<Unit>

    @GET("api/leave/all")
    suspend fun getAllLeaves(): Response<List<LeaveRequest>>

    @GET("api/leave-balance/employee/{empId}")
    suspend fun getLeaveBalance(@Path("empId") empId: Int): Response<List<LeaveBalanceResponse>>

    @GET("api/leave-type")
    suspend fun getLeaveTypes(): Response<List<LeaveTypeDetails>>

    /**
     * ATTENDANCE
     */
    @POST("api/attendance/punch")
    suspend fun postAttendance(@Body data: AttendanceRequest): Response<AttendanceResponse>

    @GET("api/attendance/my-history")
    suspend fun getAttendanceHistory(): Response<List<AttendanceLog>>

    @GET("api/attendance/latest")
    suspend fun getLatestAttendance(): Response<AttendanceLog>

    @PUT("api/attendance/checkout/{id}")
    suspend fun checkOut(@Path("id") attendanceId: Long): Response<AttendanceLog>

    @GET("api/attendance/my-stats/{year}/{month}")
    suspend fun getMonthlyStats(
        @Path("year") year: Int,
        @Path("month") month: Int
    ): Response<Map<String, Any>>

    /**
     * SALARY & DOCUMENTS
     */
    @Streaming
    @GET("api/salary/download/{id}")
    suspend fun downloadPayslip(@Path("id") id: Int): Response<ResponseBody>
}