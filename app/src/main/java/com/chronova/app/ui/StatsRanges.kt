package com.chronova.app.ui

/**
 * The time-range tabs a stats pager offers, by plan.
 *
 * The server refuses a window older than the Free plan's 7 days with a 403, so a
 * Free account only gets the tabs it can actually load.
 */
object StatsRanges {
    const val ARG_IS_PRO_USER = "is_pro_user"

    private val FREE = listOf(
        "today" to "Today",
        "last_7_days" to "Last 7 Days"
    )

    private val PRO = FREE + ("last_30_days" to "Last 30 Days")

    /** Pairs of API range name and tab label. */
    fun forPlan(isProUser: Boolean): List<Pair<String, String>> = if (isProUser) PRO else FREE
}
