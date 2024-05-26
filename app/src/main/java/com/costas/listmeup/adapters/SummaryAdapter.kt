package com.costas.listmeup.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.costas.listmeup.R
import com.costas.listmeup.models.ShoppingItem

class SummaryAdapter(context: Context, shoppingList: List<ShoppingItem>) :
    ArrayAdapter<ShoppingItem>(context, R.layout.item_summary, shoppingList) {

    private class ViewHolder {
        var categoryTextView: TextView? = null
        var itemNameTextView: TextView? = null
        var quantityTextView: TextView? = null
        var totalCostTextView: TextView? = null
    }

    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val viewHolder: ViewHolder

        if (convertView == null || convertView.tag !is ViewHolder) {
            viewHolder = ViewHolder()
            val inflater = LayoutInflater.from(context)
            convertView = inflater.inflate(R.layout.item_summary, parent, false)
            viewHolder.categoryTextView = convertView.findViewById(R.id.categoryTextView)
            viewHolder.itemNameTextView = convertView.findViewById(R.id.itemNameTextView)
            viewHolder.quantityTextView = convertView.findViewById(R.id.quantityTextView)
            viewHolder.totalCostTextView = convertView.findViewById(R.id.totalCostTextView)
            convertView.tag = viewHolder
        } else {
            viewHolder = convertView.tag as ViewHolder
        }

        val currentItem = getItem(position)
        val previousItem = if (position > 0) getItem(position - 1) else null

        // Show category if it's the first item or the category differs from the previous item
        if (previousItem == null || currentItem?.category != previousItem.category) {
            viewHolder.categoryTextView?.text = currentItem?.category
            viewHolder.categoryTextView?.visibility = View.VISIBLE
        } else {
            viewHolder.categoryTextView?.visibility = View.GONE
        }

        viewHolder.itemNameTextView?.text = currentItem?.itemName
        viewHolder.quantityTextView?.text = "Quantity: ${currentItem?.quantity}"

        // Calculate and set the total cost
        val totalCost = currentItem?.quantity?.times(currentItem.estimatedCost) ?: 0.0
        viewHolder.totalCostTextView?.text = "Total Budget Cost: ₱${"%.2f".format(totalCost)}"

        return convertView!!
    }
}
