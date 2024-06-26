package com.costas.listmeup.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.costas.listmeup.R
import com.costas.listmeup.models.ShoppingItem

@Suppress("DEPRECATION")
class ShoppingListAdapter(private val shoppingItemList: List<ShoppingItem>) :
    RecyclerView.Adapter<ShoppingListAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
        fun onItemLongClick(position: Int)
    }

    private var onItemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.onItemClickListener = listener
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemNameTextView: TextView = itemView.findViewById(R.id.itemNameTextView)
        val categoryTextView: TextView = itemView.findViewById(R.id.categoryTextView)
        val quantityTextView: TextView = itemView.findViewById(R.id.quantityTextView)
        val estimatedCostTextView: TextView = itemView.findViewById(R.id.estimatedCostTextView)
        val acquiredCheckBox: CheckBox = itemView.findViewById(R.id.acquiredCheckBox)

        init {
            // Set click listener for the entire itemView
            itemView.setOnClickListener {
                onItemClickListener?.onItemClick(adapterPosition)
            }

            // Set long click listener for the entire itemView
            itemView.setOnLongClickListener {
                onItemClickListener?.onItemLongClick(adapterPosition)
                true // Returning true indicates that the long click event is consumed
            }

            // Set a listener for CheckBox changes
            acquiredCheckBox.setOnCheckedChangeListener { _, isChecked ->
                // Update the acquired status in the corresponding ShoppingListItem item
                shoppingItemList[adapterPosition].acquired = isChecked
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_profile_details, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("StringFormatInvalid")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val shoppingItem = shoppingItemList[position]

        // Set data to views
        holder.itemNameTextView.text = holder.itemView.context.getString(
            R.string.itemName_label,
            shoppingItem.itemName
        )
        holder.categoryTextView.text = holder.itemView.context.getString(
            R.string.category_label,
            shoppingItem.category
        )
        holder.quantityTextView.text = holder.itemView.context.getString(
            R.string.quantity_label,
            shoppingItem.quantity.toString()
        )
        holder.estimatedCostTextView.text = holder.itemView.context.getString(
            R.string.estimated_cost_label,
            shoppingItem.estimatedCost.toString()
        )

        // Set the CheckBox state based on the acquired status
        holder.acquiredCheckBox.isChecked = shoppingItem.acquired
    }

    override fun getItemCount(): Int {
        return shoppingItemList.size
    }

    // Notify item changed and removed methods can be called directly on the adapter instance
    fun updateItem(position: Int) {
        notifyItemChanged(position)
    }

    fun deleteItem(position: Int) {
        notifyItemRemoved(position)
    }
}
