package com.costas.listmeup

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.costas.listmeup.adapters.ProfileDetailsAdapter
import com.costas.listmeup.models.ProfileDetails
import com.costas.listmeup.databinding.ActivityDashboardBinding

@Suppress("DEPRECATION")
class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var profileDetailsList: MutableList<ProfileDetails>
    private lateinit var adapter: ProfileDetailsAdapter


    companion object {
        private const val SHARED_PREFERENCES_NAME = "Settings"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val currentUsername = intent.getStringExtra("username")?: ""

        binding.userNameTextView.text = getString(R.string.current_username, currentUsername)

        setupRecyclerView()
        setupListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        saveItemListToSharedPreferences()
        Log.d("MainActivity", "onDestroy called")
    }

    private fun setupRecyclerView() {
        profileDetailsList = mutableListOf()
        adapter = ProfileDetailsAdapter(profileDetailsList)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        adapter.setOnItemClickListener(object : ProfileDetailsAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                showUpdateDialog(position)
            }

            override fun onItemLongClick(position: Int) {
                showRemoveItemConfirmationDialog(position)
            }
        })

        retrieveProfileDetails()
    }

    private fun setupListeners() {
        binding.add.setOnClickListener { showAddItemDialog() }
        binding.saveChangesButton.setOnClickListener {
            saveItemListToSharedPreferences()
            showToast("Changes saved!")
        }
        binding.summaryButton.setOnClickListener { navigateToSummaryActivity() }


    }

    @SuppressLint("NotifyDataSetChanged")
    private fun retrieveProfileDetails() {
        val sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)
        val itemList = ProfileDetails.getListFromSharedPreferences(sharedPreferences)
        profileDetailsList.addAll(itemList)
        adapter.notifyDataSetChanged()
    }

    @SuppressLint("SetTextI18n")
    private fun showAddItemDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_update_item, null)
        dialogBuilder.setView(dialogView)

        val etName = dialogView.findViewById<EditText>(R.id.etName)
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinnerCategory)
        val etQuantity = dialogView.findViewById<EditText>(R.id.etQuantity)
        val etEstimatedCost = dialogView.findViewById<EditText>(R.id.etEstimatedCost)

        // Populate the Spinner with categories
        val categories = resources.getStringArray(R.array.categories)
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = spinnerAdapter

        dialogBuilder.setTitle("Add Item")
        dialogBuilder.setPositiveButton("Add") { _, _ ->
            val itemName = etName.text.toString().trim()
            val category = spinnerCategory.selectedItem.toString()
            val quantity = etQuantity.text.toString().toIntOrNull() ?: 1
            val estimatedCost = etEstimatedCost.text.toString().toDoubleOrNull() ?: 0.00

            if (itemName.isNotEmpty() && quantity > 0 && estimatedCost >= 0) {
                val newItem = ProfileDetails(
                    itemName = itemName,
                    category = category,
                    acquired = false,
                    quantity = quantity,
                    estimatedCost = estimatedCost,
                )
                profileDetailsList.add(0, newItem)
                adapter.notifyItemInserted(0)
                saveItemListToSharedPreferences()
                showToast("Item added successfully")
            } else {
                showToast("Please fill in all fields correctly")
            }
        }

        dialogBuilder.setNegativeButton("Cancel", null)

        val alertDialog = dialogBuilder.create()
        alertDialog.show()

        // Set the default estimated cost
        etEstimatedCost.setText("0.00")
    }


    private fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, message, duration).show()
    }

    private fun showUpdateDialog(position: Int) {
        val currentProfileDetails = profileDetailsList[position]

        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_update_item, null)
        dialogBuilder.setView(dialogView)

        val etName = dialogView.findViewById<EditText>(R.id.etName)
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinnerCategory)
        val etQuantity = dialogView.findViewById<EditText>(R.id.etQuantity)
        val etEstimatedCost = dialogView.findViewById<EditText>(R.id.etEstimatedCost)

        // Populate the Spinner with categories
        val categories = resources.getStringArray(R.array.categories)
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = spinnerAdapter

        dialogBuilder.setTitle("Update Item")
        dialogBuilder.setPositiveButton("Update") { _, _ ->
            val itemName = etName.text.toString().trim()
            val category = spinnerCategory.selectedItem.toString()
            val quantity = etQuantity.text.toString().toIntOrNull() ?: currentProfileDetails.quantity
            val estimatedCost = etEstimatedCost.text.toString().toDoubleOrNull() ?: currentProfileDetails.estimatedCost

            if (itemName.isNotEmpty() && quantity > 0 && estimatedCost >= 0) {
                currentProfileDetails.itemName = itemName
                currentProfileDetails.category = category
                currentProfileDetails.quantity = quantity
                currentProfileDetails.estimatedCost = estimatedCost
                saveItemListToSharedPreferences()
                adapter.updateItem(position)
                showToast("Item updated successfully")
            } else {
                showToast("Please fill in all fields correctly")
            }
        }

        dialogBuilder.setNeutralButton("Remove") { _, _ ->
            profileDetailsList.removeAt(position)
            adapter.removeItem(position)
            saveItemListToSharedPreferences()
            showToast("Item removed successfully")
        }

        dialogBuilder.setNegativeButton("Cancel", null)

        val alertDialog = dialogBuilder.create()
        alertDialog.show()

        // Set the current values
        etName.setText(currentProfileDetails.itemName)
        spinnerCategory.setSelection(categories.indexOf(currentProfileDetails.category))
        etQuantity.setText(currentProfileDetails.quantity.toString())
        etEstimatedCost.setText(currentProfileDetails.estimatedCost.toString())
    }

    private fun navigateToSummaryActivity() {
        val intent = Intent(this, SummaryActivity::class.java)
        startActivity(intent)
    }

    private fun saveItemListToSharedPreferences() {
        val sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        ProfileDetails.saveListToSharedPreferences(editor, profileDetailsList)
        editor.apply()
        Log.d("MainActivity", "saveItemListToSharedPreferences called")
    }

    private fun showRemoveItemConfirmationDialog(position: Int) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Remove Item")
        dialogBuilder.setMessage("Are you sure you want to remove this item?")
        dialogBuilder.setPositiveButton("Yes") { _, _ ->
            removeItem(position)
        }
        dialogBuilder.setNegativeButton("No", null)
        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }

    private fun removeItem(position: Int) {
        profileDetailsList.removeAt(position)
        adapter.removeItem(position)
        saveItemListToSharedPreferences()
        showToast("Item removed successfully")
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MenuActivity::class.java)
        startActivity(intent)
        finish()
    }

}

