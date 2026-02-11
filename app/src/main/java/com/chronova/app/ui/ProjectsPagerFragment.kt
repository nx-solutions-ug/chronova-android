package com.chronova.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.chronova.app.databinding.FragmentProjectsPagerBinding
import com.google.android.material.tabs.TabLayoutMediator

class ProjectsPagerFragment : Fragment() {

    private var _binding: FragmentProjectsPagerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProjectsPagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPager()
    }

    private fun setupViewPager() {
        val adapter = ProjectsPagerAdapter(requireActivity())
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

    private class ProjectsPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> ProjectsStatsFragment.newInstance("today")
                1 -> ProjectsStatsFragment.newInstance("last_7_days") 
                2 -> ProjectsStatsFragment.newInstance("last_30_days")
                else -> throw IllegalArgumentException("Invalid position: $position")
            }
        }
    }
}