package com.chronova.app.ui.main.cards.viewholders

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chronova.app.databinding.ItemPieChartBinding
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate

class PieChartViewHolder(
    private val binding: ItemPieChartBinding
) : RecyclerView.ViewHolder(binding.root) {

    constructor(inflater: LayoutInflater, parent: ViewGroup) : this(
        ItemPieChartBinding.inflate(inflater, parent, false)
    )

    fun bind(title: String, dataMap: Map<String, Long>) {
        binding.tvTitle.text = title

        if (dataMap.isEmpty()) {
            binding.pieChart.setNoDataText("No data available")
            return
        }

        // Filter to top 5 entries by value and group others
        val sortedEntries = dataMap.entries.sortedByDescending { it.value }
        val topEntries = sortedEntries.take(5)
        val otherEntries = sortedEntries.drop(5)

        // Calculate total for percentage calculation
        val totalValue = dataMap.values.sum().toFloat()

        val entries = mutableListOf<PieEntry>()

        // Add top 5 entries with percentage values
        topEntries.forEach { (name, seconds) ->
            val percentage = (seconds.toFloat() / totalValue) * 100f
            entries.add(PieEntry(percentage, name))
        }

        // Group remaining entries as "Others" if there are any
        if (otherEntries.isNotEmpty()) {
            val otherTotal = otherEntries.sumOf { it.value }.toFloat()
            val otherPercentage = (otherTotal / totalValue) * 100f
            entries.add(PieEntry(otherPercentage, "Others"))
        }

        val dataSet = PieDataSet(entries, "").apply {
            colors = getTimelessColors()
            setDrawValues(true)
            valueTextSize = 12f
            valueTextColor = Color.WHITE // Keep white for visibility on colored slices
            sliceSpace = 2f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return String.format("%.0f%%", value)
                }
            }
        }

        val pieData = PieData(dataSet)

        with(binding.pieChart) {
            data = pieData
            description.isEnabled = false
            isRotationEnabled = true
            setUsePercentValues(false) // We calculate percentages manually
            setEntryLabelTextSize(10f)
            setEntryLabelColor(Color.WHITE) // white for visibility on colored slices
            centerText = title
            setCenterTextSize(14f)
            setCenterTextColor(Color.parseColor("#1e293b")) // dark color for visibility

            // Configure legend with proper theme colors
            legend.apply {
                isEnabled = true
                textSize = 12f
                textColor = Color.parseColor("#CBD5E1") // text_secondary color
                horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER
                verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM
                orientation = com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
            }

            animateY(1000)
            invalidate()
        }
    }

    private fun getTimelessColors(): List<Int> {
        // Professional color palette similar to Timeless
        return listOf(
            Color.parseColor("#4f5b69"), // Primary blue-gray
            Color.parseColor("#00acac"), // Secondary teal
            Color.parseColor("#6366f1"), // Indigo
            Color.parseColor("#8b5cf6"), // Violet
            Color.parseColor("#10b981"), // Emerald
            Color.parseColor("#f59e0b"), // Amber
            Color.parseColor("#ef4444"), // Red
            Color.parseColor("#ec4899"), // Pink
            Color.parseColor("#84cc16"), // Lime
            Color.parseColor("#06b6d4")  // Cyan
        )
    }
}