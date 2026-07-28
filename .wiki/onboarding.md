---
type: onboarding
title: Onboarding
description: A first-day walkthrough of the Chronova Android repository, the local setup, and the conventions you must follow.
tags: [onboarding, setup, contributing]
---

# Onboarding

This page is the path of least resistance for a new contributor — human
or agent — joining the Chronova Android repository. Read it top to
bottom before opening your first PR.

## 1. Understand what the app is

Chronova Android is a Kotlin Android app for developer productivity
analytics. It talks to a Chronova server (default
`https://chronova.dev/`) and displays language usage, project time,
editor activity, file activity, real-time heartbeats, and (for PRO
users) AI/Focus insights. The full feature set is in the
[README](https://github.com/nx-solutions-ug/chronova-android#features).

The architecture overview ([Architecture Overview](./architecture/overview.md))
explains the custom MVVM + Repository pattern and the no-DI,
no-ViewModel design choice. Skim the
[Data Flow](./architecture/data-flow.md) page to see the actual
request sequences for login, dashboard, leaderboard, and goals.

## 2. Local setup

Required toolchain:

- **Android Studio Ladybug (2024.2.1)** or newer.
- **JDK 17** (`JAVA_HOME` must point at it).
- **Android SDK** with API 36 platform and build-tools 34.0.0.

Then:

```bash
git clone <repository-url>
cd chronova-android
chmod +x gradlew
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

Containerised alternative:

```bash
./docker-build.sh
# writes build-output/apk/debug/app-debug.apk
```

If you need a signed release APK use `./build.sh release`. The release
keystore is committed to the repo and the passwords are hardcoded in
`app/build.gradle` — fine for a one-off build, do not reuse for
anything sensitive.

For the full build environment and dependency list, see
[Build & Deploy](./build-and-deploy.md).

## 3. Mandatory conventions

Read [Development Conventions](./development-conventions.md) **before**
writing any code. The non-negotiable rules:

1. ViewBinding only — `findViewById` is forbidden.
2. `lifecycleScope` (activities) or `viewLifecycleOwner.lifecycleScope`
   (fragments) for all coroutines.
3. `Result<T>` return type for every repository method; never throw.
4. `_binding = null` in `onDestroyView()`.
5. Guard UI updates with `isAdded && _binding != null`.
6. Cancel any `Job` you store from `lifecycleScope.launch` in
   `onDestroyView()`.
7. No Hilt, Koin, or other DI framework — instantiate the repository
   directly with `ChronovaRepository(requireContext())`.
8. No unsafe casts or `@Suppress` shortcuts.

## 4. First contribution

### 4a. Get vouched

External contributors must be vouched before opening a PR. See
[Vouch System](./ci-cd/vouch-system.md) for the process. In short:
open a Discussion, get a maintainer to comment `!vouch`, and then
PRs will be accepted by `vouch-pr.yml`.

### 4b. Pick a small task

Good first tasks:

- Add a `Result<T>` method to `ChronovaRepository` for an existing
  endpoint.
- Add a new `*Adapter` + item layout for a list that currently uses a
  inline `SimpleInsightAdapter`-style helper.
- Improve empty states (`emptyStateText`) for one of the existing
  list fragments.

Look for issues labelled `good first issue` if any are open.

### 4c. Branch and commit

```bash
git checkout -b feature/<short-description>
# make your changes
./gradlew assembleDebug   # verify it still builds
./gradlew testDebugUnitTest
git commit -m "feat: short description"
git push origin feature/<short-description>
```

### 4d. Open the PR

The OMP agent will automatically label (`label-pr` job in
[omp-ci.yml](./ci-cd/omp-agent.md#omp-ciyml)) and review (`review-pr`)
your PR. A maintainer will perform the final review.

## 5. Code map to learn

Start by reading, in this order:

1. `app/src/main/java/com/chronova/app/MainActivity.kt` — entry point,
   bottom navigation, PRO detection.
2. `app/src/main/java/com/chronova/app/LoginActivity.kt` — both
   authentication paths.
3. `app/src/main/java/com/chronova/app/data/ChronovaRepository.kt` —
   the single source of truth.
4. `app/src/main/java/com/chronova/app/data/ApiClient.kt` — Retrofit
   singleton with dynamic base URL.
5. `app/src/main/java/com/chronova/app/ui/main/MainPagerFragment.kt` —
   ViewPager2 + TabLayout + PRO tab count.
6. `app/src/main/java/com/chronova/app/ui/main/MainStatsFragment.kt` —
   the dashboard card pipeline.
7. `app/src/main/java/com/chronova/app/ui/main/cards/CardsList.kt` and
   `viewholders/` — the heterogeneous RecyclerView card system.

That covers roughly 70% of the code you will touch day-to-day. Drill
into specific features (leaderboard, goals, insights) as needed.

## 6. Where to look next

| You want to… | Read |
|--------------|------|
| Add a new API endpoint | [Server API](./api/index.md) and [Data Layer](./data-layer.md). |
| Add a new screen | [UI Layer](./ui-layer.md) and [Development Conventions](./development-conventions.md). |
| Understand PRO gating | [Data Flow](./architecture/data-flow.md) § 3 and [UI Layer](./ui-layer.md) § Insights / Leaderboard. |
| Understand the CI/CD | [CI/CD & Automation](./ci-cd/index.md). |
| Update dependencies | [Renovate](./ci-cd/index.md#renovate) and the `update-*` PRs opened by Renovate. |
| Run the wiki pipeline locally | [Wiki Pipeline](./ci-cd/wiki-pipeline.md#local-preview). |
