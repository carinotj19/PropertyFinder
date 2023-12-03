package com.example.propertyfinderdashboard.fragments

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.viewpager2.widget.ViewPager2
import com.example.propertyfinderdashboard.adapters.FragmentStateAdapter
import com.example.propertyfinderdashboard.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.textfield.TextInputEditText

class Messages : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var fragmentStateAdapter: FragmentStateAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_messages, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewPager2
        viewPager = view.findViewById(R.id.viewPager)
        // Create a list of fragments
        val fragments = listOf(Allmessages(), Groupchats(), Important())
        // Create a list of tab names
        val tabNames = listOf("All Messages", "Group Chats", "Important")
        // Initialize FragmentStateAdapter with fragments and tab names
        fragmentStateAdapter = FragmentStateAdapter(requireActivity(), fragments, tabNames)
        viewPager.adapter = fragmentStateAdapter

        // Initialize TabLayout
        tabLayout = view.findViewById(R.id.tab_layout)

        // Use TabLayoutMediator to attach TabLayout with ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = fragmentStateAdapter.getTabName(position)
        }.attach()

        val sendEmailButton: ImageButton = view.findViewById(R.id.floating_button)
        sendEmailButton.setOnClickListener {
            showEmailDialog()
        }

    }

    private fun showEmailDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_send_email, null)
        val emailEditText: TextInputEditText = dialogView.findViewById(R.id.emailEditText)
        val messageEditText: TextInputEditText = dialogView.findViewById(R.id.messageEditText)

        AlertDialog.Builder(requireContext())
            .setTitle("Send Email")
            .setView(dialogView)
            .setPositiveButton("Send") { dialog, _ ->

                val emailAddress = emailEditText.text.toString()

                Toast.makeText(requireContext(), "Message sent to $emailAddress", Toast.LENGTH_SHORT).show()

                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                // Dismiss the dialog
                dialog.dismiss()
            }
            .show()
    }
}