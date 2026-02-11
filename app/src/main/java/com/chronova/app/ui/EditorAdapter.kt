package com.chronova.app.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chronova.app.data.Editor
import com.chronova.app.databinding.ItemEditorBinding

class EditorAdapter : RecyclerView.Adapter<EditorAdapter.EditorViewHolder>() {
    
    private var editors = listOf<Editor>()
    
    fun updateData(newEditors: List<Editor>) {
        editors = newEditors
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditorViewHolder {
        val binding = ItemEditorBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EditorViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: EditorViewHolder, position: Int) {
        holder.bind(editors[position])
    }
    
    override fun getItemCount(): Int = editors.size
    
    class EditorViewHolder(private val binding: ItemEditorBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(editor: Editor) {
            binding.tvEditorName.text = editor.name
            binding.tvTotalTime.text = String.format("%.1f hrs", editor.totalTime)
            binding.tvPercentage.text = String.format("%.1f%%", editor.percentage)
            
            // Set color indicator
            try {
                binding.viewColorIndicator.setBackgroundColor(Color.parseColor(editor.color))
            } catch (e: Exception) {
                binding.viewColorIndicator.setBackgroundColor(Color.GRAY)
            }
        }
    }
}