package com.costas.listmeup

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.costas.listmeup.databinding.ActivityGuestDashboardBinding

class GuestDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGuestDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuestDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load the default fragment (GuestDashboardFragment)
        if (savedInstanceState == null) {
            loadFragment(GuestDashboardFragment())
        }

        // Set up button click listeners
        binding.dashboardButton.setOnClickListener {
            loadFragment(GuestDashboardFragment())
        }

        binding.summaryButton.setOnClickListener {
            loadFragment(GuestSummaryFragment())
        }
    }

    private fun loadFragment(fragment: Fragment) {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment != null && currentFragment::class.java == fragment::class.java) return

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()

        // Reset all buttons to the default background
        binding.dashboardButton.setBackgroundResource(R.drawable.button_background)
        binding.summaryButton.setBackgroundResource(R.drawable.button_background)

        // Highlight the active button
        when (fragment) {
            is GuestDashboardFragment -> {
                updateActiveButton(R.id.dashboardButton)
            }
            is GuestSummaryFragment -> {
                updateActiveButton(R.id.summaryButton)
            }
        }
    }

    private fun updateActiveButton(activeButtonId: Int) {
        // Reset all buttons to the default background
        binding.dashboardButton.setBackgroundResource(R.drawable.button_background)
        binding.summaryButton.setBackgroundResource(R.drawable.button_background)

        // Highlight the active button
        when (activeButtonId) {
            R.id.dashboardButton -> binding.dashboardButton.setBackgroundResource(R.drawable.button_background_active)
            R.id.summaryButton -> binding.summaryButton.setBackgroundResource(R.drawable.button_background_active)
        }
    }
}
