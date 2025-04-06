package com.intprog.eventmanager_gitbam.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.intprog.eventmanager_gitbam.R
import com.intprog.eventmanager_gitbam.models.Developer

class DeveloperAdapter(
    private val teamMembers: List<Developer>,
    private val onItemClick: (Developer) -> Unit
) : RecyclerView.Adapter<DeveloperAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: CardView = view.findViewById(R.id.card_developer)
        val imageView: ImageView = view.findViewById(R.id.developer_image)
        val nameTextView: TextView = view.findViewById(R.id.developer_name)
        val roleTextView: TextView = view.findViewById(R.id.developer_role)
        val descriptionTextView: TextView = view.findViewById(R.id.developer_description)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_developer, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val teamMember = teamMembers[position]

        holder.nameTextView.text = teamMember.name
        holder.roleTextView.text = teamMember.role
        holder.descriptionTextView.text = teamMember.description
        holder.imageView.setImageResource(teamMember.imageResId)

        holder.cardView.setOnClickListener {
            onItemClick(teamMember)
        }
    }

    override fun getItemCount() = teamMembers.size
}