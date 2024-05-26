package com.costas.listmeup

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
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
import com.costas.listmeup.adapters.ShoppingListAdapter
import com.costas.listmeup.databinding.FragmentDashboardBinding
import com.costas.listmeup.models.ShoppingItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class GuestDashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var shoppingItemList: MutableList<ShoppingItem>
    private lateinit var adapter: ShoppingListAdapter

    private lateinit var sharedPreferences: SharedPreferences
    private val SHARED_PREF_NAME = "list_me_up_shared_pref"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreferences = requireActivity().getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
        shoppingItemList = loadShoppingItems().toMutableList()

        shoppingItemList = mutableListOf()
        setupRecyclerView()
        setupListeners()
        loadDefaultData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        adapter = ShoppingListAdapter(shoppingItemList)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        adapter.setOnItemClickListener(object : ShoppingListAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                showUpdateDialog(position)
            }

            override fun onItemLongClick(position: Int) {
                showRemoveItemConfirmationDialog(position)
            }
        })
    }

    private fun setupListeners() {
        binding.add.setOnClickListener { showAddItemDialog() }
        binding.saveChangesButton.setOnClickListener {
            saveShoppingItems(shoppingItemList)
            showToast("Changes saved!")
        }
    }

    private fun loadDefaultData() {
        val savedItems = loadShoppingItems()
        if (savedItems.isEmpty()) {
            // Populate with default data
        } else {
            shoppingItemList.addAll(savedItems)
            adapter.notifyDataSetChanged()
        }
    }

    private fun saveShoppingItems(items: List<ShoppingItem>) {
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(items)
        editor.putString("shopping_items", json)
        editor.apply()
    }

    private fun loadShoppingItems(): List<ShoppingItem> {
        val gson = Gson()
        val json = sharedPreferences.getString("shopping_items", "")
        val type = object : TypeToken<List<ShoppingItem>>() {}.type
        return gson.fromJson(json, type) ?: mutableListOf()
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
                val newItem = ShoppingItem(id = (shoppingItemList.size + 1).toString(), userId = "", itemName = itemName, category = category, acquired = false, quantity = quantity, estimatedCost = estimatedCost)
                shoppingItemList.add(newItem)
                adapter.notifyItemInserted(shoppingItemList.size - 1)
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
        val currentShoppingItem = shoppingItemList[position]

        val dialogBuilder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_update_item, null)
        dialogBuilder.setView(dialogView)

        val etName = dialogView.findViewById<EditText>(R.id.etName)
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinnerCategory)
        val etQuantity = dialogView.findViewById<EditText>(R.id.etQuantity)
        val etEstimatedCost = dialogView.findViewById<EditText>(R.id.etEstimatedCost)

        val categories = resources.getStringArray(R.array.categories)
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = spinnerAdapter

        etName.setText(currentShoppingItem.itemName)
        spinnerCategory.setSelection(categories.indexOf(currentShoppingItem.category))
        etQuantity.setText(currentShoppingItem.quantity.toString())
        etEstimatedCost.setText(currentShoppingItem.estimatedCost.toString())

        dialogBuilder.setTitle("Update Item")
        dialogBuilder.setPositiveButton("Update") { _, _ ->
            val itemName = etName.text.toString().trim()
            val category = spinnerCategory.selectedItem.toString()
            val quantity = etQuantity.text.toString().toIntOrNull() ?: 1
            val estimatedCost = etEstimatedCost.text.toString().toDoubleOrNull() ?: 0.00

            if (itemName.isNotEmpty() && quantity > 0 && estimatedCost >= 0.0) {
                val updatedItem = currentShoppingItem.copy(
                    itemName = itemName,
                    category = category,
                    quantity = quantity,
                    estimatedCost = estimatedCost
                )
                shoppingItemList[position] = updatedItem
                adapter.notifyItemChanged(position)
                showToast("Item updated!")
            } else {
                showToast("Invalid input, please try again.")
            }
        }

        dialogBuilder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }

    private fun showRemoveItemConfirmationDialog(position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Item")
            .setMessage("Are you sure you want to delete this item?")
            .setPositiveButton("Yes") { _, _ ->
                deleteItem(position)
            }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun deleteItem(position: Int) {
        try {
            shoppingItemList.removeAt(position)
            adapter.notifyItemRemoved(position)
            showToast("Item deleted!")
        } catch (e: IndexOutOfBoundsException) {
            Log.e("GuestDashboardFragment", "Invalid position: $position", e)
            showToast("Failed to delete item: Invalid position")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
