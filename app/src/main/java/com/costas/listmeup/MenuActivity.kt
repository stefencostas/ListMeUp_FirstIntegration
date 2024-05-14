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
            val intent = Intent(this, LoadingActivity::class.java)
            startActivity(intent)
            finish()
        }

        findViewById<Button>(R.id.profileButton).setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
            finish()
        }
    }


}