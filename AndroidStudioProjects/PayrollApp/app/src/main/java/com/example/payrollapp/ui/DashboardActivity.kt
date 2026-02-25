package com.example.payrollapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.example.payrollapp.R
import com.example.payrollapp.databinding.ActivityDashboardBinding
import com.example.payrollapp.utils.SessionManager

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize SessionManager
        sessionManager = SessionManager(this)

        // 1. Security Check: Redirect to login if token is missing
        if (!sessionManager.isLoggedIn()) {
            performLogout()
            return
        }

        // Setup Toolbar
        setSupportActionBar(binding.toolbar) // Ensure your XML has a Toolbar with ID 'toolbar'

        // 2. Set Default Fragment (Attendance) on first launch
        if (savedInstanceState == null) {
            replaceFragment(AttendanceFragment(), "Attendance")
            binding.bottomNavigation.selectedItemId = R.id.nav_attendance
        }

        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

            when (item.itemId) {
                R.id.nav_attendance -> {
                    if (currentFragment !is AttendanceFragment) {
                        replaceFragment(AttendanceFragment(), "Attendance History")
                    }
                    true
                }
                R.id.nav_leave -> {
                    if (currentFragment !is LeaveFragment) {
                        replaceFragment(LeaveFragment(), "Leave Management")
                    }
                    true
                }
                R.id.nav_salary -> {
                    if (currentFragment !is SalaryFragment) {
                        replaceFragment(SalaryFragment(), "My Salary")
                    }
                    true
                }
                else -> false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.dashboard_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                performLogout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun performLogout() {
        // Clear all SharedPreferences data
        sessionManager.logout()

        // Create intent and clear the backstack
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        startActivity(intent)
        finish() // Closes DashboardActivity
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
    }

    private fun replaceFragment(fragment: Fragment, title: String) {
        // Update Title (Safely check for Toolbar/ActionBar)
        supportActionBar?.title = title

        // Use Fragment Transaction with transition animations
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            replace(R.id.fragment_container, fragment)
        }
    }
}