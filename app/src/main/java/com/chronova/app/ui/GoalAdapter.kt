package com.chronova.app.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chronova.app.R
import com.chronova.app.data.Goal

class GoalAdapter(
    internal val goals: MutableList<Goal>,
    private val onDeleteClick: (Goal) -> Unit
) : RecyclerView.Adapter<GoalAdapter.GoalViewHolder>() {

    class GoalViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.goal_title)
        val type: TextView = view.findViewById(R.id.goal_type)
        val progressBar: ProgressBar = view.findViewById(R.id.goal_progress_bar)
        val progressText: TextView = view.findViewById(R.id.goal_progress_text)
        val deleteBtn: ImageButton = view.findViewById(R.id.btn_delete_goal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_goal, parent, false)
        return GoalViewHolder(view)
    }

    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        val goal = goals[position]
        holder.title.text = goal.title
        holder.type.text = formatType(goal)

        val actualSeconds = goal.chartData?.sumOf { it.actualSeconds } ?: 0.0
        val targetSeconds = goal.seconds
        val progress = if (targetSeconds > 0) ((actualSeconds / targetSeconds) * 100).toInt().coerceIn(0, 100) else 0
        holder.progressBar.progress = progress

        val actualHours = actualSeconds / 3600.0
        val targetHours = targetSeconds / 3600.0
        holder.progressText.text = String.format("%d%% · %.1f hrs / %.1f hrs", progress, actualHours, targetHours)

        holder.deleteBtn.setOnClickListener { onDeleteClick(goal) }
    }

    override fun getItemCount(): Int = goals.size

    fun updateGoals(newGoals: List<Goal>) {
        goals.clear()
        goals.addAll(newGoals)
        notifyDataSetChanged()
    }

    private fun formatType(goal: Goal): String {
        val parts = mutableListOf<String>()
        parts.add(goal.type.replace("_", " "))
        parts.add(if (goal.delta == "day") "daily" else "weekly")
        goal.language?.let { parts.add(it) }
        goal.project?.let { parts.add(it) }
        return parts.joinToString(" · ")
    }
}