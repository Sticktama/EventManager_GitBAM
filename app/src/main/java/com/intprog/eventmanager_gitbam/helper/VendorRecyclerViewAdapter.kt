package com.intprog.eventmanager_gitbam.helper

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.intprog.eventmanager_gitbam.R
import com.intprog.eventmanager_gitbam.data.Vendor

class VendorRecyclerViewAdapter(
    private val vendors: List<Vendor>,
    private val onItemClicked: (Vendor) -> Unit,
    private val onDeleteClicked: (Int) -> Unit
) : RecyclerView.Adapter<VendorRecyclerViewAdapter.VendorViewHolder>() {

    private var isSelectionMode = false
    private val selectedItems = mutableSetOf<Int>()

    fun isInSelectionMode(): Boolean = isSelectionMode

    fun enterSelectionMode() {
        if (!isSelectionMode) {
            isSelectionMode = true
            notifyDataSetChanged()
        }
    }

    fun exitSelectionMode() {
        if (isSelectionMode) {
            isSelectionMode = false
            selectedItems.clear()
            notifyDataSetChanged()
        }
    }
    
    fun getSelectedItems(): List<Int> {
        return selectedItems.toList()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VendorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.vendor_item_recycler_view, parent, false)
        return VendorViewHolder(view)
    }

    override fun onBindViewHolder(holder: VendorViewHolder, position: Int) {
        val vendor = vendors[position]
        
        // Set vendor details
        holder.vendorName.text = vendor.name
        holder.vendorCategory.text = vendor.category
        holder.vendorLocation.text = vendor.location
        holder.vendorPrice.text = "₱${vendor.price}"
        holder.vendorRating.rating = vendor.rating
        holder.vendorImage.setImageResource(vendor.photo)
        
        // Set checkbox visibility based on selection mode
        holder.selectionCheckBox.visibility = if (isSelectionMode) View.VISIBLE else View.GONE
        holder.selectionCheckBox.isChecked = selectedItems.contains(position)
        
        // Set item click listeners
        holder.itemView.setOnClickListener {
            if (isSelectionMode) {
                toggleSelection(position)
                holder.selectionCheckBox.isChecked = selectedItems.contains(position)
            } else {
                onItemClicked(vendor)
            }
        }
        
        holder.itemView.setOnLongClickListener {
            if (!isSelectionMode) {
                enterSelectionMode()
                toggleSelection(position)
                holder.selectionCheckBox.isChecked = true
            }
            true
        }
        
        holder.selectionCheckBox.setOnClickListener {
            toggleSelection(position)
        }
    }
    
    private fun toggleSelection(position: Int) {
        if (selectedItems.contains(position)) {
            selectedItems.remove(position)
        } else {
            selectedItems.add(position)
        }
        notifyItemChanged(position)
    }

    override fun getItemCount(): Int = vendors.size

    class VendorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val vendorName: TextView = itemView.findViewById(R.id.vendor_name)
        val vendorCategory: TextView = itemView.findViewById(R.id.vendor_category)
        val vendorLocation: TextView = itemView.findViewById(R.id.vendor_location)
        val vendorPrice: TextView = itemView.findViewById(R.id.vendor_price)
        val vendorRating: RatingBar = itemView.findViewById(R.id.vendor_rating)
        val vendorImage: ImageView = itemView.findViewById(R.id.vendor_image)
        val selectionCheckBox: CheckBox = itemView.findViewById(R.id.checkbox_select_vendor)
    }
} 