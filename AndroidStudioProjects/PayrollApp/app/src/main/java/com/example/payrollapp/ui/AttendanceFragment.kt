package com.example.payrollapp.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.payrollapp.R
import com.example.payrollapp.adapter.AttendanceAdapter
import com.example.payrollapp.databinding.FragmentAttendanceBinding
import com.example.payrollapp.model.AttendanceLog
import com.example.payrollapp.model.AttendanceRequest
import com.example.payrollapp.network.RetrofitClient
import com.example.payrollapp.utils.SessionManager
import com.google.android.gms.location.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AttendanceFragment : Fragment(R.layout.fragment_attendance) {

    private var _binding: FragmentAttendanceBinding? = null
    private val binding get() = _binding!!

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var attendanceAdapter: AttendanceAdapter
    private lateinit var sessionManager: SessionManager
    private val attendanceList = mutableListOf<AttendanceLog>()
    private var currentUserLocation: Location? = null

    // SET FOR BAISI BICHAWA TESTING
    private val OFFICE_LAT = 28.6180
    private val OFFICE_LONG = 80.5560
    private val GEOFENCE_RADIUS_METERS = 5000.0f // 5km Radius

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) checkLocationAndValidateRange()
        else showStatus("Permission Denied", Color.RED)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAttendanceBinding.bind(view)

        sessionManager = SessionManager(requireContext())
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        setupRecyclerView()
        setupSwipeRefresh()

        checkLocationAndValidateRange()
        fetchAttendanceHistory()

        binding.btnPunchIn.setOnClickListener {
            performPunchIn()
        }
    }

    private fun checkLocationAndValidateRange() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (_binding != null && isAdded) {
                location?.let {
                    currentUserLocation = it
                    calculateDistance(it)
                } ?: requestFreshLocation()
            }
        }
    }

    private fun requestFreshLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    location?.let {
                        currentUserLocation = it
                        calculateDistance(it)
                    } ?: showStatus("GPS Weak", Color.RED)
                }
        }
    }

    private fun calculateDistance(location: Location) {
        val results = FloatArray(1)
        Location.distanceBetween(location.latitude, location.longitude, OFFICE_LAT, OFFICE_LONG, results)
        updateUIBasedOnDistance(results[0])
    }

    private fun updateUIBasedOnDistance(distance: Float) {
        _binding?.let { b ->
            val distanceText = if (distance >= 1000) {
                String.format(Locale.US, "Distance: %.2f km", distance / 1000)
            } else {
                "Distance: ${distance.toInt()}m"
            }

            b.tvDistanceDetail.text = distanceText

            if (distance <= GEOFENCE_RADIUS_METERS) {
                showStatus("Status: In Range", Color.parseColor("#2E7D32"))
                b.btnPunchIn.isEnabled = true
                b.btnPunchIn.alpha = 1.0f
                b.btnPunchIn.backgroundTintList = ContextCompat.getColorStateList(requireContext(), android.R.color.holo_green_light)
            } else {
                showStatus("Status: Outside Range", Color.RED)
                b.btnPunchIn.isEnabled = false
                b.btnPunchIn.alpha = 0.5f
                b.btnPunchIn.backgroundTintList = ContextCompat.getColorStateList(requireContext(), android.R.color.darker_gray)
            }
        }
    }

    private fun performPunchIn() {
        val empId = sessionManager.getEmployeeId()
        val location = currentUserLocation ?: return

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val request = AttendanceRequest(
            employeeId = empId,
            latitude = location.latitude,
            longitude = location.longitude,
            type = "CLOCK_IN",
            timestamp = sdf.format(Date())
        )

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.postAttendance(request)
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Punch Success!", Toast.LENGTH_SHORT).show()
                    fetchAttendanceHistory()
                }
            } catch (e: Exception) {
                Log.e("PUNCH_ERROR", e.message.toString())
            }
        }
    }

    private fun setupRecyclerView() {
        attendanceAdapter = AttendanceAdapter(attendanceList)
        binding.rvAttendanceHistory.apply {
            adapter = attendanceAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            checkLocationAndValidateRange()
            fetchAttendanceHistory()
        }
    }

    private fun fetchAttendanceHistory() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getAttendanceHistory()
                if (response.isSuccessful && response.body() != null) {
                    attendanceList.clear()
                    attendanceList.addAll(response.body()!!)
                    attendanceAdapter.notifyDataSetChanged()
                }
            } finally {
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun showStatus(text: String, color: Int) {
        binding.tvRangeStatus.text = text
        binding.tvRangeStatus.setTextColor(color)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}