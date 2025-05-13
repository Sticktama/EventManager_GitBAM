package com.intprog.eventmanager_gitbam.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.intprog.eventmanager_gitbam.R
import com.intprog.eventmanager_gitbam.models.Event
import java.text.SimpleDateFormat
import java.util.Locale

class EventDetailsFragment : Fragment() {
    private lateinit var event: Event

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            event = it.getParcelable(ARG_EVENT) ?: throw IllegalArgumentException("Event argument is required")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_event_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        val ivEventImage: ImageView = view.findViewById(R.id.iv_event_image)
        val chipCategory: Chip = view.findViewById(R.id.chip_category)
        val tvEventName: TextView = view.findViewById(R.id.tv_event_name)
        val tvDescription: TextView = view.findViewById(R.id.tv_description)
        val tvLocation: TextView = view.findViewById(R.id.tv_location)
        val tvDate: TextView = view.findViewById(R.id.tv_date)
        val tvTime: TextView = view.findViewById(R.id.tv_time)
        val tvPrice: TextView = view.findViewById(R.id.tv_price)

        // Load event image
        Glide.with(this)
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
        tvDescription.text = event.description
        tvLocation.text = event.location

        // Format date
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
        val date = inputFormat.parse(event.date)
        tvDate.text = date?.let { outputFormat.format(it) }

        // Set time
        tvTime.text = event.time

        // Set price
        tvPrice.text = if (event.price == "Free") "Free" else "$${event.price}"
    }

    companion object {
        private const val ARG_EVENT = "event"

        fun newInstance(event: Event) = EventDetailsFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARG_EVENT, event)
            }
        }
    }
} 