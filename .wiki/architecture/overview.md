---
type: architecture
title: Architecture Overview
description: How Chronova Android is structured and how the layers interact.
tags: [architecture, mvvm, repository]
---

# Architecture Overview

Chronova Android uses a **custom MVVM + Repository Pattern**. It deliberately avoids Android Architecture Components `ViewModel` and dependency-injection frameworks such as Hilt or Koin.

## Layer diagram

```
┌─────────────────────────────────────┐
│           UI Layer                  │
│  Activities, Fragments, Adapters    │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│         Repository Layer            │
│     ChronovaRepository (SSOT)       │
│  - SharedPreferences management     │
│  - API calls                        │
│  - Data transformation              │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│           Data Layer                │
│  Retrofit + ChronovaApiService      │
│  ApiModels (DTOs)                   │
└─────────────────────────────────────┘
```

## Design decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| State holder | Fragment-level properties | No AAC `ViewModel`; state lives in the fragment and is recreated with it. |
| DI | Manual instantiation | `private val repository = ChronovaRepository(requireContext())` keeps the project simple. |
| Async | Kotlin coroutines + `lifecycleScope` | Scoped to the UI lifecycle; cancel jobs in `onDestroyView()`. |
| View access | **ViewBinding only** | Type-safe and null-safe; `findViewById` is forbidden. |
| Error handling | `Result<T>` from repository methods | Callers use `onSuccess` / `onFailure` explicitly. |

## Package layout

```
app/src/main/java/com/chronova/app/
├── MainActivity.kt              # Launcher, bottom nav, toolbar, logout
├── LoginActivity.kt             # Login or API-key authentication
├── data/
│   ├── ApiClient.kt             # Retrofit singleton
│   ├── ApiModels.kt             # Request/response DTOs
│   ├── ChronovaApiService.kt    # Retrofit interface
│   └── ChronovaRepository.kt    # Single source of truth
└── ui/
    ├── main/
    │   ├── MainPagerFragment.kt
    │   ├── MainStatsFragment.kt
    │   └── cards/               # Dashboard card adapter + view holders
    ├── DashboardFragment.kt
    ├── FilesFragment.kt
    ├── LanguagesPagerFragment.kt
    ├── LanguagesStatsFragment.kt
    ├── ProjectsContainerFragment.kt
    ├── ProjectsPagerFragment.kt
    ├── ProjectsStatsFragment.kt
    ├── EditorsPagerFragment.kt
    ├── EditorsStatsFragment.kt
    ├── GoalsFragment.kt
    ├── LeaderboardFragment.kt
    ├── InsightsPagerFragment.kt
    ├── AiInsightsFragment.kt
    ├── FocusFragment.kt
    ├── CreateGoalDialogFragment.kt
    └── *Adapter.kt              # RecyclerView adapters
```

## Fragment patterns

- **Pager fragment**: hosts `ViewPager2` + `TabLayout` with a `FragmentStateAdapter`. Used for dashboard (`MainPagerFragment`) and per-section drill-downs (`LanguagesPagerFragment`, `ProjectsPagerFragment`, `EditorsPagerFragment`).
- **Stats fragment**: one tab page inside a pager. It reads a `timeRange` argument created by `newInstance()` and loads data with `lifecycleScope`.
- **List fragment**: a simple `RecyclerView` backed by a repository call, e.g. `FilesFragment`.

## Lifecycle rules

1. Inflate binding in `onCreateView()`.
2. Set `_binding = null` in `onDestroyView()`.
3. Cancel any `Job` stored from `lifecycleScope.launch` in `onDestroyView()`.
4. Guard UI updates with `isAdded && _binding != null`.

See [UI Layer](../ui-layer.md) for fragment details and [Development Conventions](../development-conventions.md) for the mandatory patterns.
