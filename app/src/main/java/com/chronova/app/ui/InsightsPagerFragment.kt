package com.chronova.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.chronova.app.databinding.FragmentInsightsPagerBinding
import com.google.android.material.tabs.TabLayoutMediator

class InsightsPagerFragment : Fragment() {

    private var _binding: FragmentInsightsPagerBinding? = null
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
        _binding = FragmentInsightsPagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isProUser) {
            setupViewPager()
        } else {
            binding.pagerContainer.visibility = View.GONE
            binding.lockedContainer.visibility = View.VISIBLE
        }
    }

    private fun setupViewPager() {
        val adapter = InsightsPagerAdapter(requireActivity())
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "AI Insights"
                1 -> "Focus"
                else -> ""
            }
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private class InsightsPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> AiInsightsFragment()
                1 -> FocusFragment()
                else -> throw IllegalArgumentException("Invalid position: $position")
            }
        }
    }
}