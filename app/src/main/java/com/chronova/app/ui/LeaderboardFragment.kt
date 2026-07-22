package com.chronova.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.chronova.app.data.ChronovaRepository
import com.chronova.app.databinding.FragmentLeaderboardBinding
import com.google.android.material.chip.Chip
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class LeaderboardFragment : Fragment() {

    private var _binding: FragmentLeaderboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: ChronovaRepository
    private lateinit var leaderboardAdapter: LeaderboardAdapter
    private var isProUser: Boolean = false
    private var loadJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isProUser = arguments?.getBoolean("is_pro_user", false) ?: false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLeaderboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = ChronovaRepository(requireContext())

        setupRangeChips()
        setupRecyclerView()
        loadLeaderboard("last_7_days")
    }

    private fun setupRangeChips() {
        val ranges = if (isProUser) {
            listOf("last_7_days" to "7 Days", "last_30_days" to "30 Days", "last_90_days" to "90 Days")
        } else {
            listOf("last_7_days" to "7 Days")
        }

        ranges.forEachIndexed { index, (range, label) ->
            val chip = Chip(requireContext()).apply {
                text = label
                isCheckable = true
                isChecked = index == 0
                setOnClickListener {
                    if (!isProUser && range != "last_7_days") {
                        Toast.makeText(context, "Pro feature", Toast.LENGTH_SHORT).show()
                        isChecked = false
                        return@setOnClickListener
                    }
                    loadLeaderboard(range)
                }
            }
            binding.rangeChips.addView(chip)
        }
    }

    private fun setupRecyclerView() {
        val currentUserId = repository.getUserId()
        leaderboardAdapter = LeaderboardAdapter(mutableListOf(), currentUserId)
        binding.leaderboardRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.leaderboardRecyclerView.adapter = leaderboardAdapter
    }

    private fun loadLeaderboard(range: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.emptyStateText.visibility = View.GONE

        loadJob?.cancel()
        loadJob = viewLifecycleOwner.lifecycleScope.launch {
            repository.getLeaders(range, null, 1)
                .onSuccess { response ->
                    if (!isAdded || _binding == null) return@launch
                    binding.progressBar.visibility = View.GONE

                    val allEntries = mutableListOf<com.chronova.app.data.LeaderEntry>()
                    // Add current user at top if present and not in first page
                    response.currentUser?.let { currentUser ->
                        if (currentUser.rank > 50 || !response.data.any { it.leaderUser.id == currentUser.leaderUser.id }) {
                            allEntries.add(currentUser)
                        }
                    }
                    allEntries.addAll(response.data)

                    if (allEntries.isEmpty()) {
                        binding.emptyStateText.visibility = View.VISIBLE
                    } else {
                        binding.emptyStateText.visibility = View.GONE
                    }
                    leaderboardAdapter.updateLeaders(allEntries)
                }
                .onFailure { e ->
                    if (!isAdded || _binding == null) return@launch
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loadJob?.cancel()
        _binding = null
    }
}