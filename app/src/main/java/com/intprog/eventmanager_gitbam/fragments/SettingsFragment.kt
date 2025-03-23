package com.intprog.eventmanager_gitbam.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.intprog.eventmanager_gitbam.LogoutActivity
import com.intprog.eventmanager_gitbam.R

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val buttonDeveloperPage = view.findViewById<Button>(R.id.btn_developer_page)
        val buttonLogout = view.findViewById<Button>(R.id.btn_logout)

        // Set click listeners
        buttonDeveloperPage.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, DevelopersFragment())
                .commit()
        }

        buttonLogout.setOnClickListener {
            val intent = Intent(requireContext(), LogoutActivity::class.java)
            startActivity(intent)
        }

    }
}