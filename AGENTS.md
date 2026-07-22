# Repository Guidelines

Chronova Android is a Kotlin Android app for developer productivity analytics. It connects to a Chronova server (REST API, default `https://chronova.dev/`) to display coding statistics — language usage, project time, editor activity, file activity, and real-time heartbeats — across multiple time ranges (Today, 7 Days, 30 Days).

## Architecture & Data Flow

Custom MVVM + Repository pattern. No DI framework, no AAC ViewModel — Fragments hold state directly.

```
┌─────────────────────────────────────┐
│           UI Layer                  │
│  Activities, Fragments, Adapters    │
│  (ViewBinding, lifecycleScope)      │
└──────────────┬──────────────────────┘
               │ lifecycleScope.launch { repository.xxx() }
┌──────────────▼──────────────────────┐
│       Repository Layer              │
│   ChronovaRepository (SSOT)         │
│   - SharedPreferences (auth, URL)   │
│   - API calls via Retrofit          │
│   - Response → UI model transform   │
│   - Returns Result<T>               │
└──────────────┬──────────────────────┘
               │ suspend fun
┌──────────────▼──────────────────────┐
│           Data Layer                 │
│  ApiClient (Retrofit singleton)     │
│  ChronovaApiService (interface)      │
│  ApiModels (DTOs with @Gson)        │
└─────────────────────────────────────┘
```

**Entry flow**: `MainActivity` (launcher) checks auth → if not authenticated, navigates to `LoginActivity` → user enters server URL + API key or email/password → token saved to SharedPreferences → returns to `MainActivity` → `BottomNavigationView` hosts dashboard fragments.

**Fragment hierarchy**:
- `MainPagerFragment` (ViewPager2 + TabLayout) → `MainStatsFragment` (dashboard cards)
- `*PagerFragment` (e.g., `LanguagesPagerFragment`) → `*StatsFragment` (time-range-specific stats)
- Simple list fragments: `FilesFragment` (RecyclerView)

**Card system**: `CardsAdapter` renders heterogeneous dashboard cards via `CardsList` (defines card types: `TYPE_GLOBAL_SUMMARY`, `TYPE_PIE_CHART`, `TYPE_LINE_CHART`) and ViewHolders (`GlobalSummaryViewHolder`, `PieChartViewHolder`, `LineChartViewHolder`) using MPAndroidChart.

## Key Directories

| Directory | Purpose |
|-----------|---------|
| `app/src/main/java/com/chronova/app/data/` | API client, service interface, DTOs, repository |
| `app/src/main/java/com/chronova/app/ui/` | Fragments, adapters (dashboard, languages, projects, editors, files) |
| `app/src/main/java/com/chronova/app/ui/main/` | Main pager + stats fragments |
| `app/src/main/java/com/chronova/app/ui/main/cards/` | Dashboard card system (adapter, card list, viewholders) |
| `app/src/main/res/layout/` | 19 XML layouts (activities, fragments, item views) |
| `app/src/main/res/drawable/` | Icons and drawables |
| `.github/workflows/` | CI/CD (build, release, OMP agent, auto-manage) |
| `.omp/` | OMP agent config, rules, command templates |
| `.wiki/` | Architecture and quickstart documentation |
| `docs/` | Build guides and troubleshooting |

## Development Commands

