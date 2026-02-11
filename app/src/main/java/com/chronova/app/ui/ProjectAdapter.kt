package com.chronova.app.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chronova.app.data.Project
import com.chronova.app.databinding.ItemProjectBinding

class ProjectAdapter : RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder>() {
    
    private var projects = listOf<Project>()
    
    fun updateData(newProjects: List<Project>) {
        projects = newProjects
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectViewHolder {
        val binding = ItemProjectBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProjectViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        holder.bind(projects[position])
    }
    
    override fun getItemCount(): Int = projects.size
    
    class ProjectViewHolder(private val binding: ItemProjectBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(project: Project) {
            binding.tvProjectName.text = project.name
            binding.tvTotalTime.text = String.format("%.1f hrs", project.totalTime)
            binding.tvPercentage.text = String.format("%.1f%%", project.percentage)
            binding.tvTopLanguage.text = project.topLanguage ?: "No language"
            
            // Set color indicator
            try {
                binding.viewColorIndicator.setBackgroundColor(Color.parseColor(project.color))
            } catch (e: Exception) {
                binding.viewColorIndicator.setBackgroundColor(Color.GRAY)
            }
        }
    }
}