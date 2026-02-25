package com.example.payrollapp.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.payrollapp.R
import com.example.payrollapp.model.AttendanceLog

class AttendanceAdapter(private var logs: List<AttendanceLog>) :
    RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder>() {

    class AttendanceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvTimeIn: TextView = view.findViewById(R.id.tvTimeIn)
        val tvTimeOut: TextView = view.findViewById(R.id.tvTimeOut)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendanceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_attendance, parent, false)
        return AttendanceViewHolder(view)
    }

    override fun onBindViewHolder(holder: AttendanceViewHolder, position: Int) {
        val log = logs[position]
        holder.tvDate.text = log.date ?: "N/A"
        holder.tvStatus.text = log.status ?: "UNKNOWN"
        holder.tvTimeIn.text = "In: ${log.clockIn ?: "--:--"}"
        holder.tvTimeOut.text = "Out: ${log.clockOut ?: "--:--"}"

        // Using hex strings or System colors to avoid "Unresolved reference" errors
        if (log.status == "PRESENT") {
            holder.tvStatus.setTextColor(Color.parseColor("#2E7D32")) // Dark Green
        } else {
            holder.tvStatus.setTextColor(Color.parseColor("#C62828")) // Dark Red
        }
    }

    override fun getItemCount() = logs.size

    fun updateData(newLogs: List<AttendanceLog>) {
        this.logs = newLogs
        notifyDataSetChanged()
    }
}