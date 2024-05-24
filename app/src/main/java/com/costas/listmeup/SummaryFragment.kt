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
import com.costas.listmeup.models.ProfileDetails
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
    private lateinit var profileDetailsList: MutableList<ProfileDetails>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val database = FirebaseDatabase.getInstance("https://listmeup-android-default-rtdb.asia-southeast1.firebasedatabase.app")
        myRef = database.getReference("profile_details")
        profileDetailsList = mutableListOf()
        setupCategorySpinner()
        loadProfileDetailsFromFirebase() // Load profile details from Firebase
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
        if (::profileDetailsList.isInitialized) {
            val acquiredItems = profileDetailsList.filter { it.acquired }
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

            binding.totalCostSumTextView.text = "Budget Cost Sum : ₱${"%.2f".format(sumBudgetCost)}"
        } else {
            Log.e("SummaryFragment", "profileDetailsList is not initialized")
            // Handle the case where profileDetailsList is not initialized
        }
    }


    private fun loadProfileDetailsFromFirebase() {
        val database = FirebaseDatabase.getInstance()
        val profileDetailsRef = database.getReference("profile_details")

        profileDetailsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                profileDetailsList.clear() // Clear existing data
                for (dataSnapshot in snapshot.children) {
                    val profileDetails = dataSnapshot.getValue(ProfileDetails::class.java)
                    if (profileDetails != null) {
                        profileDetailsList.add(profileDetails)
                    }
                }
                updateSummaryList()
                Log.d("SummaryFragment", "Data loaded successfully. Count: ${profileDetailsList.size}")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("SummaryFragment", "Database error: ${error.message}")
            }
        })
    }

}