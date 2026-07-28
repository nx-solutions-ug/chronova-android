---
type: glossary
title: Glossary
description: Domain terms and acronyms used throughout the codebase and this wiki.
tags: [glossary, terms, definitions]
---

# Glossary

A short, opinionated reference for terms that come up repeatedly in the
code, the wiki, and the discussion threads. Use this as a lookup when
something is unfamiliar.

## App / project

| Term | Meaning |
|------|---------|
| **Chronova** | The product name (server + Android app + sister services). |
| **Chronova server** | The REST API at `https://chronova.dev/`. Configurable per-user via `SharedPreferences["server_url"]`. |
| **Chronova Android** | The Kotlin app in this repository. Package `com.chronova.app`. |

## Authentication & users

| Term | Meaning |
|------|---------|
| **API key** | A bearer token stored in `SharedPreferences["api_key"]`. Sent as `Authorization: Bearer <key>`. |
| **user_id** | The server-side user id, stored in `SharedPreferences["user_id"]`, used to highlight the current user on the leaderboard. |
| **PRO user** | A user for whom `has_premium_features == true` on the server. PRO unlocks the full dashboard range, the leaderboard's 30/90-day chips, and the AI Insights / Focus tabs. |
| **has_premium_features** | The single server-computed field the client reads for PRO status. Already includes individual subscriptions, comped PRO, and organization subscriptions — the client does not re-derive it. |
| **PRO badge** | The `⭐ PRO` suffix appended to the toolbar title when the user is PRO. |

## Time ranges

The string tokens used as `range` query values and as fragment arguments:

| Token | Range | Who sees it |
|-------|-------|-------------|
| `today` | Today | Free + PRO |
| `last_7_days` | Last 7 days | Free + PRO |
| `last_30_days` | Last 30 days | Free + PRO (free only in drill-downs) |
| `last_3_months` | Last 3 months | PRO only |
| `last_year` | Last year | PRO only |
| `all_time` | All time | PRO only |
| `last_90_days` | Last 90 days | PRO only — leaderboard only |

## WakaTime compatibility

| Term | Meaning |
|------|---------|
| **WakaTime** | A popular coding-time tracker. The Chronova server API mirrors WakaTime's response shape, hence the `WakaTime*` class names in `ApiModels.kt`. |
| **`total_seconds`** | Seconds (not hours) — the canonical WakaTime field. The client converts to hours when rendering. |
| **`daily_stats`** | Per-day breakdown of `total_seconds` inside the WakaTime stats response. |
| **`operating_systems`** | WakaTime field included in the stats response but not currently surfaced in the UI. |

## Architecture

| Term | Meaning |
|------|---------|
| **MVVM** | The high-level pattern: UI observes data through a repository. |
| **Custom MVVM** | The project's deliberate choice **not** to use AAC `ViewModel`. State lives in the fragment itself. |
| **SSOT (Single Source of Truth)** | `ChronovaRepository`. Fragments and adapters do not cache state. |
| **Result\<T\>** | Kotlin's stdlib type used as the return for every repository method. Callers use `onSuccess` / `onFailure` (or `Result.fold`). |
| **ViewBinding** | Compile-time type-safe view accessors generated from XML. Mandated by the project. |
| **`_binding` / `binding`** | The nullable cache + non-null accessor pattern. `_binding` is cleared in `onDestroyView()`. |
| **lifecycleScope** | The only coroutine scope UI code is allowed to use. For fragments, `viewLifecycleOwner.lifecycleScope` is preferred to avoid leaks. |
| **Pager fragment** | A fragment hosting a `ViewPager2` with a `FragmentStateAdapter`. Examples: `MainPagerFragment`, `LanguagesPagerFragment`, `ProjectsPagerFragment`, `EditorsPagerFragment`, `ProjectsContainerFragment`, `InsightsPagerFragment`. |
| **Stats fragment** | A tab page inside a pager. Receives a `timeRange` argument from `newInstance(...)` and renders cards. |
| **Locked state** | The fallback view shown by `InsightsPagerFragment` when the user is not PRO. |

## Card dashboard

| Term | Meaning |
|------|---------|
| **`CardsList`** | A type-safe builder holding parallel `types: List<Int>` and `payloads: List<Any>` lists. |
| **`CardsAdapter`** | A heterogeneous `RecyclerView.Adapter<RecyclerView.ViewHolder>` that dispatches on the card type. |
| **Card types** | `TYPE_GLOBAL_SUMMARY` (0), `TYPE_PIE_CHART` (1), `TYPE_LINE_CHART` (2). |
| **ViewHolder** | `GlobalSummaryViewHolder`, `PieChartViewHolder`, `LineChartViewHolder` — bind the card payload. |

## Goals

| Term | Meaning |
|------|---------|
| **Goal** | A server-side coding goal with a target `seconds` and a `delta` (`day`, `week`). |
| **Goal suggestion** | Pre-defined goal templates loaded by `getGoalSuggestions()` and rendered as chips in the create-goal dialog. |
| **Coding time goal** | The default goal type (`type = "coding_time"`). |

## CI/CD

| Term | Meaning |
|------|---------|
| **OMP** | The LLM-driven automation agent ("opencode agent"). Installed in CI; runs triage, label, and review jobs. Model: `ollama-cloud/minimax-m3`. |
| **OMP commands** | Markdown prompt templates in `.omp/commands/`. `$ARGUMENTS` is replaced at runtime. |
| **Vouch system** | PR gate. External contributors must be vouched by a maintainer in a Discussion before opening a PR. Implemented with `mitchellh/vouch` and `.github/VOUCHED.td`. |
| **Wiki agent** | `@chronova/wiki-agent`. Runs in `update-wiki.yml` and writes to `.wiki/`. |
| **Wiki pipeline** | The flow that flattens `.wiki/`, opens a `wiki/staging-<timestamp>` PR, and pushes to the GitHub wiki repo. |
| **Staging PR** | PR on a branch named `wiki/staging-<unix-timestamp>`. The wiki agent uses the timestamp to detect stale PRs. |
| **Renovate** | Dependency update bot. Opens `chore(deps):` and `fix(deps):` PRs. Reviewed by the OMP `review-pr` job. |
| **Release Drafter** | Workflow that aggregates merged PRs into the next release notes by label category. |
| **`chronova-agent` App** | The GitHub App used by every workflow for auth. Credentials: `APP_CLIENT_ID` + `APP_PRIVATE_KEY` secrets. |

## Build / release

| Term | Meaning |
|------|---------|
| **AGP** | Android Gradle Plugin. Currently `8.13.2`. |
| **`org.gradle.caching=false`** | Build cache is intentionally disabled in `gradle.properties` so every build is fresh. |
| **`FAIL_ON_PROJECT_REPOS`** | The `RepositoriesMode` set in `settings.gradle` — module-level repository declarations are an error. |
| **Release keystore** | `chronova-release-key.keystore` in the repo root. Passwords `chronova123` for both store and key. Not safe for production reuse. |
| **`minifyEnabled = false`** | The release build does not run R8/ProGuard. `proguard-rules.pro` exists but is inactive. |
