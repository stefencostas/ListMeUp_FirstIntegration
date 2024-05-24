package com.costas.listmeup

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.costas.listmeup.adapters.ProfileDetailsAdapter
import com.costas.listmeup.databinding.FragmentDashboardBinding
import com.costas.listmeup.models.ProfileDetails
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var profileDetailsList: MutableList<ProfileDetails>
    private lateinit var adapter: ProfileDetailsAdapter
    private lateinit var myRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val database = FirebaseDatabase.getInstance("https://listmeup-android-default-rtdb.asia-southeast1.firebasedatabase.app")
        myRef = database.getReference("profile_details")
        profileDetailsList = mutableListOf()
        setupRecyclerView()
        setupListeners()
        retrieveProfileDetails()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        profileDetailsList = mutableListOf()
        adapter = ProfileDetailsAdapter(profileDetailsList)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
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
            saveItemListToFirebase()
            showToast("Changes saved!")
        }
    }

    private fun retrieveProfileDetails() {
        myRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                profileDetailsList.clear()
                for (data in snapshot.children) {
                    val id = data.key ?: ""
                    val itemName = data.child("itemName").getValue(String::class.java) ?: ""
                    val category = data.child("category").getValue(String::class.java) ?: ""
                    val acquired = data.child("acquired").getValue(Boolean::class.java) ?: false
                    val quantity = (data.child("quantity").getValue(Int::class.java) ?: 0)
                    val estimatedCost = (data.child("estimatedCost").getValue(Double::class.java) ?: 0.0)

                    val profileDetails = ProfileDetails(id, itemName, category, acquired, quantity, estimatedCost)
                    profileDetailsList.add(profileDetails)
                    Log.d("DashboardFragment", "Retrieved item: $itemName")
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("DashboardFragment", "Failed to read value.", error.toException())
            }
        })
    }


    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    private fun showAddItemDialog() {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_update_item, null)
        dialogBuilder.setView(dialogView)

        val etName = dialogView.findViewById<EditText>(R.id.etName)
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinnerCategory)
        val etQuantity = dialogView.findViewById<EditText>(R.id.etQuantity)
        val etEstimatedCost = dialogView.findViewById<EditText>(R.id.etEstimatedCost)

        // Populate the Spinner with categories
        val categories = resources.getStringArray(R.array.categories)
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = spinnerAdapter

        dialogBuilder.setTitle("Add Item")
        dialogBuilder.setPositiveButton("Add") { _, _ ->
            val itemName = etName.text.toString().trim()
            val category = spinnerCategory.selectedItem.toString()
            val quantity = etQuantity.text.toString().toIntOrNull() ?: 1
            val estimatedCost = etEstimatedCost.text.toString().toDoubleOrNull() ?: 0.00

            if (itemName.isNotEmpty() && quantity > 0 && estimatedCost >= 0.0) {
                val newItem = ProfileDetails(itemName = itemName, category = category, acquired = false, quantity = quantity, estimatedCost = estimatedCost)
                myRef.push().setValue(newItem)
                    .addOnCompleteListener(requireActivity()) { task ->
                        if (task.isSuccessful) {
                            Log.d("DashboardFragment", "Item added to the database")
                            Toast.makeText(requireContext(), "Item added!", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.e("DashboardFragment", "Failed to add item to the database", task.exception)
                            Toast.makeText(requireContext(), "Failed to add item to the database", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(requireContext(), "Invalid input, please try again.", Toast.LENGTH_SHORT).show()
            }
        }

        dialogBuilder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }

    private fun showUpdateDialog(position: Int) {
        val currentProfileDetails = profileDetailsList[position]

        val dialogBuilder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_update_item, null)
        dialogBuilder.setView(dialogView)

        val etName = dialogView.findViewById<EditText>(R.id.etName)
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinnerCategory)
        val etQuantity = dialogView.findViewById<EditText>(R.id.etQuantity)
        val etEstimatedCost = dialogView.findViewById<EditText>(R.id.etEstimatedCost)

        // Populate the Spinner with categories
        val categories = resources.getStringArray(R.array.categories)
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = spinnerAdapter

        // Set current item values to the EditTexts
        etName.setText(currentProfileDetails.itemName)
        val selectedCategory = categories.indexOf(currentProfileDetails.category)
        spinnerCategory.setSelection(selectedCategory)
        etQuantity.setText(currentProfileDetails.quantity.toString())
        etEstimatedCost.setText(currentProfileDetails.estimatedCost.toString())

        dialogBuilder.setTitle("Update Item")
        dialogBuilder.setPositiveButton("Update") { _, _ ->
            val itemName = etName.text.toString().trim()
            val category = spinnerCategory.selectedItem.toString()
            val quantity = etQuantity.text.toString().toIntOrNull() ?: 1
            val estimatedCost = etEstimatedCost.text.toString().toDoubleOrNull() ?: 0.00

            if (itemName.isNotEmpty() && quantity > 0 && estimatedCost >= 0.0) {
                val updatedItem = currentProfileDetails.copy(
                    itemName = itemName,
                    category = category,
                    quantity = quantity,
                    estimatedCost = estimatedCost
                )
                myRef?.child(currentProfileDetails.id)?.setValue(updatedItem)?.addOnSuccessListener {
                    showToast("Item updated!")
                }?.addOnFailureListener { e ->
                    showToast("Failed to update item: ${e.message}")
                }
            } else {
                showToast("Invalid input, please try again.")
            }
        }


        dialogBuilder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }

    private fun showRemoveItemConfirmationDialog(position: Int) { // This function is now defined
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Item")
            .setMessage("Are you sure you want to delete this item?")
            .setPositiveButton("Yes") { _, _ ->
                deleteItem(position)
            }
            .setNegativeButton("No") { _, _ ->
                // Do nothing
            }
            .show()
    }

    private fun saveItemListToFirebase() { // This function is now defined
        for (profileDetails in profileDetailsList) {
            myRef.child(profileDetails.id).setValue(profileDetails)
        }
    }

    private fun deleteItem(position: Int) {
        try {
            val selectedItem = profileDetailsList[position]
            val key = selectedItem.id
            myRef.child(key).removeValue()
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        // Item removed from the database successfully
                        Log.d("DashboardFragment", "Item deleted from the database")

                        requireActivity().runOnUiThread {
                            try {
                                // Remove the item from the list
                                profileDetailsList.removeAt(position)
                                // Notify the adapter after removing the item
                                adapter.notifyItemRemoved(position)
                            } catch (e: IndexOutOfBoundsException) {
                                Log.e("DashboardFragment", "IndexOutOfBoundsException occurred while removing item from list: $e")
                            }
                        }
                        showToast("Item deleted!")
                    } else {
                        // Failed to delete item from the database
                        Log.e("DashboardFragment", "Failed to delete item from the database", task.exception)
                        showToast("Failed to delete item from the database")
                    }
                }
        } catch (e: IndexOutOfBoundsException) {
            // Invalid position
            Log.e("DashboardFragment", "Invalid position: $position", e)
            showToast("Failed to delete item: Invalid position")
        }
    }


    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
