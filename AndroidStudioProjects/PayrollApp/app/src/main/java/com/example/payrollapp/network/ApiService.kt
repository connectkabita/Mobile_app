package com.example.payrollapp.network

import com.example.payrollapp.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    /**
     * AUTHENTICATION
     * No token needed for these endpoints.
     */
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/signup")
    suspend fun signup(@Body request: SignupRequest): Response<AuthResponse>

    /**
     * PAYROLL & SALARY
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
    suspend fun getLeaveHistory(): Response<List<LeaveRecord>>

    @POST("api/leave/apply")
    suspend fun applyForLeave(
        @Body request: LeaveRequest
    ): Response<Map<String, String>>

    @GET("api/leave/balances/my")
    suspend fun getMyLeaveBalances(): Response<List<LeaveBalanceModel>>

    /**
     * ATTENDANCE
     */
    // 1. Submit a Punch (CLOCK_IN or CLOCK_OUT)
    @POST("api/attendance/punch")
    suspend fun postAttendance(
        @Body data: AttendanceRequest
    ): Response<AttendanceResponse>

    // 2. Fetch history for the current authenticated user
    @GET("api/attendance/my-history")
    suspend fun getAttendanceHistory(): Response<List<AttendanceLog>>

    /**
     * DOWNLOAD PAYSLIP
     */
    @Streaming
    @GET("api/salary/download/{id}")
    suspend fun downloadPayslip(
        @Path("id") id: Int
    ): Response<Unit>
}