package com.example.payrollapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.payrollapp.R
import com.example.payrollapp.databinding.ItemLeaveRecordBinding
import com.example.payrollapp.model.LeaveRequest

class LeaveAdapter(private val history: List<LeaveRequest>) :
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
        val context = holder.itemView.context

        // 1. Leave Type Name with Null Safety
        binding.tvLeaveType.text = record.leaveType?.typeName ?: "Leave Request"

        // 2. Date Range and Day Count
        val dayCount = record.totalDays ?: 0
        val startDate = record.startDate ?: "---"
        val endDate = record.endDate ?: "---"
        binding.tvLeaveDate.text = "$startDate to $endDate ($dayCount Days)"

        // 3. Status Text and Pills
        val statusText = record.status ?: "Pending"
        binding.tvStatusPill.text = statusText.uppercase()

        val (backgroundRes, textColorRes) = when (statusText.lowercase()) {
            "approved", "accepted" -> R.drawable.bg_pill_approved to android.R.color.white
            "rejected", "denied" -> R.drawable.bg_pill_rejected to android.R.color.white
            else -> R.drawable.bg_pill_pending to android.R.color.black
        }

        binding.tvStatusPill.setBackgroundResource(backgroundRes)
        binding.tvStatusPill.setTextColor(ContextCompat.getColor(context, textColorRes))

        // 4. Bind the Reason (NEW: Synchronized with your latest XML)
        val reasonText = record.reason ?: "No reason provided"
        binding.tvReason.text = "Reason: $reasonText"
    }

    override fun getItemCount() = history.size
}