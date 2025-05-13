package com.intprog.eventmanager_gitbam.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.intprog.eventmanager_gitbam.R
import com.intprog.eventmanager_gitbam.models.Event
import java.text.SimpleDateFormat
import java.util.Locale

class EventAdapter(private val onEventClick: (Event) -> Unit) :
    ListAdapter<Event, EventAdapter.EventViewHolder>(EventDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivEventImage: ImageView = itemView.findViewById(R.id.iv_event_image)
        private val chipCategory: Chip = itemView.findViewById(R.id.chip_category)
        private val tvEventName: TextView = itemView.findViewById(R.id.tv_event_name)
        private val tvLocation: TextView = itemView.findViewById(R.id.tv_location)
        private val tvDate: TextView = itemView.findViewById(R.id.tv_date)
        private val tvPrice: TextView = itemView.findViewById(R.id.tv_price)
        private val btnViewDetails: MaterialButton = itemView.findViewById(R.id.btn_view_details)

        fun bind(event: Event) {
            // Load event image
            Glide.with(itemView.context)
                .load(event.imageUrl ?: R.drawable.placeholder_image)
                .centerCrop()
                .into(ivEventImage)

            // Set category chip
            chipCategory.text = event.category
            chipCategory.setChipBackgroundColorResource(
                when (event.category.lowercase()) {
                    "conference" -> R.color.colorPrimary
                    "workshop" -> R.color.colorSecondary
                    "seminar" -> R.color.colorSuccess
                    "exhibition" -> R.color.colorInfo
                    "concert" -> R.color.colorWarning
                    "sports" -> R.color.colorError
                    else -> R.color.colorDefault
                }
            )

            // Set event details
            tvEventName.text = event.name
            tvLocation.text = event.location

            // Format date
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
            val date = inputFormat.parse(event.date)
            tvDate.text = date?.let { outputFormat.format(it) }

            // Set price
            tvPrice.text = if (event.price == "Free") "Free" else "$${event.price}"

            // Set click listener
            btnViewDetails.setOnClickListener {
                onEventClick(event)
            }
        }
    }

    private class EventDiffCallback : DiffUtil.ItemCallback<Event>() {
        override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem.eventId == newItem.eventId
        }

        override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem == newItem
        }
    }
} 