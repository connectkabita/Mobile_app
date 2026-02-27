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

    private var lastAttendanceId: Long? = null
    private var isPunchedIn = false

    // COORDINATES MATCHING NEPAL OFFICE

    private val OFFICE_LAT = 28.9221
    private val OFFICE_LONG = 80.1742
    private val GEOFENCE_RADIUS_METERS = 15000000.0f

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
            if (isPunchedIn) performPunchOut() else performPunchIn()
        }
    }

    private fun checkLocationAndValidateRange() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                currentUserLocation = location
                calculateDistance(location)
            } else {
                requestFreshLocation()
            }
        }.addOnFailureListener {
            requestFreshLocation()
        }
    }

    private fun requestFreshLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val locationRequest = CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build()

            fusedLocationClient.getCurrentLocation(locationRequest, null).addOnSuccessListener { location ->
                location?.let {
                    currentUserLocation = it
                    calculateDistance(it)
                } ?: showStatus("GPS Signal Weak", Color.RED)
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
            b.tvDistanceDetail.text = if (distance < 1000) {
                "${String.format("%.0f", distance)}m from office"
            } else {
                "${String.format("%.2f", distance / 1000)} km from office"
            }

            if (distance <= GEOFENCE_RADIUS_METERS) {
                showStatus("In Range", Color.parseColor("#2E7D32"))
                b.btnPunchIn.isEnabled = true
                b.btnPunchIn.alpha = 1.0f
            } else {
                showStatus("Outside Range", Color.RED)
                b.btnPunchIn.isEnabled = false
                b.btnPunchIn.alpha = 0.5f
            }
        }
    }

    private fun performPunchIn() {
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val dateOnlyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val now = Date()

        val request = AttendanceRequest(
            empId = sessionManager.getEmployeeId(),
            latitude = currentUserLocation?.latitude ?: OFFICE_LAT,
            longitude = currentUserLocation?.longitude ?: OFFICE_LONG,
            status = "PRESENT",
            checkInTime = isoFormat.format(now),
            attendanceDate = dateOnlyFormat.format(now)
        )

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.postAttendance(request)
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Punched In!", Toast.LENGTH_SHORT).show()
                    fetchAttendanceHistory()
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Punch In Failed", e)
            }
        }
    }

    private fun performPunchOut() {
        val id = lastAttendanceId ?: return
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.checkOut(id)
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Punched Out!", Toast.LENGTH_SHORT).show()
                    fetchAttendanceHistory()
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Punch Out Failed", e)
            }
        }
    }

    private fun fetchAttendanceHistory() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getAttendanceHistory()
                if (response.isSuccessful && response.body() != null) {
                    val list = response.body()!!
                    attendanceList.clear()
                    attendanceList.addAll(list)
                    attendanceAdapter.notifyDataSetChanged()

                    if (list.isNotEmpty()) {
                        val latest = list[0]
                        isPunchedIn = latest.checkOutTime == null
                        lastAttendanceId = latest.attendanceId
                    } else {
                        isPunchedIn = false
                        lastAttendanceId = null
                    }
                    updatePunchButtonUI()
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "History fetch failed", e)
            } finally {
                _binding?.swipeRefreshLayout?.isRefreshing = false
            }
        }
    }

    private fun updatePunchButtonUI() {
        _binding?.let { b ->
            if (isPunchedIn) {
                b.btnPunchIn.text = "PUNCH OUT"
                b.btnPunchIn.backgroundTintList = ContextCompat.getColorStateList(requireContext(), android.R.color.holo_red_light)
            } else {
                b.btnPunchIn.text = "PUNCH IN"
                b.btnPunchIn.backgroundTintList = ContextCompat.getColorStateList(requireContext(), android.R.color.holo_green_light)
            }
        }
    }

    private fun setupRecyclerView() {
        attendanceAdapter = AttendanceAdapter(attendanceList)
        binding.rvAttendanceHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAttendanceHistory.adapter = attendanceAdapter
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            checkLocationAndValidateRange()
            fetchAttendanceHistory()
        }
    }

    private fun showStatus(text: String, color: Int) {
        binding.tvRangeStatus.text = "Status: $text"
        binding.tvRangeStatus.setTextColor(color)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}