package com.example.payrollapp.adapter // Matches the import in your Fragments

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.payrollapp.R
import com.example.payrollapp.databinding.ItemLeaveRecordBinding
import com.example.payrollapp.model.LeaveRecord

class LeaveAdapter(private val history: List<LeaveRecord>) :
    RecyclerView.Adapter<LeaveAdapter.LeaveViewHolder>() {

    class LeaveViewHolder(val binding: ItemLeaveRecordBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaveViewHolder {
        val binding = ItemLeaveRecordBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LeaveViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LeaveViewHolder, position: Int) {
        val record = history[position]
        val binding = holder.binding

        // 1. Display Leave Type (or Reason as fallback)
        // If your LeaveRecord model has a specific leaveType object, use that name
        binding.tvLeaveType.text = record.reason ?: "Leave Request"

        // 2. Formatting Date Range safely
        val start = record.startDate ?: "N/A"
        val end = record.endDate ?: "N/A"
        binding.tvLeaveDate.text = "$start to $end"

        // 3. Status Handling
        val statusText = record.status ?: "PENDING"
        binding.tvStatusPill.text = statusText.uppercase()

        // 4. Dynamic UI Styling
        // Using a safe 'when' block to map backend status to your drawable resources
        val backgroundRes = when (statusText.lowercase()) {
            "approved" -> R.drawable.bg_pill_approved
            "rejected" -> R.drawable.bg_pill_rejected
            else -> R.drawable.bg_pill_pending
        }

        binding.tvStatusPill.setBackgroundResource(backgroundRes)
    }

    override fun getItemCount() = history.size
}