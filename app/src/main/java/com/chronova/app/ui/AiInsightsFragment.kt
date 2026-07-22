package com.chronova.app.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chronova.app.R
import com.chronova.app.data.AiAnalyticsData
import com.chronova.app.data.ChronovaRepository
import com.chronova.app.databinding.FragmentAiInsightsBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.chip.Chip
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class AiInsightsFragment : Fragment() {

    private var _binding: FragmentAiInsightsBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: ChronovaRepository
    private var loadJob: Job? = null

    private val ranges = listOf(
        "last_7_days" to "7 Days",
        "last_30_days" to "30 Days",
        "last_3_months" to "3 Months",
        "last_6_months" to "6 Months",
        "last_year" to "1 Year",
        "all_time" to "All Time"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAiInsightsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = ChronovaRepository(requireContext())

        setupRangeChips()
        loadAiAnalytics("last_7_days")
    }

    private fun setupRangeChips() {
        ranges.forEachIndexed { index, (range, label) ->
            val chip = Chip(requireContext()).apply {
                text = label
                isCheckable = true
                isChecked = index == 0
                setOnClickListener { loadAiAnalytics(range) }
            }
            binding.rangeChips.addView(chip)
        }
    }

    private fun loadAiAnalytics(range: String) {
        binding.progressBar.visibility = View.VISIBLE

        loadJob?.cancel()
        loadJob = viewLifecycleOwner.lifecycleScope.launch {
            repository.getAiAnalytics(range)
                .onSuccess { data ->
                    if (!isAdded || _binding == null) return@launch
                    binding.progressBar.visibility = View.GONE
                    populateData(data)
                }
                .onFailure { e ->
                    if (!isAdded || _binding == null) return@launch
                    binding.progressBar.visibility = View.GONE
                }
        }
    }

    private fun populateData(data: AiAnalyticsData) {
        setupContributionPie(data)
        setupAdoptionChart(data)
        setupEfficiencyChart(data)
        setupLanguageMatrix(data)
        setupProjectDependency(data)
    }

    private fun setupContributionPie(data: AiAnalyticsData) {
        val chart = binding.contributionPieChart
        val entries = listOf(
            PieEntry(data.contributionShare.aiPercent.toFloat(), "AI"),
            PieEntry(data.contributionShare.manualPercent.toFloat(), "Manual")
        )

        val dataSet = PieDataSet(entries, "AI vs Manual").apply {
            colors = listOf(
                requireContext().getColor(R.color.colorSecondary),
                requireContext().getColor(R.color.colorPrimary)
            )
            valueTextSize = 14f
            valueTextColor = Color.WHITE
        }

        chart.data = PieData(dataSet)
        chart.description.isEnabled = false
        chart.legend.textColor = requireContext().getColor(R.color.text_secondary)
        chart.centerText = String.format("%.0f%% AI", data.contributionShare.aiPercent)
        chart.setCenterTextSize(14f)
        chart.setCenterTextColor(requireContext().getColor(R.color.text_primary))
        chart.invalidate()
    }

    private fun setupAdoptionChart(data: AiAnalyticsData) {
        val chart = binding.adoptionChart
        val timeline = data.adoptionTimeline
        if (timeline.isEmpty()) {
            chart.clear()
            return
        }

        val aiEntries = timeline.mapIndexed { i, point ->
            Entry(i.toFloat(), point.aiSeconds.toFloat())
        }
        val manualEntries = timeline.mapIndexed { i, point ->
            Entry(i.toFloat(), point.manualSeconds.toFloat())
        }

        val aiDataSet = LineDataSet(aiEntries, "AI Seconds").apply {
            color = requireContext().getColor(R.color.colorSecondary)
            setDrawCircles(false)
            lineWidth = 2f
        }
        val manualDataSet = LineDataSet(manualEntries, "Manual Seconds").apply {
            color = requireContext().getColor(R.color.colorPrimary)
            setDrawCircles(false)
            lineWidth = 2f
        }

        chart.data = LineData(aiDataSet, manualDataSet)
        chart.description.isEnabled = false
        chart.legend.textColor = requireContext().getColor(R.color.text_secondary)
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.xAxis.textColor = requireContext().getColor(R.color.text_secondary)
        chart.axisLeft.textColor = requireContext().getColor(R.color.text_secondary)
        chart.axisRight.isEnabled = false
        chart.invalidate()
    }

    private fun setupEfficiencyChart(data: AiAnalyticsData) {
        val chart = binding.efficiencyChart
        val trend = data.efficiencyTrend
        if (trend.isEmpty()) {
            chart.clear()
            return
        }

        val entries = trend.mapIndexed { i, point ->
            Entry(i.toFloat(), point.aiPercent.toFloat())
        }

        val dataSet = LineDataSet(entries, "AI %").apply {
            color = requireContext().getColor(R.color.colorSecondary)
            setDrawCircles(false)
            lineWidth = 2f
            setDrawValues(false)
        }

        chart.data = LineData(dataSet)
        chart.description.isEnabled = false
        chart.legend.textColor = requireContext().getColor(R.color.text_secondary)
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.xAxis.textColor = requireContext().getColor(R.color.text_secondary)
        chart.axisLeft.textColor = requireContext().getColor(R.color.text_secondary)
        chart.axisRight.isEnabled = false
        chart.invalidate()
    }

    private fun setupLanguageMatrix(data: AiAnalyticsData) {
        val recyclerView = binding.languageMatrixList
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = SimpleInsightAdapter(
            data.languageMatrix.map { entry ->
                InsightRowData(
                    label = entry.language,
                    value1 = String.format("%.0f%% AI", (if (entry.totalSeconds > 0) entry.aiSeconds / entry.totalSeconds * 100 else 0.0)),
                    value2 = String.format("%.1f hrs", entry.totalSeconds / 3600.0)
                )
            }
        )
    }

    private fun setupProjectDependency(data: AiAnalyticsData) {
        val recyclerView = binding.projectDependencyList
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = SimpleInsightAdapter(
            data.projectDependency.map { entry ->
                InsightRowData(
                    label = entry.project,
                    value1 = String.format("%.0f%% AI", entry.aiPercent),
                    value2 = String.format("%.1f hrs", entry.totalSeconds / 3600.0)
                )
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loadJob?.cancel()
        _binding = null
    }
}

data class InsightRowData(
    val label: String,
    val value1: String,
    val value2: String
)

class SimpleInsightAdapter(private val items: List<InsightRowData>) :
    RecyclerView.Adapter<SimpleInsightAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val label: TextView = view.findViewById(R.id.item_label)
        val value1: TextView = view.findViewById(R.id.item_value_1)
        val value2: TextView = view.findViewById(R.id.item_value_2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_insight_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.label.text = item.label
        holder.value1.text = item.value1
        holder.value2.text = item.value2
    }

    override fun getItemCount(): Int = items.size
}