package com.intprog.eventmanager_gitbam.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.intprog.eventmanager_gitbam.R
import com.intprog.eventmanager_gitbam.adapters.DeveloperAdapter
import com.intprog.eventmanager_gitbam.models.Developer

class DevelopersFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DeveloperAdapter

    // Team member data that matches the React version
    private val teamMembers = listOf(
        Developer(
            name = "Kent Albores",
            role = "Backend Developer",
            description = "Code is poetry. Security is my game.",
            expertise = listOf("Node.js", "Database Design", "API Security", "Authentication"),
            contributions = "Developed the secure user authentication system and RESTful API endpoints for event creation and management.",
            imageResId = R.drawable.kent
        ),
        Developer(
            name = "Frank Oliver Bentoy",
            role = "Web Developer",
            description = "Bringing designs to life, one line of code at a time.",
            expertise = listOf("React", "Material UI", "Responsive Design", "Frontend Development"),
            contributions = "Created the responsive user interface and implemented the event browsing and booking features.",
            imageResId = R.drawable.frank
        ),
        Developer(
            name = "Theus Gabriel Mendez",
            role = "Android Developer",
            description = "Life isn't about avoiding thunderstorms, it's about learning how to dance in the rain.",
            expertise = listOf("React Native", "Mobile UI/UX", "Cross-platform Development", "Push Notifications"),
            contributions = "Developed the companion mobile app for the event management platform, allowing users to receive notifications and manage bookings on the go.",
            imageResId = R.drawable.theus
        )
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_developer, container, false)

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.developers_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Set up adapter with click listener for detailed view
        adapter = DeveloperAdapter(teamMembers) { teamMember ->
            showDeveloperDetailDialog(teamMember)
        }

        recyclerView.adapter = adapter

        return view
    }

    private fun showDeveloperDetailDialog(teamMember: Developer) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_developer_detail, null)

        // Set developer details in dialog
        dialogView.findViewById<TextView>(R.id.developer_name).text = teamMember.name
        dialogView.findViewById<TextView>(R.id.developer_role).text = teamMember.role
        dialogView.findViewById<TextView>(R.id.developer_quote).text = "\"${teamMember.description}\""
        dialogView.findViewById<TextView>(R.id.developer_contributions).text = teamMember.contributions
        dialogView.findViewById<ImageView>(R.id.developer_image).setImageResource(teamMember.imageResId)

        // Add expertise chips
        val chipGroup = dialogView.findViewById<ChipGroup>(R.id.developer_expertise_chips)
        teamMember.expertise.forEach { skill ->
            val chip = Chip(requireContext()).apply {
                text = skill
                isCheckable = false
                setChipBackgroundColorResource(R.color.colorPrimaryLight)
                setTextColor(resources.getColor(R.color.white, null))
            }
            chipGroup.addView(chip)
        }

        // Create and show dialog
        AlertDialog.Builder(requireContext(), R.style.RoundedDialogStyle)
            .setView(dialogView)
            .setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }
}