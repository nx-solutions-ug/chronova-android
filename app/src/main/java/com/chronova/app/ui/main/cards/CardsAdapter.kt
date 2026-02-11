package com.chronova.app.ui.main.cards

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chronova.app.ui.main.cards.viewholders.GlobalSummaryViewHolder
import com.chronova.app.ui.main.cards.viewholders.PieChartViewHolder
import com.chronova.app.ui.main.cards.viewholders.LineChartViewHolder

class CardsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_GLOBAL_SUMMARY = 0
        private const val TYPE_PIE_CHART = 1
        private const val TYPE_LINE_CHART = 2
    }

    private var cardsList: CardsList = CardsList()

    fun updateCards(newCardsList: CardsList) {
        cardsList = newCardsList
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return cardsList.types[position]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        
        return when (viewType) {
            TYPE_GLOBAL_SUMMARY -> GlobalSummaryViewHolder(inflater, parent)
            TYPE_PIE_CHART -> PieChartViewHolder(inflater, parent)
            TYPE_LINE_CHART -> LineChartViewHolder(inflater, parent)
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val payload = cardsList.payloads[position]
        
        when (holder) {
            is GlobalSummaryViewHolder -> {
                val item = payload as CardsList.GlobalSummaryItem
                holder.bind(item.totalSeconds, item.timeRange)
            }
            is PieChartViewHolder -> {
                val item = payload as CardsList.PieChartItem
                holder.bind(item.title, item.data)
            }
            is LineChartViewHolder -> {
                val item = payload as CardsList.LineChartItem
                holder.bind(item.title, item.data)
            }
        }
    }

    override fun getItemCount(): Int = cardsList.types.size
}