package com.costas.listmeup

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.costas.listmeup.databinding.ActivityLoadingBinding

class LoadingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoadingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoadingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check if the user is in guest mode
        val isGuestMode = intent.getBooleanExtra("guestMode", false)

        // Determine the destination activity based on guest mode
        val destinationActivity = if (isGuestMode) {
            GuestDashboardActivity::class.java // If guest mode, go to GuestDashboardActivity
        } else {
            DashboardActivity::class.java // Else, go to DashboardActivity
        }

        // Start the destination activity after a delay
        binding.root.postDelayed({
            startActivity(Intent(this, destinationActivity))
            finish()
        }, 1000)
    }
}
