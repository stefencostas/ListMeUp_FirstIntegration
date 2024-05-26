package com.costas.listmeup

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.costas.listmeup.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.confirmButton.setOnClickListener {
            val email = binding.email.text.toString().trim()
            val password = binding.password.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(this, "Please enter valid credentials", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, retrieve user ID
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        // Navigate to DashboardActivity and pass user ID as extra
                        val intent = Intent(this, DashboardActivity::class.java)
                        intent.putExtra("userId", userId)
                        startActivity(intent)
                        finish()
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    val errorMessage = task.exception?.localizedMessage ?: "Unknown error"
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // Incorrect password
                        Toast.makeText(baseContext, "Authentication failed: Incorrect password", Toast.LENGTH_SHORT).show()
                    } else {
                        // Other authentication failures
                        Toast.makeText(baseContext, "Authentication failed: $errorMessage", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }
}
