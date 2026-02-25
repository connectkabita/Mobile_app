package com.example.payrollapp.model

import com.google.gson.annotations.SerializedName

/**
 * The main response wrapper returned by api/payrolls/command-center
 * Based on your logs, the list is nested inside "employeeRows"
 */
data class CommandCenterResponse(
    @SerializedName("employeeRows")
    val employeeRows: List<EmployeeRow>
)

/**
 * Represents the actual payroll data row.
 * NOTE: Removed underscores to match the CamelCase keys seen in your server logs.
 */
data class EmployeeRow(
    val payrollId: Int,
    val basicSalary: Double,
    val totalAllowances: Double,
    val grossSalary: Double,
    val totalTax: Double,
    val totalDeductions: Double,
    val netSalary: Double,     // Matches "netSalary" in JSON logs
    val status: String,        // Matches "PAID"
    val payPeriodStart: String,
    val payPeriodEnd: String
)

/**
 * Used for the 'my-payslip' list endpoint
 */
data class SalarySummary(
    val month: String,
    val year: Int,
    val basicSalary: Double,
    val totalAllowances: Double,
    val totalDeductions: Double,
    val netSalary: Double,
    val status: String
)

data class PayslipDetails(
    val payslipId: String,
    val employeeName: String,
    val payDate: String
)