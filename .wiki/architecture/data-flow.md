---
type: architecture
title: Data Flow & Sequences
description: End-to-end traces of the major user flows: login, dashboard load, leaderboard, goals, and PRO-gated tabs.
tags: [architecture, data-flow, sequences, coroutines]
---

# Data Flow & Sequences

This page complements [Architecture Overview](./overview.md) by tracing the
exact call sequences for the most important user journeys. Every async call
goes through `ChronovaRepository` on `lifecycleScope`, and every screen renders
its data after a `Result<T>` arrives.

## Layers and contracts

```
UI (Fragment / Activity)
   │   lifecycleScope.launch { ... }
   ▼
ChronovaRepository       (Result<T> wrapper, prefs, transformation)
   │   suspend fun
   ▼
ChronovaApiService       (Retrofit interface)
   │
   ▼
ApiClient (Retrofit) → OkHttp → Chronova server
   ▲
   │   updates base URL when the user saves a new server URL
   │
ChronovaRepository.saveServerUrl(...)
```

A few invariants that hold across **every** flow:

- `lifecycleScope` (activities) or `viewLifecycleOwner.lifecycleScope`
  (fragments) is the only allowed scope. No `GlobalScope`, no raw executors.
- Repository methods are **suspend** and return `Result<T>`. They never throw.
- UI updates after `Result.fold` are guarded with `isAdded && _binding != null`.
- A `Job` captured at launch is cancelled in `onDestroyView()`.

## 1. First launch → login

`MainActivity` is the launcher. If no API key is stored, it navigates to
`LoginActivity` and finishes itself. The login screen accepts two paths.

### 1a. Email + password

```
LoginActivity
   └─ lifecycleScope.launch
        └─ repository.login(email, password)
             └─ apiService.login(LoginRequest)
                  └─ POST https://chronova.dev/api/auth/login
        .onSuccess { resp ->
            prefs.putString("api_key", resp.apiKey)
            prefs.putString("user_id", resp.user.id)
            startActivity(MainActivity); finish()
        }
        .onFailure { e -> Toast.makeText(...) }
```

### 1b. API key

```
LoginActivity
   └─ repository.saveApiKey(apiKey)
   └─ startActivity(MainActivity); finish()
```

If the user enters a non-default server URL, `repository.saveServerUrl(url)`
both writes to `SharedPreferences` **and** calls
`ApiClient.updateBaseUrl(url)`, which invalidates the cached `Retrofit`
instance. The next repository call will use the new base URL.

## 2. Dashboard load (free vs. PRO)

`MainActivity.onCreate` always starts with a `MainPagerFragment`. The pager
shows a different number of tabs depending on the PRO status that arrives
asynchronously from `checkProSubscription()`.

```
MainActivity.onCreate
   └─ if (!repository.isAuthenticated()) → LoginActivity
   └─ lifecycleScope.launch { checkProSubscription() }   // async
   └─ replaceFragment(MainPagerFragment(isProUser=false)) // initial
        (later notified via updateProStatus(true|false))

MainPagerFragment
   └─ ViewPager2 + TabLayoutMediator
        ├─ free:  2 tabs (Today, Last 7 Days)
        └─ PRO:   6 tabs (…, Last 3 Months, Last Year, All Time)

MainStatsFragment (one per tab)
   └─ lifecycleScope.launch
        └─ repository.getStatsForRange("last_7_days" | ...)
             └─ GET api/v1/users/current/stats/{range}
        .onSuccess { stats ->
            val cards = CardsList()
                .addGlobalSummary(stats.totalSeconds, range)
                .addPieChart("Top 5 Languages", stats.languages)
                .addPieChart("Top 5 Projects", stats.projects)
                .addPieChart("Top 5 Editors", stats.editors)
                .addLineChart("Activity", stats.dailyActivity)
            cardsAdapter.updateCards(cards)
        }
```

`MainStatsFragment`, `LanguagesStatsFragment`, `ProjectsStatsFragment`, and
`EditorsStatsFragment` all reuse the same `FragmentMainStatsBinding` layout
and `CardsAdapter`; they differ only in which card categories they add.

## 3. PRO status update (post-subscription fetch)

```
MainActivity.checkProSubscription
   └─ repository.checkProSubscription()
        └─ GET api/v1/users/current
        .onSuccess { isPro ->
            isProUser = isPro
            updateTitleWithProBadge("Dashboard")
            notifyFragmentOfProStatus(isPro)
        }

MainActivity.notifyFragmentOfProStatus
   └─ (fragment as MainPagerFragment).updateProStatus(isPro)
        └─ MainPagerFragment recreates its adapter
           → pager now has 2 or 6 tabs
```

`InsightsPagerFragment` and `LeaderboardFragment` use the same mechanism
through their `is_pro_user` argument.

## 4. Insights (PRO-gated)

```
MainActivity
   └─ if nav_insights → InsightsPagerFragment(is_pro_user)
        ├─ isPro:    ViewPager2 with AiInsightsFragment + FocusFragment
        └─ !isPro:   hide pager, show lockedContainer

AiInsightsFragment
   └─ repository.getAiAnalytics(range)         // GET .../analytics/ai?range=
   └─ PieChart (AI vs Manual) + 2× LineChart + 2× RecyclerView

FocusFragment
   └─ repository.getFocusAnalytics(range)      // GET .../analytics/focus?range=
   └─ concentration score + context switches + deep work blocks
```

## 5. Leaderboard

```
LeaderboardFragment
   └─ chip group (7d only for free; 7d/30d/90d for PRO)
   └─ repository.getLeaders(range, language=null, page=1)
        └─ GET api/v1/leaders?range=...&language=...&page=...
   └─ prepends the current user (from prefs["user_id"]) if not in the first page
```

## 6. Goals

```
GoalsFragment
   └─ repository.getGoals()                       // GET .../goals
   └─ RecyclerView (GoalAdapter) with ItemTouchHelper for swipe-to-delete
   └─ FAB → CreateGoalDialogFragment (BottomSheetDialogFragment)
        └─ repository.getGoalSuggestions()        // GET .../goals/suggestions
        └─ repository.createGoal(request)         // POST .../goals
   └─ repository.deleteGoal(goal.id)              // DELETE .../goals?id=...
```

The dialog re-uses the parent fragment's `onGoalCreated` callback so the
list reloads after a successful create.

## 7. Files (recent activity)

```
FilesFragment
   └─ repository.getFileActivity(perPage = 50)
        └─ GET api/v1/users/current/heartbeats?per_page=50
   └─ Repository groups heartbeats by file
   └─ FileAdapter renders rows sorted by time spent
```

## Cancellation rules

| Where | What to cancel |
|-------|----------------|
| Every Fragment | `_binding = null` in `onDestroyView()`. |
| Fragments that store a `Job` | Cancel it in `onDestroyView()` and null it out. Example: `MainStatsFragment.loadDataJob`. |
| The pager | `MainPagerFragment` already handles `updateProStatus`; no extra cancel is needed because the pager adapter is recreated. |
| Long-lived dialogs | `CreateGoalDialogFragment` uses `viewLifecycleOwner.lifecycleScope`, so it cancels when the dialog view is destroyed. |

See [Development Conventions](../development-conventions.md) for the exact
patterns and [UI Layer](../ui-layer.md) for the screen-by-screen breakdown.
