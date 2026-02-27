package com.example.payrollapp.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.payrollapp.R
import com.example.payrollapp.model.AttendanceLog // Added Import
import java.text.SimpleDateFormat
import java.util.*

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

        // Match the field names from our updated AttendanceLog model
        holder.tvDate.text = log.attendanceDate
        holder.tvStatus.text = log.status

        // Format the ISO timestamps (2026-02-26T09:30:00) to readable time (09:30 AM)
        holder.tvTimeIn.text = "In: ${formatToTime(log.checkInTime)}"
        holder.tvTimeOut.text = "Out: ${formatToTime(log.checkOutTime)}"

        // Status-based coloring
        when (log.status) {
            "PRESENT" -> holder.tvStatus.setTextColor(Color.parseColor("#2E7D32")) // Green
            "LATE" -> holder.tvStatus.setTextColor(Color.parseColor("#F9A825"))    // Orange
            else -> holder.tvStatus.setTextColor(Color.parseColor("#C62828"))      // Red
        }
    }

    override fun getItemCount() = logs.size

    /**
     * Helper to convert API timestamp to readable time
     */
    private fun formatToTime(timestamp: String?): String {
        if (timestamp.isNullOrEmpty()) return "--:--"
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val date = parser.parse(timestamp)
            date?.let { formatter.format(it) } ?: "--:--"
        } catch (e: Exception) {
            // Check if it's already in a simple HH:mm:ss format
            timestamp ?: "--:--"
        }
    }

    fun updateData(newLogs: List<AttendanceLog>) {
        this.logs = newLogs
        notifyDataSetChanged()
    }
}