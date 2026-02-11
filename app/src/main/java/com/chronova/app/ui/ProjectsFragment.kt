package com.chronova.app.ui

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
import com.chronova.app.data.Project
import com.chronova.app.databinding.FragmentProjectsBinding
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.launch

class ProjectsFragment : Fragment() {
    
    private var _binding: FragmentProjectsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var repository: ChronovaRepository
    private lateinit var projectAdapter: ProjectAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProjectsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        repository = ChronovaRepository(requireContext())
        setupRecyclerView()
        loadProjectData()
    }
    
    private fun setupRecyclerView() {
        projectAdapter = ProjectAdapter()
        binding.rvProjects.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = projectAdapter
        }
    }
    
    private fun loadProjectData() {
        binding.progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                val result = repository.getProjects()
                result.fold(
                    onSuccess = { response ->
                        binding.progressBar.visibility = View.GONE
                        
                        // Update pie chart
                        setupPieChart(response.data)
                        
                        // Update list
                        projectAdapter.updateData(response.data)
                    },
                    onFailure = { exception ->
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(
                            requireContext(),
                            "Failed to load projects: ${exception.message}",
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
    
    private fun setupPieChart(projects: List<Project>) {
        val entries = projects.map { project ->
            PieEntry(project.totalTime.toFloat(), project.name)
        }
        
        val colors = projects.map { Color.parseColor(it.color) }
        
        val dataSet = PieDataSet(entries, "Projects").apply {
            this.colors = colors
            setDrawValues(true)
            valueTextSize = 12f
            valueTextColor = Color.WHITE
        }
        
        val pieData = PieData(dataSet)
        
        binding.chartProjects.apply {
            data = pieData
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.TRANSPARENT)
            holeRadius = 40f
            transparentCircleRadius = 45f
            setDrawCenterText(true)
            centerText = "Projects"
            setCenterTextSize(16f)
            setCenterTextColor(Color.WHITE)
            
            legend.apply {
                isEnabled = true
                textSize = 12f
                textColor = Color.WHITE
            }
            
            invalidate()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}