---
type: ui
title: UI Layer
description: Activities, fragments, navigation, charts, and the card-based dashboard.
tags: [ui, fragments, navigation, charts]
---

# UI Layer

The UI layer follows a custom MVVM pattern: fragments own their state, use `lifecycleScope` for coroutines, and observe repository results through `Result<T>`.

## Activities

### `MainActivity`

- **Launcher activity** declared in `AndroidManifest.xml`.
- Redirects to `LoginActivity` if the user is not authenticated.
- Checks the PRO subscription status with `repository.checkProSubscription()` and appends `" ⭐ PRO"` to the toolbar title when true.
- Hosts a bottom navigation bar with **Dashboard**, **Projects**, **Goals**, **Leaderboard**, and **Insights** items.
- Toolbar menu provides **Logout**, which clears the API key and returns to `LoginActivity`.

### `LoginActivity`

- Allows two authentication paths:
  1. **Email + password** → calls `repository.login()` and saves the returned `apiKey`.
  2. **API key** → validates and stores the key directly.
- Validates and saves a custom server URL if one is provided (default remains `https://chronova.dev/`).
- Navigates to `MainActivity` on success.

## Fragments

### Dashboard

- `MainPagerFragment` hosts `ViewPager2` with tabs.
  - Free users see **Today** and **Last 7 Days**.
  - PRO users see six ranges: **Today**, **Last 7 Days**, **Last 30 Days**, **Last 3 Months**, **Last Year**, **All Time**.
- `MainStatsFragment` is the page inside the pager. It builds a `CardsList` with:
  - global summary card,
  - pie charts for languages/projects/editors,
  - line chart for daily activity.
- `DashboardFragment` is a separate older dashboard with a bar chart and recent activity list. It is not currently wired to bottom navigation.

### Drill-down sections

| Pager Fragment | Stats Fragment | Focus |
|----------------|----------------|-------|
| `LanguagesPagerFragment` | `LanguagesStatsFragment` | Languages only |
| `ProjectsPagerFragment` | `ProjectsStatsFragment` | Projects only |
| `EditorsPagerFragment` | `EditorsStatsFragment` | Editors only |

Each pager provides **Today / Last 7 Days / Last 30 Days** tabs.

### Projects container

`ProjectsContainerFragment` hosts a two-tab pager that combines `ProjectsPagerFragment` and `EditorsPagerFragment` under a single bottom-navigation destination.

### Goals

`GoalsFragment` lists user goals, supports swipe-to-delete, and shows a FAB that opens `CreateGoalDialogFragment` to add a new goal.

### Leaderboard

`LeaderboardFragment` shows ranked users. Free users see only the **7 Days** range; PRO users can switch between **7 Days**, **30 Days**, and **90 Days**. The current user's entry is pinned at the top when it is not on the first page.

### AI Insights and Focus

`InsightsPagerFragment` is gated to PRO users only. Non-PRO users see a locked state. PRO users see two tabs:

- `AiInsightsFragment` — AI analytics with selectable ranges.
- `FocusFragment` — focus analytics.

### Dashboard legacy fragment

`DashboardFragment` is a standalone older dashboard with a bar chart and recent activity list. It is not wired to bottom navigation and exists as a separate implementation from the card-based `MainStatsFragment` dashboard.

## Card dashboard

`MainStatsFragment` uses a heterogeneous RecyclerView built from:

- `CardsList.kt` — type-safe builder for global summary, pie chart, and line chart items.
- `CardsAdapter.kt` — multi-view-type adapter.
- `GlobalSummaryViewHolder`, `PieChartViewHolder`, `LineChartViewHolder` — bind each card.

## Charts

Charts are rendered with [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart):

- **Pie charts** for language/project/editor distribution.
- **Line charts** for daily activity trends.
- **Bar charts** in `DashboardFragment`.

## Navigation

- Bottom navigation XML: `app/src/main/res/menu/bottom_navigation.xml`.
- Main toolbar menu: `app/src/main/res/menu/main_menu.xml`.
- Navigation is done imperatively with `FragmentManager.beginTransaction().replace(...)`; there is no AndroidX Navigation component graph.

## Mandatory ViewBinding pattern

```kotlin
class MyFragment : Fragment(R.layout.fragment_my) {
    private var _binding: FragmentMyBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(...): View {
        _binding = FragmentMyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
```

See [Development Conventions](./development-conventions.md) for the full rules.
