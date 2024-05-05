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
    }

    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val viewHolder: ViewHolder

        if (convertView == null) {
            viewHolder = ViewHolder()
            val inflater = LayoutInflater.from(context)
            convertView = inflater.inflate(R.layout.item_summary, parent, false)
            viewHolder.categoryTextView = convertView.findViewById(R.id.categoryTextView)
            viewHolder.itemNameTextView = convertView.findViewById(R.id.itemNameTextView)
            viewHolder.quantityTextView = convertView.findViewById(R.id.quantityTextView)
            convertView.tag = viewHolder
        } else {
            viewHolder = convertView.tag as ViewHolder
        }

        val item = getItem(position)

        // Check if the category is the same as the previous item
        if (position == 0 || item?.category != getItem(position - 1)?.category) {
            viewHolder.categoryTextView?.visibility = View.VISIBLE
            viewHolder.categoryTextView?.text = item?.category
        } else {
            viewHolder.categoryTextView?.visibility = View.GONE
        }

        viewHolder.itemNameTextView?.text = item?.itemName
        viewHolder.quantityTextView?.text = "Quantity: ${item?.quantity}"

        return convertView!!
    }
}