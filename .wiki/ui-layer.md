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
  - PRO gating includes the backend's `isProComped` short-circuit (see [Data Layer](./data-layer.md#pro-subscription-check)).
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

### Projects container

`ProjectsContainerFragment` combines **Projects** and **Editors** under a single tabbed container:
- tab 0 → `ProjectsPagerFragment`
- tab 1 → `EditorsPagerFragment`

### Drill-down sections

| Pager Fragment | Stats Fragment | Focus |
|----------------|----------------|-------|
| `LanguagesPagerFragment` | `LanguagesStatsFragment` | Languages only |
| `ProjectsPagerFragment` | `ProjectsStatsFragment` | Projects only |
| `EditorsPagerFragment` | `EditorsStatsFragment` | Editors only |

Each pager provides **Today / Last 7 Days / Last 30 Days** tabs.

### Goals

`GoalsFragment` lists the current user's coding goals in a `RecyclerView`, supports swipe-to-delete, and opens `CreateGoalDialogFragment` (a `BottomSheetDialogFragment`) to add goals with optional AI-generated suggestions.

### Leaderboard

`LeaderboardFragment` shows ranked coding time leaders with range chips. PRO users can select **7 / 30 / 90 Days**; free users are limited to **7 Days**.

### Insights (PRO)

`InsightsPagerFragment` is gated on the same `isProUser` flag. PRO users see two tabs:
- **AI Insights** (`AiInsightsFragment`) — AI vs. manual coding share, adoption timeline, efficiency trend, language matrix, and project dependency.
- **Focus** (`FocusFragment`) — concentration score, context switches, deep-work blocks, and project distribution.

Free users see a locked state instead of the pager.

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
- **Line charts** for daily activity, AI adoption, and AI efficiency trends.
- **Bar charts** in `DashboardFragment`.

## Navigation

- Bottom navigation XML: `app/src/main/res/menu/bottom_navigation.xml` (items: Dashboard, Projects, Goals, Leaderboard, Insights).
- Main toolbar menu: `app/src/main/res/menu/main_menu.xml`.
- There is currently no AndroidX Navigation component graph; navigation is done imperatively with `FragmentManager.beginTransaction().replace(...)`.
- `MainActivity` passes `is_pro_user` via `Bundle` arguments to `MainPagerFragment`, `LeaderboardFragment`, and `InsightsPagerFragment`.
- `MainActivity` notifies the current `MainPagerFragment` of asynchronous PRO status changes via `MainPagerFragment.updateProStatus(Boolean)`.

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
