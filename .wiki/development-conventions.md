---
type: conventions
title: Development Conventions
description: Mandatory patterns and rules for contributing to Chronova Android.
tags: [conventions, viewbinding, coroutines, repository]
---

# Development Conventions

These rules are mandatory. They keep the codebase consistent, leak-free, and easy for both humans and agents to reason about.

## 1. Always use ViewBinding

Never use `findViewById`. Every fragment and activity must use the standard ViewBinding pattern:

```kotlin
class MyFragment : Fragment(R.layout.fragment_my) {
    private var _binding: FragmentMyBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // CRITICAL: prevents memory leaks
    }
}
```

ViewBinding is enabled in `app/build.gradle`:

```gradle
buildFeatures {
    viewBinding = true
}
```

## 2. Always use `lifecycleScope` for coroutines

Launch coroutines from fragments and activities with `lifecycleScope.launch`. Never use `GlobalScope` or raw thread executors.

```kotlin
lifecycleScope.launch {
    repository.getDashboard()
        .onSuccess { data -> /* update UI */ }
        .onFailure { error -> /* show error */ }
}
```

Cancel long-running jobs in `onDestroyView()`:

```kotlin
private var loadDataJob: Job? = null

override fun onDestroyView() {
    super.onDestroyView()
    loadDataJob?.cancel()
    loadDataJob = null
    _binding = null
}
```

## 3. Always return `Result<T>` from repository methods

Repository methods must not throw to callers. Wrap network/parsing errors:

```kotlin
suspend fun getNewData(): Result<NewData> = try {
    val response = apiService.newEndpoint(apiKey)
    Result.success(transformResponse(response))
} catch (e: Exception) {
    Result.failure(e)
}
```

## 4. Always clear `_binding` in `onDestroyView()`

This is required in every fragment. The leaked binding reference is a common source of crashes after `onDestroyView()`.

## 5. Repository access without DI

No Hilt, Koin, or manual service locator. Instantiate directly:

```kotlin
private val repository = ChronovaRepository(requireContext())
```

## 6. Fragment factory pattern

Pass arguments through `Bundle` using a `newInstance` companion function:

```kotlin
companion object {
    fun newInstance(timeRange: String) = MyFragment().apply {
        arguments = Bundle().apply { putString("timeRange", timeRange) }
    }
}
```

## 7. No unsafe casts or suppression

Never use Kotlin unsafe casts, `@Suppress("...")` shortcuts, or type-script-style `as any` equivalents to bypass type safety.

## 8. Tests

No tests currently exist. Add them in:

- `app/src/test/` — unit tests (JUnit 4 is on the classpath).
- `app/src/androidTest/` — instrumented tests (Espresso is on the classpath).

## 9. File locations

| Purpose | Location |
|---------|----------|
| Repository | `app/src/main/java/com/chronova/app/data/ChronovaRepository.kt` |
| API service | `app/src/main/java/com/chronova/app/data/ChronovaApiService.kt` |
| API models | `app/src/main/java/com/chronova/app/data/ApiModels.kt` |
| Activities | `app/src/main/java/com/chronova/app/*Activity.kt` |
| Fragments | `app/src/main/java/com/chronova/app/ui/*Fragment.kt` |
| Adapters | `app/src/main/java/com/chronova/app/ui/*Adapter.kt` |
| ViewHolders | `app/src/main/java/com/chronova/app/ui/main/cards/viewholders/` |
| Layouts | `app/src/main/res/layout/` |
| Drawables | `app/src/main/res/drawable/` |
| Bottom navigation menu | `app/src/main/res/menu/bottom_navigation.xml` |
| Toolbar menu | `app/src/main/res/menu/main_menu.xml` |

## 10. Passing PRO status to fragments

Fragments that gate content by subscription receive `is_pro_user` through arguments from `MainActivity`:

```kotlin
val frag = MyProGatedFragment().apply {
    arguments = Bundle().apply { putBoolean("is_pro_user", isProUser) }
}
```

Use this flag in `onCreate()` or `onViewCreated()` to decide tab counts, available ranges, or whether to show a locked-state view.

## 11. Adding a fragment with ViewPager

Use `FragmentStateAdapter` and `TabLayoutMediator`:

```kotlin
class NewPagerFragment : Fragment(R.layout.fragment_new_pager) {
    private var _binding: FragmentNewPagerBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int) = when(position) {
                0 -> NewListFragment.newInstance("Today")
                1 -> NewListFragment.newInstance("7 Days")
                2 -> NewListFragment.newInstance("30 Days")
                else -> throw IllegalArgumentException()
            }
            override fun getItemCount() = 3
        }
        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, pos ->
            tab.text = when(pos) { 0 -> "Today"; 1 -> "7 Days"; else -> "30 Days" }
        }.attach()
    }
}
```

## 12. Vouch gate

External contributors must be **vouched** before a pull request can be merged. The `vouch-pr.yml` workflow runs on every PR and uses the `mitchellh/vouch` action to check the `.github/VOUCHED.td` list. If the PR author is not vouched, the workflow closes the PR. Write access collaborators and `[bot]` accounts are automatically allowed.

To request a vouch, open a **Discussion** describing your proposed contribution and ask a maintainer to comment `!vouch`. Maintainers use the `vouch-manage.yml` workflow to manage `.github/VOUCHED.td` via discussion comments (`!vouch`, `!denounce`, `!unvouch`). See [`CONTRIBUTING.md`](../CONTRIBUTING.md) for the full process.

## 13. Renovate / dependency PRs

Dependency updates are handled by Renovate. Workflow action updates (for example `actions/checkout` and `actions/cache`) are tracked in the repository and should not be duplicated manually. Renovate PRs are reviewed by the OMP agent in `omp-ci.yml`.

For the shorter agent quick-reference, see [`AGENTS.md`](../AGENTS.md).

## 14. Adding a new repository API group

When you add a server endpoint:

1. Add request/response DTOs in `ApiModels.kt`.
2. Declare the endpoint in `ChronovaApiService.kt`.
3. Add a suspending `Result<T>` wrapper in `ChronovaRepository.kt`.
4. Create the fragment/adapter in `ui/` and wire it through `MainActivity` if it belongs in bottom navigation.
