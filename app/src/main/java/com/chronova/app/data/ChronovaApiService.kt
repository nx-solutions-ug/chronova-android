package com.chronova.app.data

import retrofit2.Response
import retrofit2.http.*

interface ChronovaApiService {
    
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
}