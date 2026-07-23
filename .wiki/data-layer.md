---
type: data
title: Data Layer
description: Repository, API client, models, authentication, and server configuration.
tags: [data, repository, api, retrofit]
---

# Data Layer

The data layer is responsible for all network communication, local persistence of auth/server settings, and transformation of raw API responses into UI-ready models.

## Files

| File | Role |
|------|------|
| [`ChronovaRepository.kt`](https://github.com/chronova/chronova-android/blob/main/app/src/main/java/com/chronova/app/data/ChronovaRepository.kt) | Single source of truth; exposes `Result<T>` methods. |
| [`ChronovaApiService.kt`](https://github.com/chronova/chronova-android/blob/main/app/src/main/java/com/chronova/app/data/ChronovaApiService.kt) | Retrofit interface defining REST endpoints. |
| [`ApiClient.kt`](https://github.com/chronova/chronova-android/blob/main/app/src/main/java/com/chronova/app/data/ApiClient.kt) | Retrofit singleton with dynamic base URL. |
| [`ApiModels.kt`](https://github.com/chronova/chronova-android/blob/main/app/src/main/java/com/chronova/app/data/ApiModels.kt) | Data classes for requests/responses. |

## Repository

`ChronovaRepository(context)` is instantiated manually wherever it is needed (activities, fragments). It does **not** use constructor injection.

### SharedPreferences keys

| Key | Content | Default |
|-----|---------|---------|
| `api_key` | Bearer token for API calls | `null` |
| `server_url` | Base URL of the Chronova server | `https://chronova.dev/` |
| `user_id` | Current user ID (used for leaderboard highlighting) | `null` |

### Public API

All suspending methods return `Result<T>`:

```kotlin
suspend fun login(email: String, password: String): Result<LoginResponse>
suspend fun getDashboard(): Result<DashboardResponse>
suspend fun getLanguages(): Result<LanguageResponse>
suspend fun getProjects(): Result<ProjectResponse>
suspend fun getEditors(): Result<EditorResponse>
suspend fun getStatsForRange(timeRange: String): Result<StatsRangeData>
suspend fun checkProSubscription(): Result<Boolean>
suspend fun getFileActivity(perPage: Int = 50): Result<List<FileActivity>>
suspend fun getGoals(): Result<List<Goal>>
suspend fun createGoal(request: GoalCreateRequest): Result<GoalResponse>
suspend fun deleteGoal(goalId: String): Result<DeleteGoalResponse>
suspend fun getGoalSuggestions(): Result<List<GoalSuggestion>>
suspend fun getLeaders(range: String, language: String? = null, page: Int = 1): Result<LeadersResponse>
suspend fun getAiAnalytics(range: String): Result<AiAnalyticsData>
suspend fun getFocusAnalytics(range: String): Result<FocusAnalyticsData>
```

## Pro subscription check

`checkProSubscription()` calls `/api/v1/users/current` and returns the value of the backend's pre-computed `has_premium_features` field. The repository no longer reconstructs Pro access from `subscriptionStatus`, `subscriptionPlan`, or `isProComped` locally, which avoids drifting from the server's `hasProAccess()` logic. The call also persists the returned `user_id` to SharedPreferences for leaderboard highlighting.

## API service

Endpoints mirror a WakaTime-compatible Chronova API:

```kotlin
@POST("api/auth/login")
suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

@GET("api/v1/users/current/stats/{range}")
suspend fun getStats(
    @Header("Authorization") authorization: String,
    @Path("range") range: String
): Response<WakaTimeStatsResponse>

@GET("api/v1/users/current")
suspend fun getCurrentUser(@Header("Authorization") authorization: String): Response<UserResponse>

@GET("api/v1/users/current/heartbeats")
suspend fun getHeartbeats(
    @Header("Authorization") authorization: String,
    @Query("per_page") perPage: Int = 10
): Response<WakaTimeHeartbeatsResponse>

@GET("api/v1/users/current/projects")
suspend fun getProjects(@Header("Authorization") authorization: String): Response<WakaTimeProjectsResponse>

// Goals
@GET("api/v1/users/current/goals")
suspend fun getGoals(@Header("Authorization") authorization: String): Response<GoalsResponse>

@POST("api/v1/users/current/goals")
suspend fun createGoal(
    @Header("Authorization") authorization: String,
    @Body request: GoalCreateRequest
): Response<GoalResponse>

@DELETE("api/v1/users/current/goals")
suspend fun deleteGoal(
    @Header("Authorization") authorization: String,
    @Query("id") goalId: String
): Response<DeleteGoalResponse>

@GET("api/v1/users/current/goals/suggestions")
suspend fun getGoalSuggestions(@Header("Authorization") authorization: String): Response<GoalSuggestionsResponse>

// Leaderboard
@GET("api/v1/leaders")
suspend fun getLeaders(
    @Header("Authorization") authorization: String,
    @Query("range") range: String = "last_7_days",
    @Query("language") language: String? = null,
    @Query("page") page: Int = 1
): Response<LeadersResponse>

// AI Insights
@GET("api/v1/users/current/analytics/ai")
suspend fun getAiAnalytics(
    @Header("Authorization") authorization: String,
    @Query("range") range: String = "last_7_days"
): Response<AiAnalyticsResponse>

// Focus Analytics
@GET("api/v1/users/current/analytics/focus")
suspend fun getFocusAnalytics(
    @Header("Authorization") authorization: String,
    @Query("range") range: String = "last_7_days"
): Response<FocusAnalyticsResponse>
```

The authorization header is formatted as `Bearer $apiKey` inside the repository.

## Dynamic base URL

`ApiClient` caches the current `Retrofit` instance. When `ChronovaRepository.saveServerUrl(url)` is called, it invokes `ApiClient.updateBaseUrl(url)`, which invalidates the cached Retrofit instance so the next repository call uses the new base URL.

## Adding a new endpoint

1. Declare the endpoint in `ChronovaApiService.kt`.
2. Add a wrapper method in `ChronovaRepository.kt` that returns `Result<T>` and handles errors.
3. Add any new DTOs to `ApiModels.kt`.

Example:

```kotlin
// ChronovaApiService.kt
@GET("api/new-endpoint")
suspend fun newEndpoint(@Header("Authorization") apiKey: String): NewResponse

// ChronovaRepository.kt
suspend fun getNewData(): Result<NewData> = try {
    val response = apiService.newEndpoint(apiKey)
    Result.success(transformResponse(response))
} catch (e: Exception) {
    Result.failure(e)
}
```

## Error handling

- Missing API key → `Result.failure(Exception("No authentication"))`.
- HTTP error → message includes status code and Retrofit message.
- Network/parse exception → wrapped in `Result.failure(e)`.

UI callers should handle both branches with `Result.fold(onSuccess, onFailure)`.
