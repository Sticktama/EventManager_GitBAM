package com.intprog.eventmanager_gitbam.helper

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
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

    // Track which items are in delete mode
    private val itemsInDeleteMode = mutableSetOf<Int>()

    class ItemViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val eventImage: ImageView = view.findViewById(R.id.imageview_event_pic)
        val eventName: TextView = view.findViewById(R.id.textview_event_name)
        val eventDate: TextView = view.findViewById(R.id.textview_event_date_badge)
        val eventLocation: TextView = view.findViewById(R.id.textview_event_location)
        val eventOrganizer: TextView = view.findViewById(R.id.textview_event_organizer)
        val eventPrice: TextView = view.findViewById(R.id.textview_event_price)
        val categoryChip: Chip = view.findViewById(R.id.chip_category)
        val deleteButton: ImageButton = view.findViewById(R.id.button_delete)
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

        // Set delete button visibility based on delete mode
        if (position in itemsInDeleteMode) {
            holder.deleteButton.visibility = View.VISIBLE
        } else {
            holder.deleteButton.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            // If in delete mode, exit delete mode
            if (position in itemsInDeleteMode) {
                itemsInDeleteMode.remove(position)
                notifyItemChanged(position)
            } else {
                // Otherwise, perform normal click action
                onClick(item)
            }
        }

        // Set up long click listener to enter delete mode
        holder.itemView.setOnLongClickListener {
            if (position !in itemsInDeleteMode) {
                itemsInDeleteMode.add(position)
                notifyItemChanged(position)
            }
            true
        }

        holder.deleteButton.setOnClickListener {
            onDeleteClick(position)
            itemsInDeleteMode.remove(position)
        }
    }

    override fun getItemCount(): Int = listOfEvents.size

    // Method to exit delete mode for all items
    fun isInDeleteMode(): Boolean {
        return itemsInDeleteMode.isNotEmpty()
    }

    fun exitDeleteMode() {
        val affectedPositions = itemsInDeleteMode.toList()
        itemsInDeleteMode.clear()
        affectedPositions.forEach { position ->
            if (position < itemCount) {
                notifyItemChanged(position)
            }
        }
    }
}