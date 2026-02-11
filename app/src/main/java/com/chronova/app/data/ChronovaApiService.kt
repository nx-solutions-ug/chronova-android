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
}