package com.costas.listmeup

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.costas.listmeup.adapters.SummaryAdapter
import com.costas.listmeup.databinding.FragmentSummaryBinding
import com.costas.listmeup.models.ShoppingItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SummaryFragment : Fragment() {

    private var _binding: FragmentSummaryBinding? = null
    private val binding get() = _binding!!
    private var selectedCategory: String? = null
    private lateinit var summaryAdapter: SummaryAdapter
    private lateinit var myRef: DatabaseReference
    private lateinit var shoppingItemList: MutableList<ShoppingItem>
    private lateinit var auth: FirebaseAuth
    private var userId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        userId = currentUser?.uid

        if (userId != null) {
            val database = FirebaseDatabase.getInstance("https://listmeup-android-default-rtdb.asia-southeast1.firebasedatabase.app")
            myRef = database.getReference("profile_details/$userId/shopping_items")
            shoppingItemList = mutableListOf()
            setupCategorySpinner()
            loadShoppingItemsFromFirebase() // Load shopping items from Firebase
        } else {
            // Handle the case where userId is null (e.g., user is not logged in)
            Log.w("SummaryFragment", "User is not logged in")
        }
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

    private fun loadShoppingItemsFromFirebase() {
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                shoppingItemList.clear()
                for (itemSnapshot in dataSnapshot.children) {
                    val shoppingItem = itemSnapshot.getValue(ShoppingItem::class.java)
                    if (shoppingItem != null) {
                        shoppingItemList.add(shoppingItem)
                    }
                }
                updateSummaryList()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("SummaryFragment", "loadShoppingItemsFromFirebase:onCancelled", databaseError.toException())
            }
        })
    }
}
