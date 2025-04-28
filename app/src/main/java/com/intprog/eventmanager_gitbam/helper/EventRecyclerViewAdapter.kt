package com.intprog.eventmanager_gitbam.helper

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.intprog.eventmanager_gitbam.R
import com.intprog.eventmanager_gitbam.data.Event

class EventRecyclerViewAdapter(
    private val listOfEvents: MutableList<Event>,
    private val onClick: (Event) -> Unit,
    private val onDeleteClick: (Int) -> Unit
): RecyclerView.Adapter<EventRecyclerViewAdapter.ItemViewHolder>() {

    // Track which items are selected
    private var isSelectionMode = false
    private val selectedItems = mutableSetOf<Int>()

    class ItemViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val eventImage: ImageView = view.findViewById(R.id.imageview_event_pic)
        val eventName: TextView = view.findViewById(R.id.textview_event_name)
        val eventDate: TextView = view.findViewById(R.id.textview_event_date_badge)
        val eventLocation: TextView = view.findViewById(R.id.textview_event_location)
        val eventOrganizer: TextView = view.findViewById(R.id.textview_event_organizer)
        val eventPrice: TextView = view.findViewById(R.id.textview_event_price)
        val categoryChip: Chip = view.findViewById(R.id.chip_category)
        val selectionCheckBox: CheckBox = view.findViewById(R.id.checkbox_select_event)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.event_item_recycler_view, parent, false)

        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ItemViewHolder,
        position: Int
    ) {
        val item: Event = listOfEvents[position]

        holder.eventImage.setImageResource(item.photo)
        holder.eventName.text = item.eventName
        holder.eventDate.text = item.eventDate
        holder.eventLocation.text = item.eventLocation
        holder.eventOrganizer.text = item.organizer
        holder.eventPrice.text = if (item.ticketPrice > 0) "₱${item.ticketPrice}" else "Free"
        holder.categoryChip.text = item.category

        // Set category chip color based on category
        val categoryColor = when (item.category.lowercase()) {
            "conference" -> R.color.blue
            "workshop" -> R.color.pink
            "seminar" -> R.color.purple
            "exhibition" -> R.color.orange
            "concert" -> R.color.yellow
            "sports" -> R.color.green
            "networking" -> R.color.indigo
            else -> R.color.gray
        }
        holder.categoryChip.setChipBackgroundColorResource(categoryColor)

        // Set checkbox visibility and state based on selection mode
        holder.selectionCheckBox.visibility = if (isSelectionMode) View.VISIBLE else View.GONE
        holder.selectionCheckBox.isChecked = selectedItems.contains(position)

        holder.itemView.setOnClickListener {
            if (isSelectionMode) {
                toggleSelection(position)
                holder.selectionCheckBox.isChecked = selectedItems.contains(position)
            } else {
                // Perform normal click action
                onClick(item)
            }
        }

        // Set up long click listener to enter selection mode
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
        
        // If no items are selected, exit selection mode
        if (selectedItems.isEmpty()) {
            exitSelectionMode()
        }
    }

    override fun getItemCount(): Int = listOfEvents.size

    // Methods to handle selection mode
    fun isInSelectionMode(): Boolean {
        return isSelectionMode
    }

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
        return selectedItems.toList().sortedDescending() // Sort to delete from end to beginning
    }
}