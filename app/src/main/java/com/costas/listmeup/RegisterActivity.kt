package com.costas.listmeup

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.costas.listmeup.databinding.ActivityRegisterBinding
import com.costas.listmeup.models.ShoppingItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        binding.confirmButton.setOnClickListener {
            val username = binding.username.text.toString().trim()
            val email = binding.email.text.toString().trim()
            val password = binding.password.text.toString().trim()
            val confirmPassword = binding.confirmPassword.text.toString().trim()

            if (isValidEmail(email) && isValidPassword(password, confirmPassword)) {
                registerUser(username, email, password)
            } else {
                Toast.makeText(this, "Please enter valid email and password", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidPassword(password: String, confirmPassword: String): Boolean {
        return password.length >= 6 && password == confirmPassword
    }

    private fun registerUser(username: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        val userId = it.uid
                        // Store additional user information (username) in Realtime Database
                        val userDataRef = database.reference.child("users").child(userId)
                        userDataRef.child("username").setValue(username)

                        // Create a reference to the user's shopping items node
                        val shoppingItemsRef = userDataRef.child("shoppingItems")

                        // Example: Add a shopping item
                        val newItemRef = shoppingItemsRef.push() // Generate unique ID for the item
                        val newItemId = newItemRef.key // Get the unique ID
                        val newItem = ShoppingItem(
                            id = newItemId ?: "",
                            userId = userId,
                            itemName = "ItemName",
                            category = "Category",
                            acquired = false,
                            quantity = 1,
                            estimatedCost = 0.0
                        )
                        newItemRef.setValue(newItem)

                        // Registration success, navigate to LoginActivity
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
                } else {
                    // If sign up fails, display a message to the user.
                    Toast.makeText(this, "Registration failed. ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}

