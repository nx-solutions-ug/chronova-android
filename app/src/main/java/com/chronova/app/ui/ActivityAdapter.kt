package com.chronova.app.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chronova.app.data.ActivityData
import com.chronova.app.databinding.ItemActivityBinding

class ActivityAdapter : RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder>() {
    
    private var activities = listOf<ActivityData>()
    
    fun updateData(newActivities: List<ActivityData>) {
        activities = newActivities
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val binding = ItemActivityBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ActivityViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        holder.bind(activities[position])
    }
    
    override fun getItemCount(): Int = activities.size
    
    class ActivityViewHolder(private val binding: ItemActivityBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(activity: ActivityData) {
            binding.tvProject.text = activity.project
            binding.tvFile.text = activity.file
            binding.tvLanguage.text = activity.language
            binding.tvTime.text = activity.time
            binding.tvDuration.text = activity.duration
        }
    }
}