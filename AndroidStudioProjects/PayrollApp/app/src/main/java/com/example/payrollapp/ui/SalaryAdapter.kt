package com.example.payrollapp.adapter // Matches the import in SalaryFragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.payrollapp.databinding.ItemSalaryBinding
import com.example.payrollapp.model.SalarySummary
import java.util.Locale

class SalaryAdapter(private val salaryList: List<SalarySummary>) :
    RecyclerView.Adapter<SalaryAdapter.SalaryViewHolder>() {

    class SalaryViewHolder(val binding: ItemSalaryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SalaryViewHolder {
        val binding = ItemSalaryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SalaryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SalaryViewHolder, position: Int) {
        val salary = salaryList[position]

        holder.binding.apply {
            // Displays month and year (e.g., February 2026)
            tvSalaryMonth.text = String.format(Locale.US, "%s %d", salary.month, salary.year)

            // Displays status in uppercase
            tvPaymentStatus.text = salary.status.uppercase()

            // Currency formatting with comma separators for thousands
            tvBasicAmount.text = String.format(Locale.US, "Basic: Rs. %,.2f", salary.basicSalary)
            tvDeductionAmount.text = String.format(Locale.US, "Ded: Rs. %,.2f", salary.totalDeductions)
            tvNetAmount.text = String.format(Locale.US, "Net: Rs. %,.2f", salary.netSalary)

            // Dynamic Status Coloring
            when (salary.status.uppercase()) {
                "PAID" -> {
                    tvPaymentStatus.setTextColor(android.graphics.Color.parseColor("#2E7D32")) // Green
                }
                "PENDING" -> {
                    tvPaymentStatus.setTextColor(android.graphics.Color.parseColor("#FFA000")) // Orange
                }
                else -> {
                    tvPaymentStatus.setTextColor(android.graphics.Color.GRAY)
                }
            }
        }
    }

    override fun getItemCount() = salaryList.size
}