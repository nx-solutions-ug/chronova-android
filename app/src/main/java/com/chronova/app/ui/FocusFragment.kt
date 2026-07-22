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
import com.chronova.app.data.FocusAnalyticsData
import com.chronova.app.data.ChronovaRepository
import com.chronova.app.databinding.FragmentFocusBinding
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.chip.Chip
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class FocusFragment : Fragment() {

    private var _binding: FragmentFocusBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: ChronovaRepository
    private var loadJob: Job? = null

    private val ranges = listOf(
        "last_7_days" to "7 Days",
        "last_30_days" to "30 Days"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFocusBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = ChronovaRepository(requireContext())

        setupRangeChips()
        loadFocusAnalytics("last_7_days")
    }

    private fun setupRangeChips() {
        ranges.forEachIndexed { index, (range, label) ->
            val chip = Chip(requireContext()).apply {
                text = label
                isCheckable = true
                isChecked = index == 0
                setOnClickListener { loadFocusAnalytics(range) }
            }
            binding.rangeChips.addView(chip)
        }
    }

    private fun loadFocusAnalytics(range: String) {
        binding.progressBar.visibility = View.VISIBLE

        loadJob?.cancel()
        loadJob = viewLifecycleOwner.lifecycleScope.launch {
            repository.getFocusAnalytics(range)
                .onSuccess { data ->
                    if (!isAdded || _binding == null) return@launch
                    binding.progressBar.visibility = View.GONE
                    populateData(data)
                }
                .onFailure {
                    if (!isAdded || _binding == null) return@launch
                    binding.progressBar.visibility = View.GONE
                }
        }
    }

    private fun populateData(data: FocusAnalyticsData) {
        // Concentration score with color coding
        val score = data.concentrationScore.toInt()
        binding.concentrationScore.text = score.toString()
        val scoreColor = when {
            score >= 70 -> R.color.success
            score >= 40 -> R.color.warning
            else -> R.color.error
        }
        binding.concentrationScore.setTextColor(requireContext().getColor(scoreColor))

        // Total coding time
        binding.totalCodingTime.text = String.format("%.1f hrs total", data.totalCodingTime / 3600.0)

        // Context switches
        binding.contextSwitchesTotal.text = data.contextSwitches.total.toString()
        binding.contextSwitchesPerHour.text = String.format("%.1f/hr", data.contextSwitches.perHour)

        // Deep work
        binding.deepWorkTotal.text = String.format("%.1f hrs", data.deepWorkBlocks.totalDeepWorkHours)
        binding.deepWorkBlocksCount.text = data.deepWorkBlocks.totalBlocks.toString()

        // Project distribution pie chart
        setupProjectDistribution(data)

        // Deep work blocks list
        binding.deepWorkBlocksList.layoutManager = LinearLayoutManager(requireContext())
        binding.deepWorkBlocksList.adapter = FocusRowAdapter(
            data.deepWorkBlocks.blocks.map { block ->
                FocusRowData(
                    label = block.project,
                    detail = "${block.startTimeFormatted} - ${block.endTimeFormatted}",
                    value = String.format("%.1f hrs", block.duration / 3600.0)
                )
            }
        )

        // Context switches list
        binding.contextSwitchesList.layoutManager = LinearLayoutManager(requireContext())
        binding.contextSwitchesList.adapter = FocusRowAdapter(
            data.contextSwitches.recent.map { sw ->
                FocusRowData(
                    label = "${sw.fromProject} → ${sw.toProject}",
                    detail = sw.timestampFormatted,
                    value = ""
                )
            }
        )
    }

    private fun setupProjectDistribution(data: FocusAnalyticsData) {
        val chart = binding.projectDistributionChart
        if (data.projectDistribution.isEmpty()) {
            chart.clear()
            return
        }

        val entries = data.projectDistribution.map { dist ->
            PieEntry(dist.percent.toFloat(), dist.name)
        }

        val dataSet = PieDataSet(entries, "Projects").apply {
            colors = listOf(
                requireContext().getColor(R.color.colorPrimary),
                requireContext().getColor(R.color.colorSecondary),
                requireContext().getColor(R.color.success),
                requireContext().getColor(R.color.warning),
                requireContext().getColor(R.color.error),
                requireContext().getColor(R.color.surface_variant)
            )
            valueTextSize = 12f
            valueTextColor = Color.WHITE
        }

        chart.data = PieData(dataSet)
        chart.description.isEnabled = false
        chart.legend.textColor = requireContext().getColor(R.color.text_secondary)
        chart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loadJob?.cancel()
        _binding = null
    }
}

data class FocusRowData(
    val label: String,
    val detail: String,
    val value: String
)

class FocusRowAdapter(private val items: List<FocusRowData>) :
    RecyclerView.Adapter<FocusRowAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val label: TextView = view.findViewById(R.id.item_label)
        val detail: TextView = view.findViewById(R.id.item_value_1)
        val value: TextView = view.findViewById(R.id.item_value_2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_focus_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.label.text = item.label
        holder.detail.text = item.detail
        holder.value.text = item.value
        holder.value.visibility = if (item.value.isEmpty()) View.GONE else View.VISIBLE
    }

    override fun getItemCount(): Int = items.size
}