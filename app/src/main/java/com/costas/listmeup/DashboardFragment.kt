package com.costas.listmeup

import android.annotation.SuppressLint
import android.content.Context.MODE_PRIVATE
import android.os.Bundle
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
import com.costas.listmeup.constants.Constants.SHARED_PREFERENCES_NAME
import com.costas.listmeup.databinding.FragmentDashboardBinding
import com.costas.listmeup.models.ProfileDetails

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var profileDetailsList: MutableList<ProfileDetails>
    private lateinit var adapter: ProfileDetailsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
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
            saveItemListToSharedPreferences()
            showToast("Changes saved!")
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun retrieveProfileDetails() {
        val sharedPreferences = requireActivity().getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)
        val itemList = ProfileDetails.getListFromSharedPreferences(sharedPreferences)
        profileDetailsList.addAll(itemList)
        adapter.notifyDataSetChanged()
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
                profileDetailsList.add(newItem)
                adapter.notifyDataSetChanged()
                showToast("Item added!")
            } else {
                showToast("Invalid input, please try again.")
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

        // Set the current values
        etName.setText(currentProfileDetails.itemName)
        spinnerCategory.setSelection(categories.indexOf(currentProfileDetails.category))
        etQuantity.setText(currentProfileDetails.quantity.toString())
        etEstimatedCost.setText(currentProfileDetails.estimatedCost.toString())

        dialogBuilder.setTitle("Update Item")
        dialogBuilder.setPositiveButton("Update") { _, _ ->
            val updatedName = etName.text.toString().trim()
            val updatedCategory = spinnerCategory.selectedItem.toString()
            val updatedQuantity = etQuantity.text.toString().toIntOrNull() ?: currentProfileDetails.quantity
            val updatedEstimatedCost = etEstimatedCost.text.toString().toDoubleOrNull() ?: currentProfileDetails.estimatedCost

            if (updatedName.isNotEmpty() && updatedQuantity > 0 && updatedEstimatedCost >= 0.0) {
                currentProfileDetails.itemName = updatedName
                currentProfileDetails.category = updatedCategory
                currentProfileDetails.quantity = updatedQuantity
                currentProfileDetails.estimatedCost = updatedEstimatedCost
                saveItemListToSharedPreferences()
                adapter.notifyItemChanged(position)
                showToast("Item updated successfully")
            } else {
                showToast("Please fill in all fields correctly")
            }
        }

        dialogBuilder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }


    private fun showRemoveItemConfirmationDialog(position: Int) {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setTitle("Confirm Removal")
        dialogBuilder.setMessage("Are you sure you want to remove this item?")
        dialogBuilder.setPositiveButton("Remove") { _, _ ->
            profileDetailsList.removeAt(position)
            adapter.notifyItemRemoved(position)
            showToast("Item removed!")
        }
        dialogBuilder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }

    private fun saveItemListToSharedPreferences() {
        val sharedPreferences = requireActivity().getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        ProfileDetails.saveListToSharedPreferences(editor, profileDetailsList)
        editor.apply()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
