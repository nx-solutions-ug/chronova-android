package com.chronova.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.chronova.app.databinding.FragmentEditorsPagerBinding
import com.google.android.material.tabs.TabLayoutMediator

class EditorsPagerFragment : Fragment() {

    private var _binding: FragmentEditorsPagerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditorsPagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPager()
    }

    private fun setupViewPager() {
        val adapter = EditorsPagerAdapter(requireActivity())
        binding.viewPager.adapter = adapter

        // Setup tabs like Timeless app (Today, Last 7 Days, Last 30 Days)
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Today"
                1 -> "Last 7 Days"
                2 -> "Last 30 Days"
                else -> ""
            }
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private class EditorsPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> EditorsStatsFragment.newInstance("today")
                1 -> EditorsStatsFragment.newInstance("last_7_days") 
                2 -> EditorsStatsFragment.newInstance("last_30_days")
                else -> throw IllegalArgumentException("Invalid position: $position")
            }
        }
    }
}