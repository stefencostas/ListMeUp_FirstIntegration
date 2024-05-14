package com.costas.listmeup

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.costas.listmeup.databinding.DialogLoadingBinding

class LoadingActivity : AppCompatActivity() {
    private lateinit var binding: DialogLoadingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogLoadingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showLoadingDialog()

        Handler().postDelayed({
            dismissLoadingDialog()
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }, 1500)
    }

    private fun showLoadingDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setView(R.layout.dialog_loading)
        dialogBuilder.setCancelable(false)
        val dialog = dialogBuilder.create()
        dialog.show()
    }

    private fun dismissLoadingDialog() {
        val dialog = supportFragmentManager.findFragmentByTag("loading_dialog")
        if (dialog is AlertDialog) {
            dialog.dismiss()
        }
    }
}
