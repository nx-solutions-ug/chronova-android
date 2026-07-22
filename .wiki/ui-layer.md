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
- Checks the PRO subscription status with `repository.checkProSubscription()` and appends `" ⭐ PRO"` to the toolbar title when true. The Pro gate uses the backend's pre-computed `has_premium_features` flag from `GET /api/v1/users/current`, so comped and organization Pro access is handled the same as a paid subscription.
- Hosts a bottom navigation bar with `Dashboard`, `Projects`, `Goals`, `Leaderboard`, and `Insights` items.
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

### Drill-down sections

| Pager Fragment | Stats Fragment | Focus |
|----------------|----------------|-------|
| `LanguagesPagerFragment` | `LanguagesStatsFragment` | Languages only |
| `ProjectsPagerFragment` | `ProjectsStatsFragment` | Projects only |
| `EditorsPagerFragment` | `EditorsStatsFragment` | Editors only |
| `InsightsPagerFragment` | `AiInsightsFragment` / `FocusFragment` | AI analytics and focus analytics |

Most drill-down pagers provide **Today / Last 7 Days / Last 30 Days** tabs. `InsightsPagerFragment` is also gated by PRO status.

### Files

`FilesFragment` lists recent file activity derived from heartbeats, grouped by file path and time spent. It is not currently wired to bottom navigation.

### Goals

`GoalsFragment` displays active coding goals; `CreateGoalDialogFragment` adds new ones. Data comes from `repository.getGoals()` / `repository.createGoal()` / `repository.deleteGoal()`.

### Leaderboard

`LeaderboardFragment` shows ranked users for a selected range and optional language filter.

### Insights

- `InsightsPagerFragment` hosts AI and focus analytics tabs.
- `AiInsightsFragment` renders AI-generated coding insights.
- `FocusFragment` shows focus-score and distraction analytics.

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

## PRO gating

`MainActivity` stores a boolean `isProUser` after `checkProSubscription()` and passes it to fragments that need it:

- `MainPagerFragment` — free users get 2 dashboard tabs; PRO users get 6.
- `InsightsPagerFragment` — shows a locked state for free users, AI + Focus tabs for PRO users.
- `LeaderboardFragment` — receives the flag (future use).

## Navigation

- Bottom navigation XML: `app/src/main/res/menu/bottom_navigation.xml`.
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
