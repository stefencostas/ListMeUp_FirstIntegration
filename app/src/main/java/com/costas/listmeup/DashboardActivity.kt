package com.costas.listmeup

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.costas.listmeup.databinding.ActivityDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var myRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Load the default fragment (Dashboard)
        if (savedInstanceState == null) {
            val userId = auth.currentUser?.uid
            if (userId != null) {
                myRef = FirebaseDatabase.getInstance("https://listmeup-android-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("profile_details/$userId")
                val fragment = DashboardFragment.newInstance(userId)
                loadFragment(fragment)
            }
        }

        // Set up button click listeners
        binding.dashboardButton.setOnClickListener {
            val userId = auth.currentUser?.uid
            if (userId != null) {
                myRef = FirebaseDatabase.getInstance("https://listmeup-android-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("profile_details/$userId")
                val fragment = DashboardFragment.newInstance(userId)
                loadFragment(fragment)
            }
        }

        binding.summaryButton.setOnClickListener {
            loadFragment(SummaryFragment())
        }

        binding.profileButton.setOnClickListener {
            loadFragment(ProfileFragment())
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
        binding.profileButton.setBackgroundResource(R.drawable.button_background)

        // Highlight the active button
        when (fragment) {
            is DashboardFragment -> {
                updateActiveButton(R.id.dashboardButton)
                // Reset all buttons to the default background and highlight the active button
                binding.dashboardButton.setBackgroundResource(R.drawable.button_background_active)
            }
            is SummaryFragment -> updateActiveButton(R.id.summaryButton)
            is ProfileFragment -> updateActiveButton(R.id.profileButton)
        }
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