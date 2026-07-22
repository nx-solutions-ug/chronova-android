package com.chronova.app.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chronova.app.R
import com.chronova.app.data.LeaderEntry

class LeaderboardAdapter(
    private val leaders: MutableList<LeaderEntry>,
    private val currentUserId: String?
) : RecyclerView.Adapter<LeaderboardAdapter.LeaderViewHolder>() {

    class LeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val rank: TextView = view.findViewById(R.id.rank)
        val username: TextView = view.findViewById(R.id.username)
        val totalTime: TextView = view.findViewById(R.id.total_time)
        val dailyAverage: TextView = view.findViewById(R.id.daily_average)
        val proBadge: TextView = view.findViewById(R.id.pro_badge)
        val card: com.google.android.material.card.MaterialCardView = view as com.google.android.material.card.MaterialCardView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_leaderboard, parent, false)
        return LeaderViewHolder(view)
    }

    override fun onBindViewHolder(holder: LeaderViewHolder, position: Int) {
        val entry = leaders[position]
        holder.rank.text = entry.rank.toString()
        holder.username.text = entry.leaderUser.displayName
            ?: entry.leaderUser.username
        holder.totalTime.text = entry.runningTotal.humanReadableTotal
        holder.dailyAverage.text = "avg ${entry.runningTotal.humanReadableDailyAverage}"

        val isPro = entry.leaderUser.subscriptionStatus in listOf("active", "trialing", "past_due")
            || entry.leaderUser.isProComped == true
        holder.proBadge.visibility = if (isPro) View.VISIBLE else View.GONE

        if (currentUserId != null && entry.leaderUser.id == currentUserId) {
            holder.card.setCardBackgroundColor(holder.itemView.context.getColor(R.color.surface_variant))
        } else {
            holder.card.setCardBackgroundColor(holder.itemView.context.getColor(R.color.surface))
        }
    }

    override fun getItemCount(): Int = leaders.size

    fun updateLeaders(newLeaders: List<LeaderEntry>) {
        leaders.clear()
        leaders.addAll(newLeaders)
        notifyDataSetChanged()
    }
}