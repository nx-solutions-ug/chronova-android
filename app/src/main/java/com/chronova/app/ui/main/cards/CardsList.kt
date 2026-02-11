package com.chronova.app.ui.main.cards

class CardsList {
    companion object {
        const val TYPE_GLOBAL_SUMMARY = 0
        const val TYPE_PIE_CHART = 1
        const val TYPE_LINE_CHART = 2
    }

    val types = mutableListOf<Int>()
    val payloads = mutableListOf<Any>()

    fun addGlobalSummary(totalSeconds: Long, timeRange: String): CardsList {
        types.add(TYPE_GLOBAL_SUMMARY)
        payloads.add(GlobalSummaryItem(totalSeconds, timeRange))
        return this
    }

    fun addPieChart(title: String, data: Map<String, Long>): CardsList {
        types.add(TYPE_PIE_CHART)
        payloads.add(PieChartItem(title, data))
        return this
    }

    fun addLineChart(title: String, data: List<Pair<String, Float>>): CardsList {
        types.add(TYPE_LINE_CHART)
        payloads.add(LineChartItem(title, data))
        return this
    }

    data class GlobalSummaryItem(
        val totalSeconds: Long,
        val timeRange: String
    )

    data class PieChartItem(
        val title: String,
        val data: Map<String, Long>
    )

    data class LineChartItem(
        val title: String,
        val data: List<Pair<String, Float>>
    )
}