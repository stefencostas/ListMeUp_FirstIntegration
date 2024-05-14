package com.costas.listmeup

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.widget.Button
import android.view.View

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val signInButton = findViewById<Button>(R.id.signin_Button)
        val signUpButton = findViewById<Button>(R.id.signup_Button)
        val continueAsGuestTextView = findViewById<View>(R.id.continue_as_guest_textview)

        signInButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        signUpButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        continueAsGuestTextView.setOnClickListener {
            startActivity(Intent(this, LoadingActivity::class.java))
        }
    }
}