```bash
# Build
./gradlew assembleDebug          # Debug APK → app/build/outputs/apk/debug/app-debug.apk
./gradlew assembleRelease        # Release APK (signed with included keystore)
./gradlew testDebugUnitTest      # Unit tests (no tests exist yet)
./gradlew clean                  # Clean build outputs

# Scripts
./build.sh                       # Build + copy APK to ../public/downloads/
./build.sh release               # Release build variant
./docker-build.sh                # Docker build → build-output/apk/debug/

# Install
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Code Conventions & Common Patterns

### ViewBinding (Mandatory)

Never use `findViewById`. Always clear `_binding` in `onDestroyView()`:

```kotlin
class MyFragment : Fragment(R.layout.fragment_my) {
    private var _binding: FragmentMyBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater, container, savedInstanceState): View {
        _binding = FragmentMyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // CRITICAL: prevents memory leaks
    }
}
```

### Repository Access (No DI)

```kotlin
private val repository = ChronovaRepository(requireContext())
```

### Async with Result\<T\>

All repository methods return `Result<T>`. Use `lifecycleScope` for coroutines:

```kotlin
lifecycleScope.launch {
    repository.getStatsForRange(timeRange)
        .onSuccess { data -> /* update UI */ }
        .onFailure { error -> Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show() }
}
```

### Fragment Factory Pattern

```kotlin
companion object {
    fun newInstance(timeRange: String) = MyFragment().apply {
        arguments = Bundle().apply { putString("timeRange", timeRange) }
    }
}
```

### ViewPager2 + TabLayout

```kotlin
val adapter = object : FragmentStateAdapter(this) {
    override fun createFragment(position: Int) = when(position) {
        0 -> StatsFragment.newInstance("Today")
        1 -> StatsFragment.newInstance("Last 7 Days")
        2 -> StatsFragment.newInstance("Last 30 Days")
        else -> throw IllegalArgumentException()
    }
    override fun getItemCount() = 3
}
binding.viewPager.adapter = adapter
TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, pos ->
    tab.text = when(pos) { 0 -> "Today"; 1 -> "7 Days"; else -> "30 Days" }
}.attach()
```

### Adding an API Endpoint

```kotlin
// In ChronovaApiService.kt
@GET("api/new-endpoint")
suspend fun newEndpoint(@Header("Authorization") apiKey: String): NewResponse

