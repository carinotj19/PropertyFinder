package com.example.propertyfinderdashboard.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class FragmentStateAdapter(
    fragmentActivity: FragmentActivity,
    private val fragments: List<Fragment>,
    private val tabNames: List<String>
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

    fun getTabName(position: Int): String {
        return tabNames[position]
    }

}
