package com.chronova.app.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chronova.app.data.FileActivity
import com.chronova.app.databinding.ItemFileBinding

class FileAdapter : RecyclerView.Adapter<FileAdapter.FileViewHolder>() {

    private var files = listOf<FileActivity>()

    fun updateData(newFiles: List<FileActivity>) {
        files = newFiles
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val binding = ItemFileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(files[position])
    }

    override fun getItemCount(): Int = files.size

    class FileViewHolder(private val binding: ItemFileBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(file: FileActivity) {
            binding.tvFileName.text = file.fileName
            binding.tvProject.text = file.project
            binding.tvLanguage.text = file.language
            binding.tvEditor.text = file.editor
            
            // Format time spent
            val minutes = file.timeSpent / 60
            val hours = minutes / 60
            val timeText = when {
                hours > 0 -> "${hours}h ${minutes % 60}m"
                minutes > 0 -> "${minutes}m"
                else -> "${file.timeSpent}s"
            }
            binding.tvTimeSpent.text = timeText
            
            // Format full path for subtitle
            binding.tvFullPath.text = if (file.fullPath.length > 50) {
                "...${file.fullPath.takeLast(47)}"
            } else {
                file.fullPath
            }
        }
    }
}