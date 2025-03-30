package com.intprog.eventmanager_gitbam

import android.app.Activity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast

class SimpleOrganizerListActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple_organizer_list)

        val listView = findViewById<ListView>(R.id.listview)

        val organizerList = listOf(
            "Kent Albores", "Theus Mendez", "Frank Bentoy", "Hoshimachi Suisei",
            "Sakura Miko", "Diana", "John", "Alicia", "Mika", "Samuel",
            "Liam", "Noah", "Sophia", "Emma"
        )

        val arrayAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            organizerList
        )

        listView.adapter = arrayAdapter

        listView.setOnItemClickListener { _, _, position, _ ->
            Toast.makeText(
                this,
                "Item $position pressed with data : ${organizerList[position]} ",
                Toast.LENGTH_LONG
            ).show()
        }

        listView.setOnItemLongClickListener { _, _, position, _ ->
            Toast.makeText(
                this,
                "Item $position long pressed with data: ${organizerList[position]}",
                Toast.LENGTH_LONG
            ).show()
            true
        }
    }
}