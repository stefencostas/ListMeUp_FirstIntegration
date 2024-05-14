package com.costas.listmeup

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Spinner
import android.widget.TextView
import com.costas.listmeup.adapters.SummaryAdapter
import com.costas.listmeup.constants.Constants.SHARED_PREFERENCES_NAME
import com.costas.listmeup.models.ProfileDetails

class SummaryActivity : AppCompatActivity() {

    private lateinit var summaryListView: ListView
    private lateinit var summaryAdapter: SummaryAdapter
    private lateinit var categorySpinner: Spinner
    private var selectedCategory: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)

        title = "Acquired Items"

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        summaryListView = findViewById(R.id.summaryListView)
        categorySpinner = findViewById(R.id.categorySpinner)

        val categories = resources.getStringArray(R.array.category_filter)
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = categoryAdapter

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                selectedCategory = parent.getItemAtPosition(position).toString()
                updateSummaryList()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedCategory = null
                updateSummaryList()
            }
        }

        getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)
        updateSummaryList()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateSummaryList() {
        val sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)
        val itemList = ProfileDetails.getListFromSharedPreferences(sharedPreferences)

        val acquiredItems = itemList.filter { it.acquired }

        // Filter items by category if a category is selected
        val filteredItems = if (selectedCategory == "All" || selectedCategory.isNullOrEmpty()) {
            acquiredItems
        } else {
            acquiredItems.filter { it.category == selectedCategory }
        }

        // Sort items by category and then by item name
        val sortedItems = filteredItems.sortedWith(compareBy({ it.category }, { it.itemName }))

        // Group items by category
        val groupedItems = sortedItems.groupBy { it.category }

        // Set the category name in the category name TextView
        val categoryNameTextView = findViewById<TextView>(R.id.categoryNameTextView)
        categoryNameTextView.text = selectedCategory ?: "All"

        // Create the SummaryAdapter with the grouped items
        summaryAdapter = SummaryAdapter(this, groupedItems.values.flatten().toList())
        summaryListView.adapter = summaryAdapter

        // Calculate the total cost sum
        val totalCostSum = filteredItems.sumByDouble { it.quantity * it.estimatedCost }

        // Update the total cost sum TextView
        val totalCostSumTextView = findViewById<TextView>(R.id.totalCostSumTextView)
        totalCostSumTextView.text = "Total Cost Sum: ₱${"%.2f".format(totalCostSum)}"
    }
}