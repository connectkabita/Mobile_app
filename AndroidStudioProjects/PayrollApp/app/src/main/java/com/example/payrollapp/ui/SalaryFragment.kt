package com.example.payrollapp.ui

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.payrollapp.R
import com.example.payrollapp.adapter.SalaryAdapter // <--- CRITICAL: Check this path
import com.example.payrollapp.databinding.FragmentSalaryBinding
import com.example.payrollapp.network.RetrofitClient
import com.example.payrollapp.utils.SessionManager
import com.example.payrollapp.model.EmployeeRow
import com.example.payrollapp.model.SalarySummary
import kotlinx.coroutines.launch
import java.util.Locale

class SalaryFragment : Fragment(R.layout.fragment_salary) {
    private var _binding: FragmentSalaryBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private var currentRecord: EmployeeRow? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSalaryBinding.bind(view)
        sessionManager = SessionManager(requireContext())

        setupRecyclerView(emptyList())
        fetchSalaryData(2, 2026)

        binding.btnDownloadSlip.setOnClickListener {
            handleDownloadClick()
        }
    }

    private fun fetchSalaryData(month: Int, year: Int) {
        if (!sessionManager.isLoggedIn()) return handleInvalidSession()
        binding.progressBar.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getPayrollCommandCenter(month, year)
                if (isAdded) {
                    binding.progressBar.visibility = View.GONE
                    if (response.isSuccessful) {
                        val data = response.body()
                        val myRecord = data?.employeeRows?.firstOrNull()

                        if (myRecord != null) {
                            currentRecord = myRecord
                            updateUI(myRecord)
                            val history = listOf(
                                SalarySummary("February", 2026, myRecord.basicSalary,
                                    myRecord.totalAllowances, myRecord.totalDeductions,
                                    myRecord.netSalary, myRecord.status)
                            )
                            setupRecyclerView(history)
                        } else {
                            resetCardDisplay("No record found")
                        }
                    } else {
                        resetCardDisplay("Error: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                if (isAdded) {
                    binding.progressBar.visibility = View.GONE
                    resetCardDisplay("Network Error")
                }
            }
        }
    }

    private fun setupRecyclerView(list: List<SalarySummary>) {
        binding.rvSalaryHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSalaryHistory.adapter = SalaryAdapter(list)
    }

    private fun updateUI(record: EmployeeRow) {
        binding.tvCurrentNetSalary.text = String.format(Locale.US, "Rs. %,.2f", record.netSalary)
        binding.tvCurrentStatus.text = "Status: ${record.status.uppercase()}"
        binding.tvGrossSalaryValue?.text = String.format(Locale.US, "Rs. %,.2f", record.grossSalary)
        binding.tvBasicSalaryValue?.text = String.format(Locale.US, "Rs. %,.2f", record.basicSalary)
        binding.tvAllowancesValue?.text = String.format(Locale.US, "Rs. %,.2f", record.totalAllowances)
        binding.tvTaxValue?.text = String.format(Locale.US, "Rs. -%,.2f", record.totalTax)
        binding.tvDeductionsValue?.text = String.format(Locale.US, "Rs. -%,.2f", record.totalDeductions)
    }

    private fun resetCardDisplay(message: String) {
        binding.tvCurrentNetSalary.text = "Rs. 0.00"
        binding.tvCurrentStatus.text = message
    }

    private fun handleDownloadClick() {
        currentRecord?.let { downloadFile(it.payrollId) }
            ?: Toast.makeText(context, "No record to download", Toast.LENGTH_SHORT).show()
    }

    private fun downloadFile(id: Int) {
        val token = sessionManager.getAuthToken() ?: ""
        val downloadUrl = "http://10.0.2.2:8080/api/salary/download/$id"
        try {
            val request = DownloadManager.Request(Uri.parse(downloadUrl))
                .setTitle("Salary Slip - $id")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .addRequestHeader("Authorization", "Bearer $token")
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Payslip_$id.pdf")

            (requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(request)
            Toast.makeText(context, "Download Started", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleInvalidSession() {
        sessionManager.logout()
        startActivity(Intent(requireContext(), LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}