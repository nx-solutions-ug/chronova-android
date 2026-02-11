package com.chronova.app.ui.main

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.chronova.app.data.ChronovaRepository
import com.chronova.app.databinding.FragmentMainStatsBinding
import com.chronova.app.ui.main.cards.CardsAdapter
import com.chronova.app.ui.main.cards.CardsList
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MainStatsFragment : Fragment() {

    private var _binding: FragmentMainStatsBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: ChronovaRepository
    private lateinit var cardsAdapter: CardsAdapter
    private var timeRange: String = "today"
    private var isProUser: Boolean = false
    private var loadDataJob: Job? = null

    companion object {
        private const val ARG_TIME_RANGE = "time_range"
        
        fun newInstance(timeRange: String): MainStatsFragment {
            return MainStatsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TIME_RANGE, timeRange)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        timeRange = arguments?.getString(ARG_TIME_RANGE) ?: "today"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        repository = ChronovaRepository(requireContext())
        setupRecyclerView()
        loadStatsData()
    }

    private fun setupRecyclerView() {
        cardsAdapter = CardsAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = cardsAdapter
        }
    }

    private fun loadStatsData() {
        // Cancel any existing job
        loadDataJob?.cancel()

        _binding?.let { binding ->
            binding.progressBar.visibility = View.VISIBLE

            loadDataJob = lifecycleScope.launch {
                try {
                    val result = repository.getStatsForRange(timeRange)
                    result.fold(
                        onSuccess = { stats ->
                            if (isAdded && _binding != null) {
                                _binding?.progressBar?.visibility = View.GONE

                                // Build cards list like Timeless
                                val cardsList = CardsList().apply {
                                    // Global summary card
                                    addGlobalSummary(stats.totalSeconds, timeRange)

                                    // Pie charts for different categories
                                    if (stats.languages.isNotEmpty()) {
                                        addPieChart("Top 5 Languages", stats.languages)
                                    }

                                    if (stats.projects.isNotEmpty()) {
                                        addPieChart("Top 5 Projects", stats.projects)
                                    }

                                    if (stats.editors.isNotEmpty()) {
                                        addPieChart("Top 5 Editors", stats.editors)
                                    }

                                    // Activity chart if available
                                    if (stats.dailyActivity.isNotEmpty()) {
                                        addLineChart("Activity", stats.dailyActivity)
                                    }
                                }

                                cardsAdapter.updateCards(cardsList)
                            }
                        },
                        onFailure = { exception ->
                            if (isAdded && _binding != null) {
                                _binding?.progressBar?.visibility = View.GONE
                                Toast.makeText(
                                    requireContext(),
                                    "Failed to load data: ${exception.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )
                } catch (e: Exception) {
                    if (isAdded && _binding != null) {
                        _binding?.progressBar?.visibility = View.GONE
                        Toast.makeText(
                            requireContext(),
                            "Error: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loadDataJob?.cancel()
        loadDataJob = null
        _binding = null
    }
}