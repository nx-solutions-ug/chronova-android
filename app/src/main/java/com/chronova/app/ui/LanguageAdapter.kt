package com.chronova.app.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chronova.app.data.Language
import com.chronova.app.databinding.ItemLanguageBinding

class LanguageAdapter : RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder>() {
    
    private var languages = listOf<Language>()
    
    fun updateData(newLanguages: List<Language>) {
        languages = newLanguages
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        val binding = ItemLanguageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LanguageViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        holder.bind(languages[position])
    }
    
    override fun getItemCount(): Int = languages.size
    
    class LanguageViewHolder(private val binding: ItemLanguageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(language: Language) {
            binding.tvLanguageName.text = language.name
            binding.tvTotalTime.text = String.format("%.1f hrs", language.totalTime)
            binding.tvPercentage.text = String.format("%.1f%%", language.percentage)
            binding.tvTopProject.text = language.topProject ?: "No project"
            
            // Set color indicator
            try {
                binding.viewColorIndicator.setBackgroundColor(Color.parseColor(language.color))
            } catch (e: Exception) {
                binding.viewColorIndicator.setBackgroundColor(Color.GRAY)
            }
        }
    }
}