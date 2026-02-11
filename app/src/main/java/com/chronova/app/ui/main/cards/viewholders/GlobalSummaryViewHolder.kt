package com.chronova.app.ui.main.cards.viewholders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chronova.app.databinding.ItemGlobalSummaryBinding

class GlobalSummaryViewHolder(
    private val binding: ItemGlobalSummaryBinding
) : RecyclerView.ViewHolder(binding.root) {

    constructor(inflater: LayoutInflater, parent: ViewGroup) : this(
        ItemGlobalSummaryBinding.inflate(inflater, parent, false)
    )

    fun bind(totalSeconds: Long, timeRange: String) {
        val hours = totalSeconds / 3600.0
        val minutes = (totalSeconds % 3600) / 60.0

        // Format time display like Timeless
        val timeText = when {
            hours >= 1 -> String.format("%.1f hrs", hours)
            minutes >= 1 -> String.format("%.0f mins", minutes)
            else -> "${totalSeconds}s"
        }

        binding.tvTimeValue.text = timeText
        
        // Set context-appropriate subtitle
        binding.tvTimeLabel.text = when (timeRange) {
            "today" -> "Today"
            "last_7_days" -> "Last 7 Days" 
            "last_30_days" -> "Last 30 Days"
            else -> "Total Time"
        }
    }
}