# Chronova Android - LLM Context Guide

## Project Overview

**Chronova** is an Android application for developer productivity analytics. It displays coding statistics (languages, projects, editors) and heartbeats data from a Chronova server.

**Package**: `com.chronova.app`  
**Min SDK**: 24  
**Target SDK**: 36  
**Language**: Kotlin (JVM 17)  
**Architecture**: Custom MVVM with Repository pattern

---

## Project Structure

```
app/src/main/java/com/chronova/app/
├── data/
│   ├── ApiClient.kt              # Retrofit singleton instance
│   ├── ApiModels.kt              # Data classes for API responses
│   ├── ChronovaApiService.kt     # Retrofit interface
│   └── ChronovaRepository.kt     # Repository (SSOT)
├── ui/
│   ├── main/
│   │   ├── cards/
│   │   │   ├── CardsAdapter.kt
│   │   │   ├── CardsList.kt
│   │   │   └── viewholders/
│   │   ├── MainStatsFragment.kt
│   │   └── MainPagerFragment.kt
│   ├── DashboardFragment.kt
│   ├── ProjectsFragment.kt
│   ├── LanguagesFragment.kt
│   ├── EditorsFragment.kt
│   ├── FilesFragment.kt
│   └── [Adapter classes]
├── LoginActivity.kt
└── MainActivity.kt
```

---

## Key Libraries & Dependencies

```gradle
// Android Core
androidx.appcompat:appcompat:1.7.1
com.google.android.material:material:1.13.0
androidx.constraintlayout:constraintlayout:2.2.1

// Navigation & Architecture
androidx.navigation:navigation-fragment-ktx:2.9.6
androidx.lifecycle:lifecycle-viewmodel-ktx:2.10.0
androidx.lifecycle:lifecycle-livedata-ktx:2.9.4

// Networking
com.squareup.retrofit2:retrofit:3.0.0
com.squareup.retrofit2:converter-gson:3.0.0
com.squareup.okhttp3:logging-interceptor:5.3.2

// Charts
com.github.PhilJay:MPAndroidChart:3.1.0

// UI
androidx.recyclerview:recyclerview:1.4.0
androidx.viewpager2:viewpager2:1.1.0

// Storage
androidx.preference:preference-ktx:1.2.1
```

---

## Architecture Patterns

### 1. Custom MVVM (No Architecture Components ViewModel)

**No DI framework** - manual instantiation via constructors:

```kotlin
// Repository as single instance in Activity/Fragment
private val repository = ChronovaRepository(requireContext())
```

**Lifecycle-aware coroutines**:
```kotlin
// ALWAYS use lifecycleScope.launch in Fragments/Activities
lifecycleScope.launch {
    repository.getDashboard()
        .onSuccess { /* update UI */ }
        .onFailure { /* show error */ }
}
```

### 2. Repository Pattern

`ChronovaRepository` is the single source of truth:
- Manages SharedPreferences (auth, settings)
- Performs API calls via `ChronovaApiService`
- Transforms API data to UI-ready formats
- Returns `Result<T>` for error handling

### 3. Data Layer

**API Calls Pattern**:
```kotlin
suspend fun getDashboard(): Result<DashboardData> = try {
    val response = apiService.getDashboard(apiKey)
    Result.success(transformToDashboard(response))
} catch (e: Exception) {
    Result.failure(e)
}
```

**Key Data Classes** (in ApiModels.kt):
- `StatsData`, `LanguagesData`, `ProjectsData`, `EditorsData`
- `GlobalStats`, `LanguageStat`, `ProjectStat`, `EditorStat`
- `DashboardResponse`, `HeartbeatData`

### 4. UI Layer

**Fragments with ViewBinding**:
```kotlin
class MyFragment : Fragment(R.layout.fragment_my) {
    private var _binding: FragmentMyBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(...) = 
        FragmentMyBinding.inflate(...).also { _binding = it }.root
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // MUST clear to avoid memory leaks
    }
}
```

**ViewPager2 + TabLayout Pattern**:
```kotlin
// MainPagerFragment, ProjectsPagerFragment, etc.
viewPager.adapter = object : FragmentStateAdapter(this) {
    override fun createFragment(position: Int) = when(position) {
        0 -> MyListFragment.newInstance(range)  // "Today" / "7 Days" / "30 Days"
        1 -> MyStatsFragment.newInstance(range)
        else -> throw IllegalArgumentException()
    }
}
```

**RecyclerView with Multiple View Types**:
```kotlin
class CardsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val TYPE_GLOBAL_SUMMARY = 0
        private const val TYPE_PIE_CHART = 1
        private const val TYPE_LINE_CHART = 2
    }
    // onCreateViewHolder returns different ViewHolders based on viewType
}
```

### 5. Cards Pattern

`CardsList` builder for dashboard:
```kotlin
val cardsList = CardsList()
    .addGlobalSummary(totalSeconds, timeRange)
    .addPieChart(title, data)
    .addLineChart(title, data)
```

