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
- Reads an `is_pro_user` argument/flag and passes it to fragments that need to gate PRO features.

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

### Projects container

`ProjectsContainerFragment` is a tabbed container that combines the older per-category drill-downs:

| Tab | Fragment | Focus |
|-----|----------|-------|
| Projects | `ProjectsPagerFragment` → `ProjectsStatsFragment` | Projects only |
| Editors | `EditorsPagerFragment` → `EditorsStatsFragment` | Editors only |

`LanguagesPagerFragment` / `LanguagesStatsFragment` are also retained in the source tree but are not currently wired to bottom navigation.

Each stats pager provides **Today / Last 7 Days / Last 30 Days** tabs.

### Goals

`GoalsFragment` lists user-defined coding goals. It supports:

- Creating a goal through `CreateGoalDialogFragment`.
- Deleting a goal with a swipe gesture or a tap action.
- Empty-state handling when no goals exist.

### Leaderboard

`LeaderboardFragment` shows a ranked list of users. Free users see only **Last 7 Days**; PRO users can also choose **30 Days** and **90 Days**. The current user is highlighted when present.

### Insights (PRO only)

`InsightsPagerFragment` is only available to PRO users; non-PRO users see a locked-state view. It hosts two tabs:

| Tab | Fragment | Purpose |
|-----|----------|---------|
| AI Insights | `AiInsightsFragment` | AI vs. manual contribution pie chart, adoption timeline, efficiency trend, language matrix, project dependency |
| Focus | `FocusFragment` | Concentration score, deep-work blocks, context switches, project distribution |

Both fragments expose range chips (7 Days, 30 Days, etc.) backed by the repository's analytics methods.

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
- **Pie charts** in `AiInsightsFragment` and `FocusFragment`.
- **Line charts** in `AiInsightsFragment` for AI adoption and efficiency trends.

## Navigation

- Main toolbar menu: `app/src/main/res/menu/main_menu.xml`.
- Bottom navigation menu: `app/src/main/res/menu/bottom_navigation.xml`.
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
