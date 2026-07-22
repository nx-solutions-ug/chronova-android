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

## 10. Adding a fragment with ViewPager

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

For the shorter agent quick-reference, see [`AGENTS.md`](../AGENTS.md).
