package com.intprog.eventmanager_gitbam.helper

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.intprog.eventmanager_gitbam.R
import com.intprog.eventmanager_gitbam.data.Event

class EventRecyclerViewAdapter(
    private var events: List<Event>,
    private val onItemClick: (Event) -> Unit
) : RecyclerView.Adapter<EventRecyclerViewAdapter.EventViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.event_item_recycler_view, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]

        // Load event image
        if (event.imageUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(event.imageUrl)
                .placeholder(R.drawable.events_default)
                .error(R.drawable.events_default)
                .into(holder.eventImage)
        } else {
            holder.eventImage.setImageResource(R.drawable.events_default)
        }

        holder.eventName.text = event.eventName
        holder.eventDate.text = event.eventDate
        holder.eventLocation.text = event.eventLocation
        holder.eventPrice.text = "₱${event.ticketPrice}"
        holder.eventCategory.text = event.category

        // Create a new rounded background with the category color
        val categoryColor = getCategoryColor(holder.itemView.context, event.category)
        val shapeDrawable = GradientDrawable()
        shapeDrawable.shape = GradientDrawable.RECTANGLE
        shapeDrawable.cornerRadius = 80f
        shapeDrawable.setColor(categoryColor)
        holder.eventCategory.background = shapeDrawable

        // Set up click listener
        holder.itemView.setOnClickListener {
            onItemClick(event)
        }
    }

    private fun getCategoryColor(context: Context, category: String): Int {
        return when (category.lowercase()) {
            "conference" -> ContextCompat.getColor(context, R.color.category_conference)
            "workshop" -> ContextCompat.getColor(context, R.color.category_workshop)
            "seminar" -> ContextCompat.getColor(context, R.color.category_seminar)
            "exhibition" -> ContextCompat.getColor(context, R.color.category_exhibition)
            "concert" -> ContextCompat.getColor(context, R.color.category_concert)
            "sports" -> ContextCompat.getColor(context, R.color.category_sports)
            "networking" -> ContextCompat.getColor(context, R.color.category_networking)
            else -> ContextCompat.getColor(context, R.color.category_default)
        }
    }

    override fun getItemCount() = events.size

    fun updateEvents(newEvents: List<Event>) {
        events = newEvents
        notifyDataSetChanged()
    }

    class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val eventImage: ImageView = view.findViewById(R.id.event_image)
        val eventName: TextView = view.findViewById(R.id.event_name)
        val eventDate: TextView = view.findViewById(R.id.event_date)
        val eventLocation: TextView = view.findViewById(R.id.event_location)
        val eventPrice: TextView = view.findViewById(R.id.event_price)
        val eventCategory: TextView = view.findViewById(R.id.event_category)
    }
}