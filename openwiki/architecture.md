# Architecture

Chronova Android uses a hand-rolled MVVM with a single repository acting as
the source of truth for both the network and `SharedPreferences`. There is
no DI framework and no Architecture Components `ViewModel` — each fragment
holds its own state and recreates on configuration change.

## Layers

```
┌─────────────────────────────────────────────┐
│  UI                                         │
│  Activities, Fragments, RecyclerView        │
│  Adapters, ViewHolders (cards/)             │
└──────────────────┬──────────────────────────┘
                   │  ChronovaRepository (instance per Fragment/Activity)
┌──────────────────▼──────────────────────────┐
│  Repository (data/ChronovaRepository.kt)    │
│  - SharedPreferences (api_key, server_url) │
│  - API calls wrapped in Result<T>           │
│  - Response -> domain transformation        │
└──────────────────┬──────────────────────────┘
                   │
┌──────────────────▼──────────────────────────┐
│  Data                                       │
│  ApiClient (Retrofit singleton, mutable     │
│    base URL), ChronovaApiService,           │
│    ApiModels (Gson DTOs)                    │
└─────────────────────────────────────────────┘
```

### UI layer

- **Activities** (`LoginActivity`, `MainActivity`) are thin entry points:
  they inflate ViewBinding, instantiate the repository, and route to the
  right fragment. `MainActivity` is also the bottom-nav host and the
  Pro-subscription gate.
- **Fragments** live under `com.chronova.app.ui` and follow a strict
  ViewBinding pattern (see [development.md](development.md)). They own no
  `ViewModel`; instead, they call `repository.suspendMethod()` inside
  `lifecycleScope.launch { ... }`.
- **Pager + Stats** triplets — e.g. `ProjectsPagerFragment` +
  `ProjectsFragment`/`ProjectsStatsFragment` + `ProjectAdapter` — let each
  tab swap between a list view and a stats view for a given time range.
- **Dashboard cards** (`ui/main/cards/`) build a heterogeneous list via
  `CardsList` / `CardsAdapter`. The card types are:
  `TYPE_GLOBAL_SUMMARY`, `TYPE_PIE_CHART`, `TYPE_LINE_CHART`, rendered by
  `GlobalSummaryViewHolder`, `PieChartViewHolder`, and
  `LineChartViewHolder` respectively. `PieChartViewHolder` collapses
  everything past the top 5 slices into an "Others" slice.

### Repository layer

`ChronovaRepository` is the single source of truth. It:

- Reads/writes `SharedPreferences` ("chronova_prefs") for the API key and
  server URL.
- Exposes a `Bearer`-token `Authorization` header helper
  (`getAuthHeader()`).
- Calls `ApiClient.apiService` for the four endpoints defined on
  `ChronovaApiService` and returns `Result<T>` for every suspend method.
- Validates user-entered URLs with a small regex (`isValidUrl`).

`saveServerUrl()` calls `ApiClient.updateBaseUrl()`, which lazily
recreates the underlying `Retrofit` instance the next time `apiService`
is accessed.

### Data layer

- `ApiClient` is an `object` that holds a single `OkHttpClient` (with
  `HttpLoggingInterceptor` at `BODY` level and 30-second timeouts) and
  caches the active `Retrofit` instance. Calling `updateBaseUrl()` clears
  the cache so the next access rebuilds it with the new base URL.
