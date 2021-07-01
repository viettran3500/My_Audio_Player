package com.viet.myaudioplayer.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter


class ViewPagerAdapter(fm: FragmentActivity) : FragmentStateAdapter(fm) {

    private var fragments: MutableList<Fragment> = mutableListOf()
    var titles: MutableList<String> = mutableListOf()

    fun addFragments(fragment: Fragment, title: String) {
        fragments.add(fragment)
        titles.add(title)
    }

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}