package com.intprog.eventmanager_gitbam

import android.app.Activity
import android.os.Bundle
import android.widget.ListView
import android.widget.Toast
import com.intprog.eventmanager_gitbam.data.Organizer
import com.intprog.eventmanager_gitbam.helper.OrganizersCustomListViewAdapter

class CustomOrganizerListActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple_organizer_list)

        val listView = findViewById<ListView>(R.id.listview)

        val organizerList = listOf(
            Organizer("Maria", "Clara", "Santos", R.drawable.ic_person),
            Organizer("James", "Alberto", "Reyes", R.drawable.ic_person),
            Organizer("Sophia", "Isabel", "Cruz", R.drawable.ic_person),
            Organizer("Luis", "Antonio", "Gonzales", R.drawable.ic_person),
            Organizer("Elena", "Victoria", "Dela Paz", R.drawable.ic_person),
            Organizer("Carlos", "Manuel", "Fernandez", R.drawable.ic_person),
            Organizer("Isabella", "Gabriela", "Lopez", R.drawable.ic_person),
            Organizer("Daniel", "Hector", "Ramirez", R.drawable.ic_person),
            Organizer("Valentina", "Beatriz", "Silva", R.drawable.ic_person),
            Organizer("Ricardo", "Javier", "Mendoza", R.drawable.ic_person)
        )

        val adapter = OrganizersCustomListViewAdapter(
            this,
            organizerList,
            onClick = { organizer ->
                Toast.makeText(this, "${organizer.firstname} was clicked", Toast.LENGTH_LONG).show()
            },
            onLongClick = { student ->
                Toast.makeText(this, "${student.lastname} was long clicked", Toast.LENGTH_LONG).show()
            }
        )
        listView.adapter = adapter
    }
}