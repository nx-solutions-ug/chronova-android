package com.chronova.app.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.chronova.app.databinding.FragmentMainPagerBinding
import com.google.android.material.tabs.TabLayoutMediator

class MainPagerFragment : Fragment() {

    private var _binding: FragmentMainPagerBinding? = null
    private val binding get() = _binding!!
    private var isProUser: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isProUser = arguments?.getBoolean("is_pro_user", false) ?: false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainPagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPager()
    }

    private fun setupViewPager() {
        val adapter = MainPagerAdapter(requireActivity(), isProUser)
        binding.viewPager.adapter = adapter

        // Setup tabs based on PRO status
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = if (isProUser) {
                when (position) {
                    0 -> "Today"
                    1 -> "Last 7 Days"
                    2 -> "Last 30 Days"
                    3 -> "Last 3 Months"
                    4 -> "Last Year"
                    5 -> "All Time"
                    else -> ""
                }
            } else {
                when (position) {
                    0 -> "Today"
                    1 -> "Last 7 Days"
                    else -> ""
                }
            }
        }.attach()

        // Set default tab to "Last 7 Days"
        binding.viewPager.setCurrentItem(1, false)
    }

    fun updateProStatus(isPro: Boolean) {
        if (isProUser != isPro && isAdded && _binding != null) {
            isProUser = isPro
            // Recreate the adapter with new PRO status
            val adapter = MainPagerAdapter(requireActivity(), isProUser)
            binding.viewPager.adapter = adapter

            // Re-setup tabs
            TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
                tab.text = if (isProUser) {
                    when (position) {
                        0 -> "Today"
                        1 -> "Last 7 Days"
                        2 -> "Last 30 Days"
                        3 -> "Last 3 Months"
                        4 -> "Last Year"
                        5 -> "All Time"
                        else -> ""
                    }
                } else {
                    when (position) {
                        0 -> "Today"
                        1 -> "Last 7 Days"
                        else -> ""
                    }
                }
            }.attach()

            // Set default tab to "Last 7 Days"
            binding.viewPager.setCurrentItem(1, false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private class MainPagerAdapter(
        activity: FragmentActivity,
        private val isProUser: Boolean
    ) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = if (isProUser) 6 else 2

        override fun createFragment(position: Int): Fragment {
            return if (isProUser) {
                when (position) {
                    0 -> MainStatsFragment.newInstance("today")
                    1 -> MainStatsFragment.newInstance("last_7_days")
                    2 -> MainStatsFragment.newInstance("last_30_days")
                    3 -> MainStatsFragment.newInstance("last_3_months")
                    4 -> MainStatsFragment.newInstance("last_year")
                    5 -> MainStatsFragment.newInstance("all_time")
                    else -> throw IllegalArgumentException("Invalid position: $position")
                }
            } else {
                when (position) {
                    0 -> MainStatsFragment.newInstance("today")
                    1 -> MainStatsFragment.newInstance("last_7_days")
                    else -> throw IllegalArgumentException("Invalid position: $position")
                }
            }
        }
    }
}