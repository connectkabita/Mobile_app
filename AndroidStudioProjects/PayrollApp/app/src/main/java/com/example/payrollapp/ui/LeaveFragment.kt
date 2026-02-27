package com.example.payrollapp.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.payrollapp.R
import com.example.payrollapp.adapter.LeaveAdapter
import com.example.payrollapp.databinding.FragmentLeaveBinding
import com.example.payrollapp.network.RetrofitClient
import com.example.payrollapp.utils.SessionManager
import kotlinx.coroutines.launch

class LeaveFragment : Fragment(R.layout.fragment_leave) {
    private var _binding: FragmentLeaveBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLeaveBinding.bind(view)

        sessionManager = SessionManager(requireContext())
        RetrofitClient.init(requireContext())

        setupRecyclerView()

        // Refresh action
        binding.swipeRefreshLayout.setOnRefreshListener {
            loadAllData()
        }

        // Navigate to Apply screen
        binding.btnApplyLeave.setOnClickListener {
            startActivity(Intent(requireContext(), ApplyLeaveActivity::class.java))
        }

        loadAllData()
    }

    private fun setupRecyclerView() {
        binding.rvLeaveHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun loadAllData() {
        if (!isAdded) return
        binding.swipeRefreshLayout.isRefreshing = true

        val empId = sessionManager.getEmployeeId()
        if (empId <= 0) {
            binding.tvAvailableBalance.text = "Session Expired"
            binding.swipeRefreshLayout.isRefreshing = false
            return
        }

        // Fetch both datasets
        fetchLeaveBalance(empId)
        fetchLeaveHistory() // empId no longer needed for history with the new API
    }

    private fun fetchLeaveBalance(empId: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getLeaveBalance(empId)
                if (isAdded && response.isSuccessful) {
                    val balances = response.body() ?: emptyList()

                    // Sum up all current balance days from the list
                    val totalAvailable = balances.sumOf { it.currentBalanceDays ?: 0.0 }

                    binding.tvAvailableBalance.text = if (totalAvailable % 1 == 0.0) {
                        "${totalAvailable.toInt()} Days"
                    } else {
                        "$totalAvailable Days"
                    }
                }
            } catch (e: Exception) {
                Log.e("LEAVE_ERROR", "Balance Sync Failed: ${e.message}")
            } finally {
                checkLoadingComplete()
            }
        }
    }

    private fun fetchLeaveHistory() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // FIXED: Changed from getLeavesByEmployee(empId) to getMyLeaveHistory()
                val response = RetrofitClient.instance.getMyLeaveHistory()

                if (isAdded && response.isSuccessful) {
                    val history = response.body() ?: emptyList()

                    if (history.isEmpty()) {
                        binding.rvLeaveHistory.visibility = View.GONE
                        binding.tvNoHistory?.visibility = View.VISIBLE
                    } else {
                        binding.tvNoHistory?.visibility = View.GONE
                        binding.rvLeaveHistory.visibility = View.VISIBLE
                        // Show newest leave at the top using .reversed()
                        binding.rvLeaveHistory.adapter = LeaveAdapter(history.reversed())
                    }
                } else if (response.code() == 403) {
                    Log.e("LEAVE_ERROR", "Access Forbidden: Check JWT Token or Roles")
                }
            } catch (e: Exception) {
                Log.e("LEAVE_ERROR", "History Sync Failed: ${e.message}")
            } finally {
                checkLoadingComplete()
            }
        }
    }

    private fun checkLoadingComplete() {
        if (!isAdded) return
        binding.swipeRefreshLayout.isRefreshing = false
    }

    override fun onResume() {
        super.onResume()
        loadAllData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}