package com.costas.listmeup

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

@Suppress("DEPRECATION")
class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        auth = FirebaseAuth.getInstance()

        val currentPasswordEditText = view.findViewById<EditText>(R.id.current_password_edittext)
        val newPasswordEditText = view.findViewById<EditText>(R.id.new_password_edittext)
        val confirmPasswordEditText = view.findViewById<EditText>(R.id.confirm_password_edittext)
        val emailEditText = view.findViewById<EditText>(R.id.email_edittext)
        val updateSettingsButton = view.findViewById<Button>(R.id.update_settings_button)
        val logoutButton = view.findViewById<Button>(R.id.logout_button)

        updateSettingsButton.setOnClickListener {
            val currentPassword = currentPasswordEditText.text.toString().trim()
            val newPassword = newPasswordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()

            if (isValidPassword(newPassword, confirmPassword)) {
                updateSettings(currentPassword, newPassword, email)
            } else {
                Toast.makeText(requireContext(), "Invalid password", Toast.LENGTH_SHORT).show()
            }
        }

        logoutButton.setOnClickListener {
            logoutUser()
        }

        return view
    }

    private fun isValidPassword(password: String, confirmPassword: String): Boolean {
        return password.length >= 6 && password == confirmPassword
    }

    private fun updateSettings(currentPassword: String, newPassword: String, email: String) {
        val user = auth.currentUser
        if (user != null) {
            // Get user's credential
            val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)

            // Re-authenticate user
            user.reauthenticate(credential)
                .addOnCompleteListener { reAuthTask ->
                    if (reAuthTask.isSuccessful) {
                        // If reauthentication is successful, update password and/or email
                        user.updatePassword(newPassword)
                            .addOnCompleteListener { updatePasswordTask ->
                                if (updatePasswordTask.isSuccessful) {
                                    // Password updated successfully
                                    Toast.makeText(requireContext(), "Password updated successfully", Toast.LENGTH_SHORT).show()
                                } else {
                                    // Failed to update password
                                    Toast.makeText(requireContext(), "Failed to update password", Toast.LENGTH_SHORT).show()
                                }
                            }
                        // If email needs to be updated
                        if (email.isNotEmpty() && email != user.email) {
                            user.updateEmail(email)
                                .addOnCompleteListener { updateEmailTask ->
                                    if (updateEmailTask.isSuccessful) {
                                        // Email updated successfully
                                        Toast.makeText(requireContext(), "Email updated successfully", Toast.LENGTH_SHORT).show()
                                    } else {
                                        // Failed to update email
                                        Toast.makeText(requireContext(), "Failed to update email", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }
                    } else {
                        // Failed to reauthenticate user
                        Toast.makeText(requireContext(), "Authentication failed. Please check your current password", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            // User is null
            Toast.makeText(requireContext(), "User is null", Toast.LENGTH_SHORT).show()
        }
    }


    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut()
        // Navigate to login screen or main activity
        val intent = Intent(activity, LoginActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }
}
