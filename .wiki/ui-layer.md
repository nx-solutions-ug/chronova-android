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
- Passes the PRO flag to fragments that need it (`MainPagerFragment`, `LeaderboardFragment`, `InsightsPagerFragment`).
- Hosts a bottom navigation bar with five items: **Dashboard**, **Projects**, **Goals**, **Leaderboard**, and **Insights**.
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
- `DashboardFragment` is a legacy dashboard with a bar chart and recent activity list. It is not currently wired to bottom navigation.

### Projects container

`ProjectsContainerFragment` hosts a `ViewPager2` with two tabs:
- **Projects** (`ProjectsPagerFragment` → `ProjectsStatsFragment`)
- **Editors** (`EditorsPagerFragment` → `EditorsStatsFragment`)

### Drill-down sections

| Pager Fragment | Stats Fragment | Focus |
|----------------|----------------|-------|
| `LanguagesPagerFragment` | `LanguagesStatsFragment` | Languages only |
| `ProjectsPagerFragment` | `ProjectsStatsFragment` | Projects only |
| `EditorsPagerFragment` | `EditorsStatsFragment` | Editors only |

Each pager provides **Today / Last 7 Days / Last 30 Days** tabs.

### Goals

`GoalsFragment` lists user-defined coding goals from the server. It supports swipe-to-delete and a floating action button that opens `CreateGoalDialogFragment` to add new goals.

### Leaderboard

`LeaderboardFragment` shows a ranked list of users for a selected time range. Free users can view the **Last 7 Days** range only; PRO users can also choose **Last 30 Days** and **Last 90 Days**. The current user is pinned at the top when they are not already visible in the first page.

### Insights

`InsightsPagerFragment` hosts two PRO-gated tabs:
- **AI Insights** (`AiInsightsFragment`) — AI vs. manual coding analytics.
- **Focus** (`FocusFragment`) — concentration score, context switches, and deep-work blocks.

Free users see a locked-state view instead of the pager.

### Files

`FilesFragment` lists recent file activity derived from heartbeats, grouped by file path and time spent.

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

- Bottom navigation XML: `app/src/main/res/menu/bottom_navigation.xml` defines `Dashboard`, `Projects`, `Goals`, `Leaderboard`, and `Insights`.
- Main toolbar menu: `app/src/main/res/menu/main_menu.xml` defines **Logout**.
- There is currently no AndroidX Navigation component graph; navigation is done imperatively with `FragmentManager.beginTransaction().replace(...)`.

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
The screen-by-screen call sequences — login, dashboard, leaderboard,
goals, PRO update — are in [Data Flow & Sequences](./architecture/data-flow.md).
