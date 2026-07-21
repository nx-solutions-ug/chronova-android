# Development

Coding patterns, build/test workflow, CI, and operational notes for working
on Chronova Android.

## Mandatory patterns

These are enforced across the codebase — see `AGENTS.md` for the agent
quick-reference and the source files referenced for examples.

### 1. ViewBinding everywhere

```kotlin
class MyFragment : Fragment(R.layout.fragment_my) {
    private var _binding: FragmentMyBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null          // CRITICAL: prevents leaks
    }
}
```

- `viewBinding = true` is set in `app/build.gradle` under
  `buildFeatures`.
- Activities use `lateinit var binding: ActivityXBinding` and inflate
  manually in `onCreate`.
- Never use `findViewById`.

### 2. Repository access without DI

```kotlin
private val repository = ChronovaRepository(requireContext())
```

Each Fragment/Activity constructs its own. The repository is stateless
beyond `SharedPreferences` and the lazily-recreated Retrofit client.

### 3. `Result<T>` from every suspend call

Every public `suspend` function in `ChronovaRepository` returns
`Result<T>`. Fragments consume it like:

```kotlin
lifecycleScope.launch {
    repository.getDashboard()
        .onSuccess { data -> /* update UI */ }
        .onFailure { error -> /* show error */ }
}
```

Use `lifecycleScope` (not `viewModelScope` — there is no `ViewModel`).

### 4. Pager + tab mediator

The Pro/Non-Pro time-range tabs use `FragmentStateAdapter` +
`TabLayoutMediator`. The triplet for each domain
(`ProjectsPagerFragment` / `ProjectsFragment` + `ProjectsStatsFragment` /
`ProjectAdapter`) follows the same shape; copy the existing files rather
than introducing a new abstraction.

### 5. Card list for the dashboard

`CardsList` is a small builder of `(type, payload)` items. New card
kinds should:

1. Add a `const val TYPE_*` to `CardsList`.
2. Add a payload data class and an `addXxx(...)` builder.
3. Add a `ViewHolder` in `ui/main/cards/viewholders/` and a new
   `getItemViewType()` branch + `onCreateViewHolder()` /
   `onBindViewHolder()` branch in `CardsAdapter`.

`PieChartViewHolder` is the canonical example for how to handle empty
data, top-N grouping, and chart data assembly.

## Adding a new API endpoint

1. Add the suspend function to `ChronovaApiService.kt`. Always use
   `Response<T>` (not bare `T`) so the repository can branch on
   `isSuccessful` and surface HTTP errors.
2. Add or extend the matching DTOs in `ApiModels.kt`. If the wire format
   follows the WakaTime shape, reuse the `WakaTime*` types and
   transform in the repository; otherwise define app-shaped types.
3. Add a `suspend fun ...: Result<T>` on `ChronovaRepository` that
   - requires `getAuthHeader() != null` (return `Result.failure` if not)
   - runs the call inside `try { ... } catch (e: Exception) { Result.failure(e) }`
   - on `isSuccessful` + non-null body, transforms the wire DTO into the
     app-shaped model and returns `Result.success(...)`.
4. Call the new method from a fragment inside `lifecycleScope.launch`,
   then update the UI on `onSuccess`.

## Build variants

| Variant | Command | Output |
|---|---|---|
| Debug | `./gradlew assembleDebug` | `app/build/outputs/apk/debug/app-debug.apk` |
| Release | `./gradlew assembleRelease` | `app/build/outputs/apk/release/app-release.apk` (signed with the in-repo keystore) |

`./build.sh [release]` wraps Gradle and copies the APK to
`../public/downloads/chronova-<variant>.apk`.

`./docker-build.sh` builds inside `Dockerfile.build` (openjdk:17-jdk-slim
+ Android SDK 34 + command-line tools) and writes the APK to
`build-output/apk/debug/app-debug.apk`.

## Testing

- No tests exist yet. `app/build.gradle` already pulls in JUnit 4.13.2,
  AndroidX Test ext:junit 1.3.0, and Espresso 3.7.0.
- New unit tests go in `app/src/test/`; instrumented tests in
  `app/src/androidTest/`.
- `testInstrumentationRunner` is set to
  `androidx.test.runner.AndroidJUnitRunner`.
- The CI `build.yml` workflow runs `./gradlew testDebugUnitTest
  --stacktrace` and uploads `app/build/reports/tests/` as the
  `test-results` artifact.

## CI workflows (`.github/workflows/`)

| Workflow | Trigger | Purpose |
|---|---|---|
| `build.yml` | `workflow_dispatch` | JDK 17 + Android SDK, runs `testDebugUnitTest`, builds debug + release APKs, uploads `app-debug` / `app-release` / `test-results` artifacts. |
| `release.yml` | release pipeline | Drafts/releases the signed APK. |
| `auto-manage.yml` | repo events | Auto-triage / labelling. |
| `omp.yml` / `omp-ci.yml` | `/omp` comments, PRs | Runs the OMP agent against the Ollama-cloud `minimax-m3` model using commands in `.omp/commands/`. |
| `openwiki-update.yml` | `workflow_dispatch`, push to `main`, daily 08:00 UTC | Builds the OpenWiki fork and opens a `docs: update OpenWiki` PR via `peter-evans/create-pull-request@v8`. |
| `release-drafter.yml` | config for the release drafter. |

The OpenWiki update workflow uses `actions/create-github-app-token@v3`
with `APP_CLIENT_ID` / `APP_PRIVATE_KEY` secrets, runs `openwiki --update
--print` against the `feat/ollama-provider` branch of the OpenWiki fork,
and opens a PR with `add-paths: openwiki`.

## .omp/ agent config

`.omp/commands/` defines slash-commands (`label-pr`, `review-pr`,
`triage-issue`) that the OMP agent expands when triggered from a
`/omp ...` comment. `.omp/stream-log.py` streams OMP's JSON output into
GitHub-friendly logs. There are no production runtime implications — it
is a contributor convenience.

## Common pitfalls

- **Forgetting `_binding = null` in `onDestroyView()`** — leaks the entire
  fragment view. ViewBinding cleanup is mandatory in this codebase.
- **Calling `ApiClient.apiService` from a background thread without
  re-checking the base URL** — the base URL only takes effect after the
  next `apiService` access (it lazily rebuilds the Retrofit). The login
  flow always calls `ApiClient.updateBaseUrl(...)` before navigating to
  `MainActivity`, so this rarely matters, but if you add a new entry
  point, follow the same pattern.
- **Hardcoding `https://chronova.dev/` instead of `repository.getServerUrl()`**
  — the server URL is user-configurable per `SharedPreferences`. Always
  go through the repository.
- **Adding a new `ViewModel`** — there is no `ViewModel` in this
  codebase; fragment-level state is intentional. If you need a
  `ViewModel`, justify it in the PR.
- **New strings/layouts** — keep them in `res/values/strings.xml` and
  `res/layout/`. Material 3 theming lives in `res/values/themes.xml`.

## Useful entry points

- App entry: `app/src/main/AndroidManifest.xml`
- Auth gate: `app/src/main/java/com/chronova/app/MainActivity.kt`
- Sign-in: `app/src/main/java/com/chronova/app/LoginActivity.kt`
- Source of truth: `app/src/main/java/com/chronova/app/data/ChronovaRepository.kt`
- Retrofit wiring: `app/src/main/java/com/chronova/app/data/ApiClient.kt`
- Card list: `app/src/main/java/com/chronova/app/ui/main/cards/CardsList.kt`
- Pie chart example: `app/src/main/java/com/chronova/app/ui/main/cards/viewholders/PieChartViewHolder.kt`
