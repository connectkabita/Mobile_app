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
import com.example.payrollapp.adapter.LeaveAdapter // <--- CRITICAL: Check this path
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

        binding.rvLeaveHistory.layoutManager = LinearLayoutManager(requireContext())

        binding.btnApplyLeave.setOnClickListener {
            startActivity(Intent(requireContext(), ApplyLeaveActivity::class.java))
        }

        fetchLeaveHistory()
    }

    private fun fetchLeaveHistory() {
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(requireContext(), "Session expired.", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getLeaveHistory()
                if (isAdded) {
                    binding.progressBar.visibility = View.GONE
                    if (response.isSuccessful) {
                        val history = response.body() ?: emptyList()
                        binding.rvLeaveHistory.adapter = LeaveAdapter(history)
                    } else {
                        Toast.makeText(requireContext(), "Failed to load history", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                if (isAdded) {
                    binding.progressBar.visibility = View.GONE
                    Log.e("LEAVE_ERROR", "Exception: ${e.message}")
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}