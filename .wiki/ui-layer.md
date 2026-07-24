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
- Hosts a bottom navigation bar with five items: **Dashboard**, **Projects**, **Goals**, **Leaderboard**, and **Insights**.
- Toolbar menu provides **Logout**, which clears the API key and returns to `LoginActivity`.
- Passes the `is_pro_user` flag to fragments that need it (`MainPagerFragment`, `LeaderboardFragment`, `InsightsPagerFragment`).

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
- `DashboardFragment` is a legacy dashboard with a bar chart and recent activity list. It is not wired to bottom navigation.

### Drill-down sections

| Pager / Container Fragment | Child fragments | Focus |
|----------------|----------------|-------|
| `ProjectsContainerFragment` | `ProjectsPagerFragment` + `EditorsPagerFragment` | Projects and editors under a single bottom-nav tab |
| `LanguagesPagerFragment` | `LanguagesStatsFragment` | Languages only |
| `ProjectsPagerFragment` | `ProjectsStatsFragment` | Projects only |
| `EditorsPagerFragment` | `EditorsStatsFragment` | Editors only |
| `InsightsPagerFragment` | `AiInsightsFragment` + `FocusFragment` | AI analytics and focus analytics (PRO only) |

The language, project, and editor pagers provide **Today / Last 7 Days / Last 30 Days** tabs.

### Files

`FilesFragment` lists recent file activity derived from heartbeats, grouped by file path and time spent. It is currently not wired to bottom navigation; the bottom nav uses **Projects** instead.

### Goals

`GoalsFragment` shows a list of personal coding goals, supports swipe-to-delete, and uses `CreateGoalDialogFragment` to add new goals. It calls `repository.getGoals()`, `repository.createGoal(...)`, and `repository.deleteGoal(...)`.

### Leaderboard

`LeaderboardFragment` displays ranked coding leaders for a selected range. Free users only see **7 Days**; PRO users can switch to **30 Days** or **90 Days**. It highlights the current user and appends them to the top if they are not in the first page of results.

### AI Insights and Focus

Available under the **Insights** bottom-nav tab for PRO users only. Free users see a locked-state view.

- `AiInsightsFragment` renders AI vs. manual contribution share, adoption timeline, efficiency trend, per-language AI usage, and project dependency matrices.
- `FocusFragment` shows concentration score, context switches, deep-work blocks, and project distribution.

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
- Navigation is done imperatively with `FragmentManager.beginTransaction().replace(...)`. AndroidX Navigation dependencies are on the classpath but no navigation graph is used.

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
