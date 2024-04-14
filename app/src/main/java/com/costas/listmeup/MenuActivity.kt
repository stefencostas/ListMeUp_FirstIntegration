package com.costas.listmeup

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        findViewById<Button>(R.id.listNowButton).setOnClickListener {
            startDashboardActivity()
        }
    }

    private fun startDashboardActivity() {
        val intent = Intent(this, DashboardActivity::class.java)
        startActivity(intent)
        finish()
    }
}