package com.costas.listmeup

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val confirmButton = findViewById<Button>(R.id.confirm_button)
        confirmButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}