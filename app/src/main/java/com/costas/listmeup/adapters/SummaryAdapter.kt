package com.costas.listmeup.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.costas.listmeup.R
import com.costas.listmeup.models.ProfileDetails

@Suppress("NAME_SHADOWING")
class SummaryAdapter(context: Context, items: List<ProfileDetails>) :
    ArrayAdapter<ProfileDetails>(context, R.layout.item_summary, items) {

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
        val currentCategory = if (position > 0) getItem(position - 1)?.category else ""
        val nextCategory = if (position < count - 1) getItem(position + 1)?.category else ""

        if (currentCategory != nextCategory || position == 0) {
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

            viewHolder.categoryTextView?.text = getItem(position)?.category
            viewHolder.categoryTextView?.visibility = View.VISIBLE
        } else {
            if (convertView == null) {
                viewHolder = ViewHolder()
                val inflater = LayoutInflater.from(context)
                convertView = inflater.inflate(R.layout.item_summary, parent, false)
                viewHolder.itemNameTextView = convertView.findViewById(R.id.itemNameTextView)
                viewHolder.quantityTextView = convertView.findViewById(R.id.quantityTextView)
                viewHolder.totalCostTextView = convertView.findViewById(R.id.totalCostTextView)
                convertView.tag = viewHolder
            } else {
                viewHolder = convertView.tag as ViewHolder
                viewHolder.categoryTextView?.visibility = View.GONE
            }
        }

        val item = getItem(position)
        viewHolder.itemNameTextView?.text = item?.itemName
        viewHolder.quantityTextView?.text = "Quantity: ${item?.quantity}"

        // Calculate and set the total cost
        val totalCost = if (item?.quantity != null) item.quantity * item.estimatedCost else 0.0
        viewHolder.totalCostTextView?.text = "Total Cost: ₱${"%.2f".format(totalCost)}"

        return convertView!!
    }
}