---

## Naming Conventions

| Entity | Pattern | Example |
|--------|---------|---------|
| Classes | PascalCase with suffix | `ChronovaRepository`, `MainActivity` |
| Methods | camelCase | `getDashboard()`, `onViewCreated()` |
| Variables | camelCase | `repository`, `binding` |
| Constants | UPPER_SNAKE_CASE | `TYPE_GLOBAL_SUMMARY` |
| Packages | lowercase, hierarchical | `com.chronova.app.data` |

**ViewBinding naming**: Layout `fragment_main.xml` → `FragmentMainBinding`

---

## Kotlin Idioms

### ViewBinding Pattern
```kotlin
private var _binding: FragmentDashboardBinding? = null
private val binding get() = _binding!!
```

### Companion Object Factories
```kotlin
companion object {
    fun newInstance(timeRange: String): MyFragment {
        return MyFragment().apply {
            arguments = Bundle().apply { putString("timeRange", timeRange) }
        }
    }
}
```

### Coroutines & Result Handling
```kotlin
lifecycleScope.launch {
    repository.someOperation()
        .onSuccess { result ->
            // Update UI on Main thread
        }
        .onFailure { error ->
            Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
        }
}
```

### Null Safety
- Use `?.let { }` for nullable checks
- Elvis operator `?:` for defaults
- `!!` only when non-null is guaranteed
- `lateinit` for properties initialized in lifecycle methods

---

## Error Handling

**Repository Pattern**:
- All API methods return `Result<T>`
- Wrap in try-catch, return `Result.failure(exception)` on error

**UI Pattern**:
```kotlin
.onFailure { error ->
    when (error) {
        is IOException -> showNetworkError()
        is HttpException -> showApiError(error.code())
        else -> showGenericError(error.message)
    }
}
```

---

## SharedPreferences Keys

```kotlin
private const val PREFS_NAME = "chronova_prefs"
private const val KEY_API_KEY = "api_key"
private const val KEY_SERVER_URL = "server_url"
// Default: "https://chronova.dev/"
```

---

## Color Mapping (Repository)

Repository contains static color maps for:
- **Languages**: Kotlin, Java, Python, JavaScript, TypeScript, Go, Rust, C++, etc.
- **Editors**: VS Code, IntelliJ, Vim, Emacs, etc.
- **Projects**: Hash-based selection from predefined array

---

## API Endpoints

Base URL: Stored in SharedPreferences, default `https://chronova.dev/`

```kotlin
interface ChronovaApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
    
    @GET("api/dashboard")
    suspend fun getDashboard(@Header("Authorization") apiKey: String): DashboardResponse
    
    @GET("api/stats")
    suspend fun getStats(@Header("Authorization") apiKey: String, 
                         @Query("range") range: String): StatsResponse
    
    // ... languages, projects, editors, heartbeats, file-activity
}
```

---

## Testing

**Status**: No tests currently exist in the project.

If adding tests, follow:
- JUnit 4 for unit tests
- Espresso for UI tests (already in dependencies)
- MockK or Mockito for mocking

---

## Common Tasks

### Add a New Fragment
1. Create class extending `Fragment(R.layout.fragment_new)`
2. Create layout XML in `res/layout/fragment_new.xml`
3. Add `newInstance()` factory in companion object
4. Use ViewBinding pattern
5. Launch coroutines in `lifecycleScope`

### Add a New API Endpoint
1. Add method to `ChronovaApiService`
2. Add data class to `ApiModels.kt` if needed
3. Add public method to `ChronovaRepository`
4. Return `Result<T>` with try-catch wrapper
5. Call from Fragment with `.onSuccess/.onFailure`

### Add a New Card Type
1. Add constant to `CardsAdapter` companion object
2. Create ViewHolder class in `viewholders/`
3. Add to `onCreateViewHolder` when block
4. Add bind logic to `onBindViewHolder`
5. Add builder method to `CardsList`

---

## Build & Deploy

```bash
# Build debug APK
./gradlew assembleDebug

# Build release (keystore included)
./gradlew assembleRelease

# Using Docker
./docker-build.sh
```

---

## Notes for LLMs

1. **No Dependency Injection** - Instantiate Repository directly
2. **No Hilt/Dagger** - Manual dependency management
3. **Result<T> Pattern** - All async operations return Result for error handling
4. **lifecycleScope** - Required for Fragment/Activity coroutines
5. **ViewBinding** - Mandatory for type-safe view access
6. **Multiple View Types** - CardsAdapter shows pattern for complex RecyclerViews
7. **SharedPreferences** - Repository manages all local storage
8. **No Tests** - Test files don't exist; add in `src/test/` and `src/androidTest/`
9. **Default Server** - `https://chronova.dev/` is hardcoded default
10. **ProGuard** - Disabled for release builds (minifyEnabled = false)
