package com.costas.listmeup

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.costas.listmeup.databinding.ActivityDashboardBinding

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private var activeButtonId: Int = R.id.dashboardButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load the default fragment (Dashboard)
        if (savedInstanceState == null) {
            loadFragment(DashboardFragment())
        }

        // Set up button click listeners
        binding.dashboardButton.setOnClickListener {
            loadFragment(DashboardFragment())
            updateActiveButton(R.id.dashboardButton)
        }

        binding.summaryButton.setOnClickListener {
            loadFragment(SummaryFragment())
            updateActiveButton(R.id.summaryButton)
        }

        binding.profileButton.setOnClickListener {
            loadFragment(ProfileFragment())
            updateActiveButton(R.id.profileButton)
        }

        // Set the initial active button appearance
        updateActiveButton(activeButtonId)
    }

    private fun loadFragment(fragment: Fragment) {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment != null && currentFragment::class == fragment::class) return

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun updateActiveButton(activeButtonId: Int) {
        // Reset all buttons to the default background
        binding.dashboardButton.setBackgroundResource(R.drawable.button_background)
        binding.summaryButton.setBackgroundResource(R.drawable.button_background)
        binding.profileButton.setBackgroundResource(R.drawable.button_background)

        // Highlight the active button
        when (activeButtonId) {
            R.id.dashboardButton -> binding.dashboardButton.setBackgroundResource(R.drawable.button_background_active)
            R.id.summaryButton -> binding.summaryButton.setBackgroundResource(R.drawable.button_background_active)
            R.id.profileButton -> binding.profileButton.setBackgroundResource(R.drawable.button_background_active)
        }
    }
}
