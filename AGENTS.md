# Agent Instructions for Chronova Android

## Quick Reference

```kotlin
// ViewBinding Pattern (MANDATORY)
class MyFragment : Fragment(R.layout.fragment_my) {
    private var _binding: FragmentMyBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(...): View {
        _binding = FragmentMyBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // CRITICAL: Prevents memory leaks
    }
}

// Repository Access (No DI)
private val repository = ChronovaRepository(requireContext())

// Async Operations with Result<T>
lifecycleScope.launch {
    repository.getDashboard()
        .onSuccess { data -> /* Update UI */ }
        .onFailure { error -> /* Show error */ }
}

// Fragment Factory Pattern
companion object {
    fun newInstance(timeRange: String) = MyFragment().apply {
        arguments = Bundle().apply { putString("timeRange", timeRange) }
    }
}
```

## Architecture

**Custom MVVM + Repository Pattern**
- `com.chronova.app.data` - Repository, API Service, Models
- `com.chronova.app.ui` - Activities, Fragments, Adapters
- No Dependency Injection framework (manual instantiation)
- No Architecture Components ViewModel (Fragment-level state)

## Critical Rules

1. **Always use ViewBinding** - Never findViewById
2. **Always use lifecycleScope** for coroutines in Fragments/Activities
3. **Always return Result<T>** from Repository methods
4. **Always clear _binding in onDestroyView()**
5. **Never use @ts-ignore or as any equivalents**
6. **No tests exist** - create in `src/test/` and `src/androidTest/`

## File Locations

| Purpose | Location |
|---------|----------|
| Repository | `app/src/main/java/com/chronova/app/data/ChronovaRepository.kt` |
| API Service | `app/src/main/java/com/chronova/app/data/ChronovaApiService.kt` |
| API Models | `app/src/main/java/com/chronova/app/data/ApiModels.kt` |
| Activities | `app/src/main/java/com/chronova/app/*Activity.kt` |
| Fragments | `app/src/main/java/com/chronova/app/ui/*Fragment.kt` |
| Adapters | `app/src/main/java/com/chronova/app/ui/*Adapter.kt` |
| ViewHolders | `app/src/main/java/com/chronova/app/ui/main/cards/viewholders/` |
| Layouts | `app/src/main/res/layout/` |
| Drawables | `app/src/main/res/drawable/` |

## Common Operations

### Add API Endpoint
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

### Add Fragment with ViewPager
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

## Dependencies

**Key Libraries:**
- Retrofit 3.0 + Gson for networking
- MPAndroidChart for charts
- AndroidX Navigation 2.9.6
- AndroidX Lifecycle 2.10.0
- Material Design 1.13.0

See `app/build.gradle` for complete list.

## Build Commands

```bash
./gradlew assembleDebug      # Debug APK
./gradlew assembleRelease    # Release APK (keystore included)
./docker-build.sh            # Docker build
```

## API Base URL

Default: `https://chronova.dev/`
Stored in SharedPreferences (key: `server_url`)

---

*For detailed patterns and conventions, see `.opencode/instructions/llms.md`*