// In ChronovaRepository.kt
suspend fun getNewData(): Result<NewData> = try {
    val response = apiService.newEndpoint(apiKey)
    Result.success(transformResponse(response))
} catch (e: Exception) {
    Result.failure(e)
}
```

### Naming Conventions

- **Packages**: `com.chronova.app.data` (data layer), `com.chronova.app.ui` (UI), `com.chronova.app.ui.main.cards` (nested)
- **Classes**: PascalCase — `LanguagesPagerFragment`, `LanguageAdapter`, `PieChartViewHolder`
- **Layouts**: snake_case — `fragment_languages_pager.xml`, `item_language.xml`
- **SharedPreferences keys**: snake_case — `api_key`, `server_url`

### Critical Rules

1. Always use ViewBinding — never `findViewById`
2. Always use `lifecycleScope` for coroutines in Fragments/Activities
3. Always return `Result<T>` from Repository methods
4. Always clear `_binding` in `onDestroyView()`
5. Guard UI updates with `isAdded && _binding != null`
6. Cancel jobs in `onDestroyView()` (e.g., `loadDataJob?.cancel()`)

## Important Files

| File | Role |
|------|------|
| `app/src/main/java/com/chronova/app/LoginActivity.kt` | Auth entry — email/password or API key login |
| `app/src/main/java/com/chronova/app/MainActivity.kt` | Hosts BottomNavigationView, checks auth + PRO status |
| `app/src/main/java/com/chronova/app/data/ApiClient.kt` | Retrofit singleton with OkHttp logging; `updateBaseUrl()` for custom servers |
| `app/src/main/java/com/chronova/app/data/ChronovaRepository.kt` | SSOT — SharedPreferences, API calls, data transformation, `Result<T>` |
| `app/src/main/java/com/chronova/app/data/ChronovaApiService.kt` | Retrofit interface — `login`, `getStats`, `getHeartbeats`, `getProjects` |
| `app/src/main/java/com/chronova/app/data/ApiModels.kt` | DTOs with Gson serialization annotations |
| `app/src/main/java/com/chronova/app/ui/main/cards/CardsAdapter.kt` | Heterogeneous RecyclerView adapter for dashboard cards |
| `app/src/main/java/com/chronova/app/ui/main/cards/CardsList.kt` | Card type definitions and payload model |
| `app/src/main/AndroidManifest.xml` | Permissions: `INTERNET`, `ACCESS_NETWORK_STATE`; `usesCleartextTraffic=true` |
| `app/build.gradle` | Dependencies, build types, signing config, ViewBinding enabled |
| `app/proguard-rules.pro` | ProGuard rules for Retrofit/Gson (note: `minifyEnabled=false` in release, so rules are inactive) |

## Runtime/Tooling Preferences

| Requirement | Value |
|-------------|-------|
| **Language** | Kotlin 2.1.20 |
| **JDK** | 17 (source + target compatibility) |
| **Gradle** | 9.2.1 (wrapper) — note: README says 8.13.2, that's the AGP version |
| **AGP** | 8.13.2 |
| **Min SDK** | 24 (Android 7.0) |
| **Target/Compile SDK** | 36 (Android 16) |
| **ViewBinding** | Enabled |
| **Build cache** | Disabled (`org.gradle.caching=false`, `settings.gradle` local cache off) — ensures fresh builds |
| **Repository mode** | `FAIL_ON_PROJECT_REPOS` — all deps declared in `settings.gradle` |
| **JitPack** | Required for MPAndroidChart (`maven { url 'https://jitpack.io' }`) |
| **Signing** | Release keystore included in repo (`chronova-release-key.keystore`), passwords hardcoded in `app/build.gradle` |
| **Android Studio** | Ladybug (2024.2.1) or newer |

### Key Dependencies

| Category | Library | Version |
|----------|---------|---------|
| Networking | Retrofit + Gson converter | 3.0.0 |
| HTTP logging | OkHttp logging-interceptor | 5.3.2 |
| Charts | MPAndroidChart (JitPack) | 3.1.0 |
| Navigation | AndroidX Navigation KTX | 2.9.6 |
| Lifecycle | ViewModel + LiveData KTX | 2.10.0 / 2.9.4 |
| UI | Material Design | 1.13.0 |
| Lists | RecyclerView | 1.4.0 |
| Paging | ViewPager2 | 1.1.0 |
| Storage | Preference KTX | 1.2.1 |

## Testing & QA

**No tests exist.** Test dependencies are declared but `app/src/test/` and `app/src/androidTest/` are empty.

| Framework | Scope | Dependency |
|-----------|-------|------------|
| JUnit 4.13.2 | Unit tests (`src/test/`) | `testImplementation 'junit:junit:4.13.2'` |
| AndroidX Test JUnit 1.3.0 | Instrumented tests (`src/androidTest/`) | `androidTestImplementation 'androidx.test.ext:junit:1.3.0'` |
| Espresso 3.7.0 | UI tests | `androidTestImplementation 'androidx.test.espresso:espresso-core:3.7.0'` |

**Test runner**: `androidx.test.runner.AndroidJUnitRunner` (configured in `app/build.gradle`).

**No static analysis configured**: No ktlint, detekt, or custom lint.xml. Default Android lint applies. ProGuard rules exist but `minifyEnabled=false` means they're inactive.

## CI/CD

GitHub Actions workflows in `.github/workflows/`:

| Workflow | Trigger | Purpose |
|----------|---------|---------|
| `build.yml` | Manual (`workflow_dispatch`) | JDK 17 + Android SDK setup, `testDebugUnitTest`, `assembleDebug`, `assembleRelease`, uploads APK artifacts (7-day debug, 30-day release retention) |
| `omp-ci.yml` | Issues opened, PR opened/synchronize/ready_for_review | OMP agent triages issues, labels PRs (type + priority), reviews PRs. Skips re-review for agent-authored commits. Uses model `ollama-cloud/minimax-m3` |
| `omp.yml` | Issue/PR comments containing `/omp` | OMP agent execution on-demand via comments. Expands `.omp/commands/*.md` templates |
| `auto-manage.yml` | Issues opened/reopened, PRs opened | Tags issues `needs-triage`, auto-assigns to `niklasschaeffer` |

**OMP agent**: Installed via `curl -fsSL https://omp.sh/install | sh` in CI. Command templates in `.omp/commands/` (e.g., `triage-issue.md`, `review-pr.md`, `label-pr.md`). Output formatted by `.omp/stream-log.py`.

**Release Drafter** (`.github/release-drafter.yml`): Categorizes PRs by label — Features, Bug Fixes, Maintenance, Dependencies.

## Notes

- **Server URL**: Default `https://chronova.dev/`, configurable via SharedPreferences key `server_url`. `usesCleartextTraffic=true` allows HTTP for custom servers.
- **PRO status**: `MainPagerFragment` dynamically adjusts tab count based on PRO subscription (more tabs for PRO users).
- **WakaTime compatibility**: API models reference WakaTime-style responses; the Chronova server API mirrors WakaTime's structure.
- **Signing credentials are hardcoded**: `storePassword: 'chronova123'`, `keyPassword: 'chronova123'`, `keyAlias: 'chronova'` in `app/build.gradle`. Keystore file is committed to the repo.