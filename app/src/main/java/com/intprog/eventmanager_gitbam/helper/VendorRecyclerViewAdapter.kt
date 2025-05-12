package com.intprog.eventmanager_gitbam.helper

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.intprog.eventmanager_gitbam.R
import com.intprog.eventmanager_gitbam.data.Vendor

class VendorRecyclerViewAdapter(
    private var vendors: List<Vendor>,
    private val onItemClicked: (Vendor) -> Unit
) : RecyclerView.Adapter<VendorRecyclerViewAdapter.VendorViewHolder>() {

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
        
        // Set item click listener
        holder.itemView.setOnClickListener {
            onItemClicked(vendor)
        }
    }

    override fun getItemCount(): Int = vendors.size

    fun updateVendors(newVendors: List<Vendor>) {
        vendors = newVendors
        notifyDataSetChanged()
    }

    class VendorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val vendorName: TextView = itemView.findViewById(R.id.vendor_name)
        val vendorCategory: TextView = itemView.findViewById(R.id.vendor_category)
        val vendorLocation: TextView = itemView.findViewById(R.id.vendor_location)
        val vendorPrice: TextView = itemView.findViewById(R.id.vendor_price)
        val vendorRating: RatingBar = itemView.findViewById(R.id.vendor_rating)
        val vendorImage: ImageView = itemView.findViewById(R.id.vendor_image)
    }
} 