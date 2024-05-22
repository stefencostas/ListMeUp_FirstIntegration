package com.costas.listmeup

import android.annotation.SuppressLint
import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.costas.listmeup.adapters.SummaryAdapter
import com.costas.listmeup.constants.Constants.SHARED_PREFERENCES_NAME
import com.costas.listmeup.databinding.FragmentSummaryBinding
import com.costas.listmeup.models.ProfileDetails

@Suppress("DEPRECATION")
class SummaryFragment : Fragment() {

    private var _binding: FragmentSummaryBinding? = null
    private val binding get() = _binding!!
    private var selectedCategory: String? = null
    private lateinit var summaryAdapter: SummaryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCategorySpinner()
        updateSummaryList()
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

    @SuppressLint("SetTextI18n")
    private fun updateSummaryList() {
        val sharedPreferences = requireActivity().getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)
        val itemList = ProfileDetails.getListFromSharedPreferences(sharedPreferences)

        val acquiredItems = itemList.filter { it.acquired }
        val filteredItems = when (selectedCategory) {
            "All", null -> acquiredItems
            else -> acquiredItems.filter { it.category == selectedCategory }
        }

        val sortedItems = filteredItems.sortedWith(compareBy({ it.category }, { it.itemName }))
        val groupedItems = sortedItems.groupBy { it.category }

        summaryAdapter = SummaryAdapter(requireContext(), groupedItems.values.flatten())
        binding.summaryListView.adapter = summaryAdapter

        val sumBudgetCost = filteredItems.sumOf { it.quantity * it.estimatedCost }
        binding.totalCostSumTextView.text = "Budget Cost Sum : ₱${"%.2f".format(sumBudgetCost)}"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                requireActivity().onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
