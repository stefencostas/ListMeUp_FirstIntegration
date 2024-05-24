package com.costas.listmeup

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.costas.listmeup.databinding.ActivityMainBinding
import com.google.firebase.FirebaseApp

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase automatically using the Firebase SDK
        FirebaseApp.initializeApp(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signinButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        binding.signupButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        binding.continueAsGuestTextview.setOnClickListener {
            startActivity(Intent(this, LoadingActivity::class.java))
        }
    }
}