- `ChronovaApiService` is a small Retrofit interface with four endpoints
  (see [API surface](architecture.md#api-surface) below). All return
  `Response<T>` so callers can branch on `isSuccessful` and on the
  response body.
- `ApiModels.kt` is a long file of Gson DTOs. There are two parallel
  shapes:
  - The app-shaped ones used by the UI: `DashboardResponse`,
    `LanguageResponse`, `ProjectResponse`, `EditorResponse`, etc.
  - WakaTime-compatible ones (`WakaTimeStatsResponse`,
    `WakaTimeStatsData`, `WakaTimeLanguageData`, `WakaTimeProjectData`,
    `WakaTimeEditorData`, `WakaTimeOSData`, `WakaTimeDailyData`, plus
    `WakaTimeHeartbeatsResponse`) — these are the actual wire format
    returned by `/api/v1/users/current/...`. The repository transforms
    them into the app-shaped models before returning to the UI.

## API surface

`ChronovaApiService` (`app/src/main/java/com/chronova/app/data/ChronovaApiService.kt`):

| Method | Path | Notes |
|---|---|---|
| `POST api/auth/login` | `login(LoginRequest)` | Returns `LoginResponse` (message + user + apiKey). |
| `GET api/v1/users/current/stats/{range}` | `getStats(auth, range)` | `range` values currently used: `last_7_days`, `last_30_days`. |
| `GET api/v1/users/current` | `getCurrentUser(auth)` | Used to detect Pro subscription. |
| `GET api/v1/users/current/heartbeats` | `getHeartbeats(auth, perPage=10)` | Recent file activity. |
| `GET api/v1/users/current/projects` | `getProjects(auth)` | Full project list. |

All endpoints are authed with `Authorization: Bearer <api-key>` except
`login`. The default base URL is `https://chronova.dev/`; `updateBaseUrl()`
rebuilds the Retrofit instance when the user changes it from
`LoginActivity`.

## Data flow walkthrough: dashboard load

1. `MainActivity.onCreate` calls `ApiClient.updateBaseUrl(repository.getServerUrl())`,
   then `checkProSubscription()` (which fires `getCurrentUser`).
2. It inflates `activity_main.xml`, attaches the bottom nav, and replaces
   the fragment container with `MainPagerFragment` (passing `is_pro_user`
   via `Bundle`).
3. `MainPagerFragment` builds a `MainPagerAdapter` (a `FragmentStateAdapter`)
   and a `TabLayoutMediator`. Pro users see 6 tabs (Today, 7d, 30d, 3mo,
   Year, All Time); non-Pro users see 2 tabs. Default tab is "Last 7 Days".
4. The current tab fragment (e.g. `MainStatsFragment` for the chosen
   range) calls `repository.getDashboard()` / `getLanguages()` /
   `getProjects()` / `getEditors()` inside `lifecycleScope`.
5. `repository.getDashboard()` fires `getStats("last_7_days")` and
   `getHeartbeats(perPage=10)` in parallel-branches, then maps the
   WakaTime-shaped responses into a `DashboardResponse` via
   `transformToDashboard(stats, heartbeats)`.
6. The fragment converts the `DashboardResponse` into a `CardsList` and
   hands it to `CardsAdapter`, which dispatches each item to the right
   `ViewHolder`. `PieChartViewHolder` builds the chart at `bind()` time.

## Pro / Free gating

`MainActivity.checkProSubscription()` reads the user response and flips
`isProUser`. `MainPagerFragment.updateProStatus(isPro)` recreates the
adapter with the new PRO flag and re-attaches the `TabLayoutMediator`,
so a Pro upgrade unlocks the additional time-range tabs without an
activity restart.

## Why this design

- **Single repository** keeps the auth, server URL, and HTTP plumbing in
  one place — no DI, no service locators, no Hilt. Each Fragment/Activity
  just does `private val repository = ChronovaRepository(requireContext())`.
- **Manual DI** keeps the dependency surface trivially small. If a future
  refactor wants Hilt, every construction site is already isolated in
  one line.
- **`Result<T>` everywhere** forces explicit success/failure handling at
  every UI call site (`onSuccess { ... }.onFailure { ... }`).
- **WakaTime-shaped DTOs** suggest the server reuses the WakaTime API
  shape, and `ChronovaRepository` is where the WakaTime → Chronova UI
  shape translation lives. If the backend ever drops that compatibility
  shim, the change is local to `ChronovaRepository` and `ApiModels`.
