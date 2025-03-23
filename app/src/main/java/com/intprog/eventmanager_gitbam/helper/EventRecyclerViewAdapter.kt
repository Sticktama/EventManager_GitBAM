package com.intprog.eventmanager_gitbam.helper

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.intprog.eventmanager_gitbam.R
import com.intprog.eventmanager_gitbam.data.Event

class EventRecyclerViewAdapter(
    private val listOfEvents: List<Event>,
    private val onClick: (Event) -> Unit
): RecyclerView.Adapter<EventRecyclerViewAdapter.ItemViewHolder>() {

    class ItemViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val eventImage = view.findViewById<ImageView>(R.id.imageview_event_pic)
        val eventId = view.findViewById<TextView>(R.id.textview_id)
        val eventName = view.findViewById<TextView>(R.id.textview_event_name)
        val eventDate = view.findViewById<TextView>(R.id.textview_event_date)
        val eventLocation = view.findViewById<TextView>(R.id.textview_event_location)
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
        holder.eventId.setText(item.id)
        holder.eventName.setText(item.eventName)
        holder.eventDate.setText(item.eventDate)
        holder.eventLocation.setText(item.eventLocation)

        holder.itemView.setOnClickListener {
            onClick(item)
        }
    }

    override fun getItemCount(): Int = listOfEvents.size
}