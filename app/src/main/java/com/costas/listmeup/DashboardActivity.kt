package com.costas.listmeup

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.costas.listmeup.ProfileDetailsAdapter
import com.costas.listmeup.databinding.ActivityMainBinding
import com.costas.listmeup.ProfileDetails
import com.costas.listmeup.databinding.ActivityDashboardBinding

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
        setupRecyclerView()
        setupListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        saveItemListToSharedPreferences()
        Log.d("DashboardActivity", "onDestroy called")
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


    }

    private fun retrieveProfileDetails() {
        val sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)
        val itemList = ProfileDetails.getListFromSharedPreferences(sharedPreferences)
        profileDetailsList.addAll(itemList)
        adapter.notifyDataSetChanged()
    }

    private fun showAddItemDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_update_item, null)
        dialogBuilder.setView(dialogView)

        val etName = dialogView.findViewById<EditText>(R.id.etName)
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinnerCategory)
        val numberPickerQuantity = dialogView.findViewById<NumberPicker>(R.id.numberPickerQuantity)

        // Populate the Spinner with categories
        val categories = resources.getStringArray(R.array.categories)
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = spinnerAdapter

        // Set up the NumberPicker for quantity
        numberPickerQuantity.minValue = 0
        numberPickerQuantity.maxValue = 10 // Adjust the maximum value as needed

        dialogBuilder.setTitle("Add Item")
        dialogBuilder.setPositiveButton("Add") { _, _ ->
            val itemName = etName.text.toString().trim()
            val category = spinnerCategory.selectedItem.toString()
            val quantity = numberPickerQuantity.value

            if (itemName.isNotEmpty() && quantity > 0) {
                val newItem = ProfileDetails(
                    itemName = itemName,
                    category = category,
                    acquired = false,
                    quantity = quantity
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
    }


    private fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, message, duration).show()
    }

    private fun showUpdateDialog(position: Int) {
        var currentProfileDetails = profileDetailsList[position]

        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_update_item, null)
        dialogBuilder.setView(dialogView)

        val etName = dialogView.findViewById<EditText>(R.id.etName)
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinnerCategory)
        val numberPickerQuantity = dialogView.findViewById<NumberPicker>(R.id.numberPickerQuantity)

        etName.setText(currentProfileDetails.itemName)

        val categories = resources.getStringArray(R.array.categories)
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = spinnerAdapter

        val currentCategory = currentProfileDetails.category
        val categoryIndex = categories.indexOf(currentCategory)
        spinnerCategory.setSelection(categoryIndex)

        numberPickerQuantity.minValue = 0
        numberPickerQuantity.maxValue = 10 // Adjust the maximum value as needed
        numberPickerQuantity.value = currentProfileDetails.quantity

        dialogBuilder.setTitle("Update Item")
        dialogBuilder.setPositiveButton("Update") { _, _ ->
            currentProfileDetails.itemName = etName.text.toString()
            currentProfileDetails.category = spinnerCategory.selectedItem.toString()
            currentProfileDetails.quantity = numberPickerQuantity.value
            saveItemListToSharedPreferences()
            adapter.updateItem(position)
            showToast("Item updated successfully")
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
    }

    private fun saveItemListToSharedPreferences() {
        val sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        ProfileDetails.saveListToSharedPreferences(editor, profileDetailsList)
        editor.apply()
        Log.d("DashboardActivity", "saveItemListToSharedPreferences called")
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

    private fun removeCheckedItems() {
        val checkedItems = adapter.getCheckedItems()

        if (checkedItems.isNotEmpty()) {
            profileDetailsList.removeAll(checkedItems)
            adapter.notifyDataSetChanged()
            saveItemListToSharedPreferences()
            showToast("Checked items removed successfully")
        } else {
            showToast("No checked items to remove")
        }
    }
}
