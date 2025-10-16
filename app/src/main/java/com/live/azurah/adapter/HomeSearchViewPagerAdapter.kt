package com.live.azurah.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.live.azurah.fragment.SearchPostUserFragment

class HomeSearchViewPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val tabCount: Int
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = tabCount

    override fun createFragment(position: Int): Fragment {
        return SearchPostUserFragment.newInstance(position)
    }


}