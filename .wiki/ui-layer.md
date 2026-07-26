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
- Hosts a bottom navigation bar with **Dashboard**, **Projects**, **Goals**, **Leaderboard**, and **Insights** items (`app/src/main/res/menu/bottom_navigation.xml`).
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
- `DashboardFragment` and `FilesFragment` are retained in the source but are not wired to bottom navigation.

### Projects

- `ProjectsContainerFragment` is swapped in when the user selects **Projects** in the bottom nav.
- It hosts a `ViewPager2` with two tabs:
  - **Projects** — `ProjectsPagerFragment` → `ProjectsStatsFragment` per time range.
  - **Editors** — `EditorsPagerFragment` → `EditorsStatsFragment` per time range.

### Goals

- `GoalsFragment` lists the user’s coding goals in a `RecyclerView` via `GoalAdapter`.
- A floating action button opens `CreateGoalDialogFragment` to add a goal.
- Swiping a goal left or right triggers `ChronovaRepository.deleteGoal()` and refreshes the list.

### Leaderboard

- `LeaderboardFragment` shows a ranked list of users via `LeaderboardAdapter`.
- Free users see only the **7 Days** range; PRO users can switch between **7 Days**, **30 Days**, and **90 Days**.
- The current user is pinned at the top when they are not in the first page of results.

### Insights

- `InsightsPagerFragment` is shown for the **Insights** bottom-nav item.
- It is gated behind the PRO subscription:
  - Free users see a locked overlay.
  - PRO users see two tabs:
    - **AI Insights** — `AiInsightsFragment` renders AI-vs-manual contribution charts, adoption timelines, efficiency trends, language matrix, and project dependency data.
    - **Focus** — `FocusFragment` renders concentration score, context switches, deep-work blocks, and project distribution from `FocusAnalyticsData`.

### Drill-down sections

| Pager Fragment | Stats Fragment | Focus |
|----------------|----------------|-------|
| `LanguagesPagerFragment` | `LanguagesStatsFragment` | Languages only |
| `ProjectsPagerFragment` | `ProjectsStatsFragment` | Projects only |
| `EditorsPagerFragment` | `EditorsStatsFragment` | Editors only |

Each pager provides **Today / Last 7 Days / Last 30 Days** tabs.

### Files and legacy dashboard

`FilesFragment` and `DashboardFragment` exist in the source but are not currently wired to the bottom navigation.

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

- Bottom navigation XML: `app/src/main/res/menu/bottom_navigation.xml` defines Dashboard, Projects, Goals, Leaderboard, and Insights.
- Main toolbar menu: `app/src/main/res/menu/main_menu.xml`.
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
