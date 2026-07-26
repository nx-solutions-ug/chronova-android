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
- Hosts a bottom navigation bar with Dashboard, Projects, Goals, Leaderboard, and Insights items.
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

Each pager provides **Today / Last 7 Days / Last 30 Days** tabs.

### Goals

`GoalsFragment` displays a scrollable list of coding goals via `GoalAdapter`. Users can:
- Tap the FAB to open `CreateGoalDialogFragment` and create a new goal (`repository.createGoal()`).
- Swipe left or right on a goal to delete it (`repository.deleteGoal()`).

`repository.getGoalSuggestions()` can populate suggested goal targets.

### Leaderboard

`LeaderboardFragment` shows a ranked list of users from `repository.getLeaders()`. Range chips let users switch ranges. Free users can only select **7 Days**; PRO users also get **30 Days** and **90 Days**.

### Insights

`InsightsPagerFragment` hosts tabs for analytics features:
- **AI Insights** (`AiInsightsFragment`): contribution share (AI vs. manual), adoption timeline, efficiency trend, language matrix, and project dependency data from `repository.getAiAnalytics()`.
- **Focus** (`FocusFragment`): concentration score, deep-work blocks, context switches, and project distribution from `repository.getFocusAnalytics()`.

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

- **Dashboard** — tabbed stats overview (`MainPagerFragment` → `MainStatsFragment`).
- **Projects** — drill-down project stats (`ProjectsContainerFragment` → `ProjectsPagerFragment` → `ProjectsStatsFragment`).
- **Goals** — list, create, and swipe-to-delete coding goals (`GoalsFragment`, `CreateGoalDialogFragment`).
- **Leaderboard** — ranked leaderboard with range chips; some ranges are PRO-only (`LeaderboardFragment`).
- **Insights** — tabbed AI insights and focus analytics (`InsightsPagerFragment` → `AiInsightsFragment`, `FocusFragment`).
- Bottom navigation XML: `app/src/main/res/menu/bottom_navigation.xml`.
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
