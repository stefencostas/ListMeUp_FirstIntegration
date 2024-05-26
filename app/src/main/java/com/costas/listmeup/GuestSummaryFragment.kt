package com.costas.listmeup

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.costas.listmeup.adapters.SummaryAdapter
import com.costas.listmeup.databinding.FragmentSummaryBinding
import com.costas.listmeup.models.ShoppingItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class GuestSummaryFragment : Fragment() {

    private var _binding: FragmentSummaryBinding? = null
    private val binding get() = _binding!!
    private var selectedCategory: String? = null
    private lateinit var summaryAdapter: SummaryAdapter
    private lateinit var shoppingItemList: MutableList<ShoppingItem>

    private lateinit var sharedPreferences: SharedPreferences
    private val SHARED_PREF_NAME = "list_me_up_shared_pref"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreferences = requireActivity().getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
        shoppingItemList = loadShoppingItems().toMutableList()

        shoppingItemList = mutableListOf()
        setupCategorySpinner()
        loadDefaultData() // Load default data for guest mode
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupCategorySpinner() {
        val categories = resources.getStringArray(R.array.category_filter)
        val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.categorySpinner.adapter = categoryAdapter

        binding.categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                selectedCategory = parent.getItemAtPosition(position).toString()
                updateSummaryList()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedCategory = null
                updateSummaryList()
            }
        }
    }

    private fun loadShoppingItems(): List<ShoppingItem> {
        val gson = Gson()
        val json = sharedPreferences.getString("shopping_items", "")
        val type = object : TypeToken<List<ShoppingItem>>() {}.type
        return gson.fromJson(json, type) ?: mutableListOf()
    }

    @SuppressLint("SetTextI18n")
    private fun updateSummaryList() {
        val binding = _binding ?: return // Ensures safety if _binding is null
        if (::shoppingItemList.isInitialized) {
            val acquiredItems = shoppingItemList.filter { it.acquired }
            val filteredItems = when (selectedCategory) {
                "All", null -> acquiredItems
                else -> acquiredItems.filter { it.category == selectedCategory }
            }

            val sumBudgetCost = filteredItems.sumOf { it.quantity * it.estimatedCost }
            val sortedItems = filteredItems.sortedWith(compareBy({ it.category }, { it.itemName }))
            val groupedItems = sortedItems.groupBy { it.category }

            // Initialize the adapter only if the list is not empty
            summaryAdapter = SummaryAdapter(requireContext(), groupedItems.values.flatten())
            binding.summaryListView.adapter = summaryAdapter

            binding.totalCostSumTextView.text = "Total budget cost: ₱$sumBudgetCost"
        }
    }

    private fun loadDefaultData() {
        val savedItems = loadShoppingItems()
        if (savedItems.isEmpty()) {
            // Populate with default data
        } else {
            shoppingItemList.addAll(savedItems)
            updateSummaryList()
        }
    }
}
