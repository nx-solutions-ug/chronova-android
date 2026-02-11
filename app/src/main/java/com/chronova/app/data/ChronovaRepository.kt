package com.chronova.app.data

import android.content.Context
import android.content.SharedPreferences
import retrofit2.Response

class ChronovaRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("chronova_prefs", Context.MODE_PRIVATE)
    private val apiService = ApiClient.apiService

    fun saveApiKey(apiKey: String) {
        prefs.edit().putString("api_key", apiKey).apply()
    }

    fun getApiKey(): String? {
        return prefs.getString("api_key", null)
    }

    fun saveServerUrl(serverUrl: String) {
        prefs.edit().putString("server_url", serverUrl).apply()
        ApiClient.updateBaseUrl(serverUrl)
    }

    fun getServerUrl(): String {
        return prefs.getString("server_url", "https://app.chronova.dev/") ?: "https://app.chronova.dev/"
    }

    fun isValidUrl(url: String): Boolean {
        return try {
            val trimmedUrl = url.trim()
            if (trimmedUrl.isEmpty()) return false

            // Basic URL validation
            val urlPattern = Regex("^https?://[a-zA-Z0-9.-]+(?:\\.[a-zA-Z]{2,})?(?::[0-9]+)?(?:/.*)?$")
            urlPattern.matches(trimmedUrl)
        } catch (e: Exception) {
            false
        }
    }

    fun clearAuth() {
        prefs.edit().remove("api_key").apply()
    }

    fun isAuthenticated(): Boolean {
        return getApiKey() != null
    }

    private fun getAuthHeader(): String? {
        return getApiKey()?.let { "Bearer $it" }
    }

    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Login failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDashboard(): Result<DashboardResponse> {
        val authHeader = getAuthHeader() ?: return Result.failure(Exception("No authentication"))
        return try {
            // Get weekly stats and recent activity
            val statsResponse = apiService.getStats(authHeader, "last_7_days")
            val heartbeatsResponse = apiService.getHeartbeats(authHeader, 10)

            if (statsResponse.isSuccessful && heartbeatsResponse.isSuccessful) {
                val stats = statsResponse.body()?.data
                val heartbeats = heartbeatsResponse.body()?.data

                if (stats != null && heartbeats != null) {
                    val dashboard = transformToDashboard(stats, heartbeats)
                    Result.success(dashboard)
                } else {
                    Result.failure(Exception("Empty response data - stats: ${stats != null}, heartbeats: ${heartbeats != null}"))
                }
            } else {
                val statsError = if (!statsResponse.isSuccessful) "Stats API failed: ${statsResponse.code()} - ${statsResponse.message()}" else ""
                val heartbeatsError = if (!heartbeatsResponse.isSuccessful) "Heartbeats API failed: ${heartbeatsResponse.code()} - ${heartbeatsResponse.message()}" else ""
                Result.failure(Exception("API calls failed - $statsError $heartbeatsError"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLanguages(): Result<LanguageResponse> {
        val authHeader = getAuthHeader() ?: return Result.failure(Exception("No authentication"))
        return try {
            val response = apiService.getStats(authHeader, "last_30_days")
            if (response.isSuccessful && response.body() != null) {
                val stats = response.body()?.data
                if (stats != null) {
                    val languages = stats.languages.map { lang ->
                        Language(
                            name = lang.name,
                            totalTime = lang.totalSeconds / 3600.0,
                            percentage = lang.percent,
                            topProject = null, // Could be enhanced later
                            color = getLanguageColor(lang.name)
                        )
                    }
                    Result.success(LanguageResponse(languages))
                } else {
                    Result.failure(Exception("Empty response data"))
                }
            } else {
                Result.failure(Exception("Failed to get languages: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProjects(): Result<ProjectResponse> {
        val authHeader = getAuthHeader() ?: return Result.failure(Exception("No authentication"))
        return try {
            val response = apiService.getStats(authHeader, "last_30_days")
            if (response.isSuccessful && response.body() != null) {
                val stats = response.body()?.data
                if (stats != null) {
                    val projects = stats.projects.map { proj ->
                        Project(
                            name = proj.name,
                            totalTime = proj.totalSeconds / 3600.0,
                            percentage = proj.percent,
                            topLanguage = null, // Could be enhanced later
                            color = getProjectColor(proj.name)
                        )
                    }
                    Result.success(ProjectResponse(projects))
                } else {
                    Result.failure(Exception("Empty response data"))
                }
            } else {
                Result.failure(Exception("Failed to get projects: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getEditors(): Result<EditorResponse> {
        val authHeader = getAuthHeader() ?: return Result.failure(Exception("No authentication"))
        return try {
            val response = apiService.getStats(authHeader, "last_30_days")
            if (response.isSuccessful && response.body() != null) {
                val stats = response.body()?.data
                if (stats != null) {
                    val editors = stats.editors.map { editor ->
                        Editor(
                            name = editor.name,
                            totalTime = editor.totalSeconds / 3600.0,
                            percentage = editor.percent,
                            color = getEditorColor(editor.name)
                        )
                    }
                    Result.success(EditorResponse(editors))
                } else {
                    Result.failure(Exception("Empty response data"))
                }
            } else {
                Result.failure(Exception("Failed to get editors: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getStatsForRange(timeRange: String): Result<StatsRangeData> {
        val authHeader = getAuthHeader() ?: return Result.failure(Exception("No authentication"))
        return try {
            val apiRange = when (timeRange) {
                "today" -> "today"
                "last_7_days" -> "last_7_days"
                "last_30_days" -> "last_30_days"
                "last_3_months" -> "last_3_months"
                "last_year" -> "last_year"
                "all_time" -> "all_time"
                else -> "today"
            }

            val response = apiService.getStats(authHeader, apiRange)
            if (response.isSuccessful && response.body() != null) {
                val stats = response.body()?.data
                if (stats != null) {
                    val languagesMap = stats.languages.associate { it.name to it.totalSeconds.toLong() }
                    val projectsMap = stats.projects.associate { it.name to it.totalSeconds.toLong() }
                    val editorsMap = stats.editors.associate { it.name to it.totalSeconds.toLong() }

                    // Generate mock daily activity data (could be enhanced with real data)
                    val dailyActivity = generateDailyActivity(stats.dailyStats)

                    val rangeData = StatsRangeData(
                        totalSeconds = stats.totalSeconds.toLong(),
                        languages = languagesMap,
                        projects = projectsMap,
                        editors = editorsMap,
                        dailyActivity = dailyActivity
                    )
                    Result.success(rangeData)
                } else {
                    Result.failure(Exception("Empty response data"))
                }
            } else {
                Result.failure(Exception("Failed to get stats for range: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkProSubscription(): Result<Boolean> {
        val authHeader = getAuthHeader() ?: return Result.failure(Exception("No authentication"))
        return try {
            val response = apiService.getCurrentUser(authHeader)
            if (response.isSuccessful && response.body() != null) {
                val userData = response.body()?.data
                if (userData != null) {
                    // Check individual subscription
                    val hasIndividualPro = userData.subscriptionStatus?.let { status ->
                        userData.subscriptionPlan?.let { plan ->
                            status in listOf("active", "trialing", "past_due", "canceled") && plan == "pro"
                        } ?: false
                    } ?: false

                    // Check organization subscriptions
                    val hasOrgPro = userData.organizationSubscriptions?.any { org ->
                        org.subscriptionStatus?.let { status ->
                            org.subscriptionPlan?.let { plan ->
                                status in listOf("active", "trialing", "past_due", "canceled") &&
                                plan in listOf("org_team", "enterprise")
                            } ?: false
                        } ?: false
                    } ?: false

                    Result.success(hasIndividualPro || hasOrgPro)
                } else {
                    Result.failure(Exception("Empty response data"))
                }
            } else {
                Result.failure(Exception("Failed to check subscription: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFileActivity(perPage: Int = 50): Result<List<FileActivity>> {
        val authHeader = getAuthHeader() ?: return Result.failure(Exception("No authentication"))
        return try {
            val response = apiService.getHeartbeats(authHeader, perPage)
            if (response.isSuccessful && response.body() != null) {
                val heartbeats = response.body()?.data ?: emptyList()

                // Group heartbeats by file and calculate time spent
                val fileMap = mutableMapOf<String, FileActivityBuilder>()

                heartbeats.forEach { heartbeat ->
                    val filename = heartbeat.entity.split("/").lastOrNull() ?: heartbeat.entity
                    val key = "$filename|${heartbeat.project}|${heartbeat.language}"

                    if (fileMap.containsKey(key)) {
                        fileMap[key]!!.addTime(heartbeat.time)
                    } else {
                        fileMap[key] = FileActivityBuilder(
                            fileName = filename,
                            fullPath = heartbeat.entity,
                            project = heartbeat.project ?: "Unknown",
                            language = heartbeat.language ?: "Unknown",
                            editor = heartbeat.editor ?: "Unknown",
                            initialTime = heartbeat.time
                        )
                    }
                }

                // Convert to FileActivity list and sort by time spent
                val fileActivities = fileMap.values
                    .map { it.build() }
                    .sortedByDescending { it.timeSpent }

                Result.success(fileActivities)
            } else {
                Result.failure(Exception("Failed to get file activity: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Transformation functions to convert WakaTime API data to Android UI format
    private fun transformToDashboard(stats: WakaTimeStatsData, heartbeats: List<WakaTimeHeartbeatData>): DashboardResponse {
        val totalSeconds = if (stats.totalSeconds > 0) stats.totalSeconds else 0.0
        val totalHours = totalSeconds / 3600.0
        val weeklyHours = totalSeconds / 3600.0 // This is already weekly data

        // Transform daily data from API response
        val dailyData = if (stats.dailyStats?.isNotEmpty() == true) {
            // Use real daily stats from API
            stats.dailyStats.map { dailyStat ->
                DailyData(
                    date = formatDateForDisplay(dailyStat.date),
                    hours = dailyStat.totalSeconds / 3600.0
                )
            }
        } else {
            // Fallback to generate placeholder data if no daily stats available
            (0..6).map { day ->
                DailyData(
                    date = "Day $day",
                    hours = 0.0
                )
            }
        }

        // Transform languages
        val languageData = stats.languages.take(5).map { lang ->
            LanguageData(
                name = lang.name,
                value = maxOf(0, (lang.totalSeconds / 3600.0).toInt()),
                color = getLanguageColor(lang.name)
            )
        }

        // Transform projects
        val projectData = stats.projects.take(5).map { proj ->
            ProjectData(
                name = proj.name,
                hours = maxOf(0.0, proj.totalSeconds / 3600.0),
                percentage = maxOf(0.0, proj.percent),
                color = getProjectColor(proj.name)
            )
        }

        // Transform recent activity
        val recentActivity = heartbeats.map { hb ->
            val filename = hb.entity.split("/").lastOrNull() ?: hb.entity
            ActivityData(
                project = hb.project ?: "Unknown",
                file = filename,
                language = hb.language ?: "Unknown",
                time = formatTimestamp(hb.time),
                duration = "Active"
            )
        }

        return DashboardResponse(
            totalHours = maxOf(0.0, totalHours),
            weeklyHours = maxOf(0.0, weeklyHours),
            projects = maxOf(0, stats.projects.size),
            languages = maxOf(0, stats.languages.size),
            dailyData = dailyData,
            languageData = languageData,
            projectData = projectData,
            recentActivity = recentActivity
        )
    }

    private fun getLanguageColor(language: String): String {
        return when (language.lowercase()) {
            "javascript" -> "#f1e05a"
            "typescript" -> "#3178c6"
            "python" -> "#3776ab"
            "java" -> "#b07219"
            "kotlin" -> "#a97bff"
            "swift" -> "#fa7343"
            "go" -> "#00add8"
            "rust" -> "#dea584"
            "c++" -> "#f34b7d"
            "c#" -> "#239120"
            "php" -> "#4f5d95"
            "ruby" -> "#701516"
            "html" -> "#e34c26"
            "css" -> "#1572b6"
            else -> "#6b7280"
        }
    }

    private fun getProjectColor(project: String): String {
        val colors = arrayOf("#3B82F6", "#EF4444", "#10B981", "#F59E0B", "#8B5CF6", "#EC4899", "#06B6D4", "#84CC16")
        return colors[project.hashCode().rem(colors.size).let { if (it < 0) it + colors.size else it }]
    }

    private fun getEditorColor(editor: String): String {
        return when (editor.lowercase()) {
            "vs code", "visual studio code" -> "#007acc"
            "webstorm" -> "#000000"
            "intellij idea" -> "#000000"
            "pycharm" -> "#000000"
            "android studio" -> "#3ddc84"
            "xcode" -> "#147efb"
            "sublime text" -> "#ff9800"
            "atom" -> "#66595c"
            "vim", "neovim" -> "#019833"
            "emacs" -> "#7f5ab6"
            else -> "#6b7280"
        }
    }

    private fun formatTimestamp(timestamp: Double): String {
        val now = System.currentTimeMillis() / 1000.0
        val diff = (now - timestamp).toLong()
        return when {
            diff < 0 -> "Future" // Handle future timestamps gracefully
            diff < 60 -> "Just now"
            diff < 3600 -> "${diff / 60}m ago"
            diff < 86400 -> "${diff / 3600}h ago"
            else -> "${diff / 86400}d ago"
        }
    }

    private fun formatDateForDisplay(dateString: String): String {
        return try {
            // Parse the date string (e.g., "2025-09-02")
            val parts = dateString.split("-")
            if (parts.size == 3) {
                val month = parts[1].toInt()
                val day = parts[2].toInt()
                "$month/$day" // Return as "9/2" format
            } else {
                dateString // fallback to original string
            }
        } catch (e: Exception) {
            dateString // fallback to original string if parsing fails
        }
    }

    private fun generateDailyActivity(dailyStats: List<WakaTimeDailyData>?): List<Pair<String, Float>> {
        return if (dailyStats?.isNotEmpty() == true) {
            dailyStats.map { stat ->
                formatDateForDisplay(stat.date) to (stat.totalSeconds / 3600.0).toFloat()
            }
        } else {
            // Generate placeholder data for demo
            listOf(
                "Today" to 2.5f,
                "Yesterday" to 3.2f,
                "2 days ago" to 1.8f,
                "3 days ago" to 4.1f,
                "4 days ago" to 2.9f
            )
        }
    }
}

// Data classes for the new stats range functionality
data class StatsRangeData(
    val totalSeconds: Long,
    val languages: Map<String, Long>,
    val projects: Map<String, Long>,
    val editors: Map<String, Long>,
    val dailyActivity: List<Pair<String, Float>>
)
