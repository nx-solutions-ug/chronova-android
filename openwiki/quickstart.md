# Chronova Android — OpenWiki

OpenWiki documentation for **Chronova Android**, the mobile client for the
[Chronova](https://chronova.dev) developer-productivity analytics service.

> Start here, then follow the links at the bottom for deeper notes.

## What this app is

Chronova Android is a Kotlin/Gradle Android app that talks to a Chronova
backend and visualises coding activity:

- Dashboard with summary, pie charts, and daily-activity line chart
- Languages, Projects, Editors, and Files tabs with time-range filters
- Today / 7-day / 30-day (and Pro: 3-month / Year / All Time) ranges
- Email-password or API-key sign-in against a configurable server URL
- Offline cache of credentials and server URL in `SharedPreferences`

The app follows a small, hand-rolled MVVM with a single `ChronovaRepository`
acting as the source of truth for both networking and preferences — there is
no DI framework and no Architecture Components `ViewModel`.

## Repository layout

```
chronova-android/
├── app/                                 # Android module
│   ├── build.gradle                     # Module build script
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml          # MainActivity + LoginActivity
│       ├── java/com/chronova/app/
│       │   ├── LoginActivity.kt         # Sign-in / API-key screen
│       │   ├── MainActivity.kt          # Auth gate + bottom nav host
│       │   ├── data/                    # Network + repository layer
│       │   │   ├── ApiClient.kt         # Retrofit singleton
│       │   │   ├── ApiModels.kt         # Gson DTOs
│       │   │   ├── ChronovaApiService.kt# Retrofit interface
│       │   │   └── ChronovaRepository.kt# SSOT (prefs + API)
│       │   └── ui/                      # Activities, Fragments, Adapters
│       │       ├── DashboardFragment.kt
│       │       ├── ProjectsFragment.kt, ProjectsPagerFragment.kt,
│       │       │   ProjectsStatsFragment.kt, ProjectAdapter.kt
│       │       ├── LanguagesFragment.kt, LanguagesPagerFragment.kt,
│       │       │   LanguagesStatsFragment.kt, LanguageAdapter.kt
│       │       ├── EditorsFragment.kt, EditorsPagerFragment.kt,
│       │       │   EditorsStatsFragment.kt, EditorAdapter.kt
│       │       ├── FilesFragment.kt, FileAdapter.kt
│       │       ├── ActivityAdapter.kt
│       │       └── main/                # Dashboard tab host
│       │           ├── MainPagerFragment.kt
│       │           ├── MainStatsFragment.kt
│       │           └── cards/           # Card list UI
│       │               ├── CardsAdapter.kt
│       │               ├── CardsList.kt
│       │               └── viewholders/
│       │                   ├── GlobalSummaryViewHolder.kt
│       │                   ├── PieChartViewHolder.kt
│       │                   └── LineChartViewHolder.kt
│       └── res/                         # Layouts, drawables, themes, strings
├── build.gradle                         # Root build script
├── settings.gradle
├── gradle.properties
├── gradlew, gradle/wrapper/             # Gradle wrapper
├── Dockerfile.build                     # Containerised APK build
├── build.sh                             # Host build helper
├── docker-build.sh                      # Docker build helper
├── docs/                                # Additional human docs
│   ├── build-apk.md
│   ├── quick-apk-build.md
│   ├── build-status.md
│   └── SYNC-FIX.md
├── .github/workflows/                   # CI: build, release, omp, openwiki
├── .omp/                                # OMP agent commands/config
├── AGENTS.md                            # AI-agent quick reference
├── README.md
└── LICENSE                              # MIT
```

## Build and run

### Prerequisites

- JDK 17
- Android SDK with API 36 (compile/target) and a device on API 24+
- Android Studio Ladybug (2024.2.1) or newer (optional)

### Quick local build

```bash
./gradlew assembleDebug                 # app/build/outputs/apk/debug/app-debug.apk
./gradlew assembleRelease               # app/build/outputs/apk/release/app-release.apk
```

The release config signs with the in-repo `chronova-release-key.keystore`
(alias `chronova`, passwords live in `app/build.gradle`).

Convenience wrappers:

- `./build.sh` (host) — wraps Gradle, copies the APK to `../public/downloads/`.
- `./docker-build.sh` — builds the APK inside a container defined by
  `Dockerfile.build` and writes it under `build-output/`.

### Install

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## First-run flow

1. `LoginActivity` loads the saved server URL (default `https://chronova.dev/`)
   from `SharedPreferences` (`server_url` key).
2. The user signs in with email+password (calls `POST api/auth/login`) or
   pastes an API key directly. The API key (or the one returned by login) is
   persisted under the `api_key` key and used as a `Bearer` token.
3. `MainActivity` checks `repository.isAuthenticated()`; if not, it routes
   to `LoginActivity`. Otherwise it initialises the Retrofit base URL,
   fetches the current user to detect Pro status, and shows the dashboard
   `MainPagerFragment` with tabs.
4. Bottom navigation (`R.id.nav_dashboard`, `R.id.nav_languages`) swaps
   fragments inside the `R.id.fragment_container`.

## Tech stack at a glance

| Area | Choice |
|---|---|
| Language | Kotlin 2.1.20 (JVM 17) |
| Min / Target SDK | 24 / 36 |
| Build | Gradle 8.13.2 |
| HTTP | Retrofit 3.0.0 + Gson + OkHttp logging 5.3.2 |
| Charts | MPAndroidChart 3.1.0 |
| UI | Material 1.13.0, ConstraintLayout 2.2.1, RecyclerView 1.4.0, ViewPager2 1.1.0 |
| Lifecycle | androidx.lifecycle 2.10.0 (no `ViewModel`) |
| Storage | `SharedPreferences` via `preference-ktx` 1.2.1 |

See `app/build.gradle` for the full dependency list.

## Where to look next

- [Architecture](architecture.md) — MVVM, repository, API, data flow.
- [Development](development.md) — coding patterns, build, CI, troubleshooting.
- [Project README](../README.md) and `docs/` — original human docs.
- [AGENTS.md](../AGENTS.md) — quick reference for AI agents working in this repo.
