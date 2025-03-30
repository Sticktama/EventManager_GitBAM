package com.intprog.eventmanager_gitbam.helper

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.intprog.eventmanager_gitbam.R
import com.intprog.eventmanager_gitbam.data.Organizer

class OrganizersCustomListViewAdapter(
    private val context: Context,
    private val organizerList: List<Organizer>,
    private val onClick: (Organizer) -> Unit,
    private val onLongClick: (Organizer) -> Unit
): BaseAdapter() {
    override fun getCount(): Int = organizerList.size

    override fun getItem(position: Int): Any = organizerList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.organizers_item_custom_list_view, parent, false)

        val profilePic = view.findViewById<ImageView>(R.id.imageview_organizer_pic)
        val fullname = view.findViewById<TextView>(R.id.tv_fullname)

        val organizer = organizerList[position]

        profilePic.setImageResource(organizer.photoRes)
        fullname.setText("${organizer.lastname}, ${organizer.firstname} ${organizer.middlename}")

        view.setOnClickListener{
            onClick(organizer)
        }

        view.setOnLongClickListener{
            onLongClick(organizer)
            true
        }
        return view
    }
}