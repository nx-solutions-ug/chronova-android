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
import com.chronova.app.databinding.FragmentMainStatsBinding
import com.chronova.app.ui.main.cards.CardsAdapter
import com.chronova.app.ui.main.cards.CardsList
import kotlinx.coroutines.launch

class ProjectsStatsFragment : Fragment() {

    private var _binding: FragmentMainStatsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var repository: ChronovaRepository
    private lateinit var cardsAdapter: CardsAdapter
    private var timeRange: String = "today"

    companion object {
        private const val ARG_TIME_RANGE = "time_range"
        
        fun newInstance(timeRange: String): ProjectsStatsFragment {
            return ProjectsStatsFragment().apply {
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
        loadProjectsData()
    }

    private fun setupRecyclerView() {
        cardsAdapter = CardsAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = cardsAdapter
        }
    }

    private fun loadProjectsData() {
        binding.progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                val result = repository.getStatsForRange(timeRange)
                result.fold(
                    onSuccess = { stats ->
                        binding.progressBar.visibility = View.GONE
                        
                        // Build cards list focused on projects
                        val cardsList = CardsList().apply {
                            // Global summary card showing total time
                            addGlobalSummary(stats.totalSeconds, timeRange)
                            
                            // Main projects pie chart
                            if (stats.projects.isNotEmpty()) {
                                addPieChart("Top 5 Projects", stats.projects)
                            }
                        }
                        
                        cardsAdapter.updateCards(cardsList)
                    },
                    onFailure = { exception ->
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(
                            requireContext(),
                            "Failed to load project data: ${exception.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(
                    requireContext(),
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}