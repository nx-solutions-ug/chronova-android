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

| Container / Pager | Child fragments | Focus |
|-------------------|-----------------|-------|
| `ProjectsContainerFragment` | `ProjectsPagerFragment`, `EditorsPagerFragment` | Projects and editors |
| `ProjectsPagerFragment` | `ProjectsStatsFragment` | Projects only |
| `EditorsPagerFragment` | `EditorsStatsFragment` | Editors only |
| `LanguagesPagerFragment` | `LanguagesStatsFragment` | Languages only |
| `InsightsPagerFragment` | `AiInsightsFragment`, `FocusFragment` | AI analytics and focus analytics |

Each stats pager provides **Today / Last 7 Days / Last 30 Days** tabs. Insights tabs are **AI Insights** and **Focus**.

### Lists

| Fragment | Content |
|----------|---------|
| `FilesFragment` | Recent file activity derived from heartbeats, grouped by file path and time spent. |
| `GoalsFragment` | Personal coding goals loaded via `repository.getGoals()`; supports creation and swipe-to-delete. |
| `LeaderboardFragment` | Global ranking from `repository.getLeaders()`; PRO users can switch ranges. |

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

## PRO status gating

Several screens consult the `is_pro_user` argument to limit functionality:

- `MainPagerFragment` shows two tabs for free users and six tabs for PRO users.
- `InsightsPagerFragment` is fully locked behind the PRO paywall when accessed by a free user.
- `LeaderboardFragment` offers 7/30/90-day ranges for PRO users; free users only see **7 Days**.

## Navigation

- Bottom navigation XML: `app/src/main/res/menu/bottom_navigation.xml`.
- Main toolbar menu: `app/src/main/res/menu/main_menu.xml`.
- There is currently no AndroidX Navigation component graph; navigation is done imperatively with `FragmentManager.beginTransaction().replace(...)`, driven by `MainActivity.setupBottomNavigation()`.

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
