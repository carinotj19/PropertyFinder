package com.example.propertyfinderdashboard

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.propertyfinderdashboard.fragments.Allmessages
import com.example.propertyfinderdashboard.fragments.Groupchats
import com.example.propertyfinderdashboard.fragments.Important

class FragmentStateAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        // Return the number of fragments
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        // Return the fragment for the given position
        return when (position) {
            0 -> Allmessages()
            1 -> Groupchats()
            2 -> Important()
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }
}
