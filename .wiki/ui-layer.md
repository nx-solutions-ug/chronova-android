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
- Hosts a bottom navigation bar with five items: `Dashboard`, `Projects`, `Goals`, `Leaderboard`, and `Insights`.
- Toolbar menu provides **Logout**, which clears the API key and returns to `LoginActivity`.

### `LoginActivity`

- Allows two authentication paths:
  1. **Email + password** → calls `repository.login()` and saves the returned `apiKey` and `user_id`.
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
- `DashboardFragment` is a separate older dashboard with a bar chart and recent activity list. It is not currently wired to bottom navigation (the main dashboard is served by `MainPagerFragment` → `MainStatsFragment`).

### Drill-down sections

| Pager Fragment | Stats Fragment | Focus |
|----------------|----------------|-------|
| `LanguagesPagerFragment` | `LanguagesStatsFragment` | Languages only |
| `ProjectsPagerFragment` | `ProjectsStatsFragment` | Projects only |
| `EditorsPagerFragment` | `EditorsStatsFragment` | Editors only |

Each pager provides **Today / Last 7 Days / Last 30 Days** tabs.

### Projects container

`ProjectsContainerFragment` hosts `ProjectsPagerFragment` and `EditorsPagerFragment` as sub-tabs under the **Projects** bottom navigation item. This makes the previously built Editors section reachable.

### Goals

`GoalsFragment` displays the user's coding goals as a RecyclerView. It supports:

- loading goals from `repository.getGoals()`,
- creating goals via `CreateGoalDialogFragment` (POST to `repository.createGoal(...)`),
- swipe-to-delete (DELETE via `repository.deleteGoal(goalId)`),
- empty-state and progress indicators.

Goal suggestions can be fetched with `repository.getGoalSuggestions()`.

### Leaderboard

`LeaderboardFragment` shows a ranked list of users returned by `repository.getLeaders(range, ...)`. Features include:

- range chips: **7 Days** for everyone; **30 Days** and **90 Days** only for PRO users,
- current-user row highlighting using the persisted `user_id`,
- the current user is pinned at the top when not on the first page.

### Insights

`InsightsPagerFragment` is PRO-gated: non-PRO users see a locked overlay. PRO users see two sub-tabs:

- **AI Insights** (`AiInsightsFragment`) — AI vs. manual contribution share, adoption timeline, efficiency trend, language matrix, and project dependency.
- **Focus** (`FocusFragment`) — concentration score, deep-work blocks, context switches, and project distribution.

Both fragments load from `repository.getAiAnalytics(range)` and `repository.getFocusAnalytics(range)` respectively.

### Files

`FilesFragment` lists recent file activity derived from heartbeats, grouped by file path and time spent.

## Card dashboard

`MainStatsFragment` uses a heterogeneous RecyclerView built from:

- `CardsList.kt` — type-safe builder for global summary, pie chart, and line chart items.
- `CardsAdapter.kt` — multi-view-type adapter.
- `GlobalSummaryViewHolder`, `PieChartViewHolder`, `LineChartViewHolder` — bind each card.

## Charts

Charts are rendered with [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart):

- **Pie charts** for language/project/editor distribution and AI contribution share.
- **Line charts** for daily activity trends and AI efficiency trend.
- **Bar charts** in `DashboardFragment`.

## Navigation

- Bottom navigation XML: `app/src/main/res/menu/bottom_navigation.xml`.
- Main toolbar menu: `app/src/main/res/menu/main_menu.xml`.
- Navigation is done imperatively with `FragmentManager.beginTransaction().replace(...)`. There is no AndroidX Navigation component graph.

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
