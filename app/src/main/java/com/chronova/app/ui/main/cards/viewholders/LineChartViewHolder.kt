package com.chronova.app.ui.main.cards.viewholders

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chronova.app.databinding.ItemLineChartBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class LineChartViewHolder(
    private val binding: ItemLineChartBinding
) : RecyclerView.ViewHolder(binding.root) {

    constructor(inflater: LayoutInflater, parent: ViewGroup) : this(
        ItemLineChartBinding.inflate(inflater, parent, false)
    )

    fun bind(title: String, data: List<Pair<String, Float>>) {
        binding.tvTitle.text = title
        
        if (data.isEmpty()) {
            binding.lineChart.setNoDataText("No data available")
            return
        }

        // Convert data to Line entries
        val entries = data.mapIndexed { index, (_, value) ->
            Entry(index.toFloat(), value)
        }

        val dataSet = LineDataSet(entries, title).apply {
            color = Color.parseColor("#4f5b69") // Timeless primary color
            setCircleColor(Color.parseColor("#00acac")) // Timeless secondary color
            lineWidth = 3f
            circleRadius = 4f
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        val lineData = LineData(dataSet)
        
        binding.lineChart.apply {
            this.data = lineData
            description.isEnabled = false
            
            // Configure legend with proper theme colors
            legend.apply {
                isEnabled = true
                textSize = 12f
                textColor = Color.parseColor("#CBD5E1") // text_secondary color
                horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER
                verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.TOP
            }
            
            // Configure X-axis with theme colors
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                valueFormatter = IndexAxisValueFormatter(data.map { it.first })
                textColor = Color.parseColor("#CBD5E1") // text_secondary color
                textSize = 10f
                axisLineColor = Color.parseColor("#334155") // surface_variant color
            }
            
            // Configure Y-axis with theme colors
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = Color.parseColor("#334155") // surface_variant color for subtle grid
                textColor = Color.parseColor("#CBD5E1") // text_secondary color
                textSize = 10f
                axisLineColor = Color.parseColor("#334155") // surface_variant color
            }
            
            axisRight.isEnabled = false
            
            animateX(1000)
            invalidate()
        }
    }
}