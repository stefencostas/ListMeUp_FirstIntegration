package com.costas.listmeup

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val signInButton = findViewById<Button>(R.id.signin_Button)
        val signUpButton = findViewById<Button>(R.id.signup_Button)
        signInButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        signUpButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}