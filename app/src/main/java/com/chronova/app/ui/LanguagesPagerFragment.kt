package com.chronova.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.chronova.app.databinding.FragmentLanguagesPagerBinding
import com.google.android.material.tabs.TabLayoutMediator

class LanguagesPagerFragment : Fragment() {

    private var _binding: FragmentLanguagesPagerBinding? = null
    private val binding get() = _binding!!
    private var isProUser: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isProUser = arguments?.getBoolean(StatsRanges.ARG_IS_PRO_USER, false) ?: false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLanguagesPagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPager()
    }

    private fun setupViewPager() {
        val ranges = StatsRanges.forPlan(isProUser)
        val adapter = LanguagesPagerAdapter(requireActivity(), ranges.map { it.first })
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = ranges.getOrNull(position)?.second ?: ""
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private class LanguagesPagerAdapter(
        activity: FragmentActivity,
        private val ranges: List<String>
    ) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = ranges.size

        override fun createFragment(position: Int): Fragment {
            val range = ranges.getOrNull(position)
                ?: throw IllegalArgumentException("Invalid position: $position")
            return LanguagesStatsFragment.newInstance(range)
        }
    }
}
