package com.chronova.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.chronova.app.databinding.FragmentProjectsContainerBinding
import com.google.android.material.tabs.TabLayoutMediator

class ProjectsContainerFragment : Fragment() {

    private var _binding: FragmentProjectsContainerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProjectsContainerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPager()
    }

    private fun setupViewPager() {
        val adapter = ProjectsContainerPagerAdapter(requireActivity())
        binding.categoryViewPager.adapter = adapter

        TabLayoutMediator(binding.categoryTabLayout, binding.categoryViewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Projects"
                1 -> "Editors"
                else -> ""
            }
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private class ProjectsContainerPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> ProjectsPagerFragment()
                1 -> EditorsPagerFragment()
                else -> throw IllegalArgumentException("Invalid position: $position")
            }
        }
    }
}