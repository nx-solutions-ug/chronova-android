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
import com.chronova.app.R
import com.chronova.app.data.ChronovaRepository
import com.chronova.app.databinding.FragmentDashboardBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {
    
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: ChronovaRepository
    private lateinit var activityAdapter: ActivityAdapter
    private var loadDataJob: Job? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        repository = ChronovaRepository(requireContext())
        setupRecyclerView()
        loadDashboardData()
    }
    
    private fun setupRecyclerView() {
        activityAdapter = ActivityAdapter()
        binding.rvRecentActivity.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = activityAdapter
        }
    }
    
    private fun loadDashboardData() {
        // Cancel any existing job
        loadDataJob?.cancel()

        _binding?.let { binding ->
            binding.progressBar.visibility = View.VISIBLE

            loadDataJob = lifecycleScope.launch {
                try {
                    val result = repository.getDashboard()
                    result.fold(
                        onSuccess = { dashboard ->
                            if (isAdded && _binding != null) {
                                _binding?.progressBar?.visibility = View.GONE

                                // Update stats
                                _binding?.tvTotalHours?.text = String.format("%.1f hrs", dashboard.totalHours)
                                _binding?.tvWeeklyHours?.text = String.format("%.1f hrs", dashboard.weeklyHours)
                                _binding?.tvProjects?.text = dashboard.projects.toString()
                                _binding?.tvLanguages?.text = dashboard.languages.toString()

                                // Update chart
                                setupChart(dashboard.dailyData)

                                // Update recent activity
                                activityAdapter.updateData(dashboard.recentActivity)
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
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
            }
        }
    }
    
    private fun setupChart(dailyData: List<com.chronova.app.data.DailyData>) {
        val entries = dailyData.mapIndexed { index, data ->
            BarEntry(index.toFloat(), data.hours.toFloat())
        }
        
        val dataSet = BarDataSet(entries, "Daily Hours").apply {
            color = Color.parseColor("#3B82F6")
            setDrawValues(false)
        }
        
        val barData = BarData(dataSet)
        
        binding.chartDaily.apply {
            data = barData
            description.isEnabled = false
            legend.isEnabled = false
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                valueFormatter = com.github.mikephil.charting.formatter.IndexAxisValueFormatter(
                    dailyData.map { it.date }
                )
                textColor = Color.parseColor("#CBD5E1") // text_secondary color
                textSize = 10f
                axisLineColor = Color.parseColor("#334155") // surface_variant color
            }
            
            axisLeft.apply {
                axisMinimum = 0f
                setDrawGridLines(true)
                gridColor = Color.parseColor("#334155") // surface_variant color for subtle grid
                textColor = Color.parseColor("#CBD5E1") // text_secondary color
                textSize = 10f
                axisLineColor = Color.parseColor("#334155") // surface_variant color
            }
            
            axisRight.isEnabled = false
            
            invalidate()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        loadDataJob?.cancel()
        loadDataJob = null
        _binding = null
    }
}