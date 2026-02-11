package com.chronova.app.data

import com.google.gson.annotations.SerializedName

data class DashboardResponse(
    @SerializedName("totalHours")
    val totalHours: Double,
    @SerializedName("weeklyHours") 
    val weeklyHours: Double,
    @SerializedName("projects")
    val projects: Int,
    @SerializedName("languages")
    val languages: Int,
    @SerializedName("dailyData")
    val dailyData: List<DailyData>,
    @SerializedName("languageData")
    val languageData: List<LanguageData>,
    @SerializedName("projectData")
    val projectData: List<ProjectData>,
    @SerializedName("recentActivity")
    val recentActivity: List<ActivityData>
)

data class DailyData(
    @SerializedName("date")
    val date: String,
    @SerializedName("hours")
    val hours: Double
)

data class LanguageData(
    @SerializedName("name")
    val name: String,
    @SerializedName("value")
    val value: Int,
    @SerializedName("color")
    val color: String
)

data class ProjectData(
    @SerializedName("name")
    val name: String,
    @SerializedName("hours")
    val hours: Double,
    @SerializedName("percentage")
    val percentage: Double,
    @SerializedName("color")
    val color: String
)

data class ActivityData(
    @SerializedName("project")
    val project: String,
    @SerializedName("file")
    val file: String,
    @SerializedName("language")
    val language: String,
    @SerializedName("time")
    val time: String,
    @SerializedName("duration")
    val duration: String
)

data class LanguageResponse(
    @SerializedName("data")
    val data: List<Language>
)

data class Language(
    @SerializedName("name")
    val name: String,
    @SerializedName("totalTime")
    val totalTime: Double,
    @SerializedName("percentage")
    val percentage: Double,
    @SerializedName("topProject")
    val topProject: String?,
    @SerializedName("color")
    val color: String
)

data class ProjectResponse(
    @SerializedName("data")
    val data: List<Project>
)

data class Project(
    @SerializedName("name")
    val name: String,
    @SerializedName("totalTime")
    val totalTime: Double,
    @SerializedName("percentage")
    val percentage: Double,
    @SerializedName("topLanguage")
    val topLanguage: String?,
    @SerializedName("color")
    val color: String
)

data class EditorResponse(
    @SerializedName("data")
    val data: List<Editor>
)

data class Editor(
    @SerializedName("name")
    val name: String,
    @SerializedName("totalTime")
    val totalTime: Double,
    @SerializedName("percentage")
    val percentage: Double,
    @SerializedName("color")
    val color: String
)

data class LoginRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
)

data class LoginResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("user")
    val user: User?,
    @SerializedName("apiKey")
    val apiKey: String?
)

data class User(
    @SerializedName("id")
    val id: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("username")
    val username: String?
)

data class UserResponse(
    @SerializedName("data")
    val data: UserData
)

data class UserData(
    @SerializedName("id")
    val id: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("username")
    val username: String?,
    @SerializedName("subscriptionStatus")
    val subscriptionStatus: String?,
    @SerializedName("subscriptionPlan")
    val subscriptionPlan: String?,
    @SerializedName("organizationSubscriptions")
    val organizationSubscriptions: List<OrganizationSubscriptionData>?
)

data class OrganizationSubscriptionData(
    @SerializedName("organizationId")
    val organizationId: String?,
    @SerializedName("organizationName")
    val organizationName: String?,
    @SerializedName("subscriptionStatus")
    val subscriptionStatus: String?,
    @SerializedName("subscriptionPlan")
    val subscriptionPlan: String?,
    @SerializedName("subscriptionPeriodEnd")
    val subscriptionPeriodEnd: String?
)

data class ApiError(
    @SerializedName("error")
    val error: String
)

// WakaTime-compatible API response models
data class WakaTimeStatsResponse(
    @SerializedName("data")
    val data: WakaTimeStatsData
)

data class WakaTimeStatsData(
    @SerializedName("range")
    val range: String,
    @SerializedName("total_seconds")
    val totalSeconds: Double,
    @SerializedName("daily_average")
    val dailyAverage: Double,
    @SerializedName("human_readable_total")
    val humanReadableTotal: String,
    @SerializedName("human_readable_daily_average")
    val humanReadableDailyAverage: String,
    @SerializedName("languages")
    val languages: List<WakaTimeLanguageData>,
    @SerializedName("projects")
    val projects: List<WakaTimeProjectData>,
    @SerializedName("editors")
    val editors: List<WakaTimeEditorData>,
    @SerializedName("operating_systems")
    val operatingSystems: List<WakaTimeOSData>,
    @SerializedName("daily_stats")
    val dailyStats: List<WakaTimeDailyData>?
)

data class WakaTimeLanguageData(
    @SerializedName("name")
    val name: String,
    @SerializedName("total_seconds")
    val totalSeconds: Double,
    @SerializedName("percent")
    val percent: Double,
    @SerializedName("digital")
    val digital: String,
    @SerializedName("text")
    val text: String,
    @SerializedName("hours")
    val hours: Int,
    @SerializedName("minutes")
    val minutes: Int
)

data class WakaTimeProjectData(
    @SerializedName("name")
    val name: String,
    @SerializedName("total_seconds")
    val totalSeconds: Double,
    @SerializedName("percent")
    val percent: Double,
    @SerializedName("digital")
    val digital: String,
    @SerializedName("text")
    val text: String,
    @SerializedName("hours")
    val hours: Int,
    @SerializedName("minutes")
    val minutes: Int
)

data class WakaTimeEditorData(
    @SerializedName("name")
    val name: String,
    @SerializedName("total_seconds")
    val totalSeconds: Double,
    @SerializedName("percent")
    val percent: Double,
    @SerializedName("digital")
    val digital: String,
    @SerializedName("text")
    val text: String,
    @SerializedName("hours")
    val hours: Int,
    @SerializedName("minutes")
    val minutes: Int
)

data class WakaTimeOSData(
    @SerializedName("name")
    val name: String,
    @SerializedName("total_seconds")
    val totalSeconds: Double,
    @SerializedName("percent")
    val percent: Double,
    @SerializedName("digital")
    val digital: String,
    @SerializedName("text")
    val text: String,
    @SerializedName("hours")
    val hours: Int,
    @SerializedName("minutes")
    val minutes: Int
)

data class WakaTimeDailyData(
    @SerializedName("date")
    val date: String,
    @SerializedName("total_seconds")
    val totalSeconds: Double,
    @SerializedName("text")
    val text: String,
    @SerializedName("hours")
    val hours: Int,
    @SerializedName("minutes")
    val minutes: Int
)

data class WakaTimeHeartbeatsResponse(
    @SerializedName("data")
    val data: List<WakaTimeHeartbeatData>,
    @SerializedName("total")
    val total: Int,
    @SerializedName("page")
    val page: Int,
    @SerializedName("per_page")
    val perPage: Int,
    @SerializedName("total_pages")
    val totalPages: Int
)

data class WakaTimeHeartbeatData(
    @SerializedName("id")
    val id: String,
    @SerializedName("entity")
    val entity: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("category")
    val category: String,
    @SerializedName("time")
    val time: Double,
    @SerializedName("project")
    val project: String?,
    @SerializedName("language")
    val language: String?,
    @SerializedName("editor")
    val editor: String?,
    @SerializedName("operating_system")
    val operatingSystem: String?
)

data class WakaTimeProjectsResponse(
    @SerializedName("data")
    val data: List<WakaTimeProjectInfo>,
    @SerializedName("total")
    val total: Int
)

data class WakaTimeProjectInfo(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("privacy")
    val privacy: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("last_heartbeat_at")
    val lastHeartbeatAt: String,
    @SerializedName("repository")
    val repository: WakaTimeRepository?,
    @SerializedName("human_readable_last_heartbeat_at")
    val humanReadableLastHeartbeatAt: String,
    @SerializedName("total_seconds")
    val totalSeconds: Double,
    @SerializedName("heartbeat_count")
    val heartbeatCount: Int,
    @SerializedName("branches")
    val branches: List<String>,
    @SerializedName("languages")
    val languages: List<WakaTimeProjectLanguage>
)

data class WakaTimeRepository(
    @SerializedName("name")
    val name: String,
    @SerializedName("full_name")
    val fullName: String,
    @SerializedName("html_url")
    val htmlUrl: String
)

data class WakaTimeProjectLanguage(
    @SerializedName("name")
    val name: String,
    @SerializedName("seconds")
    val seconds: Double
)

// File activity data classes for detailed file breakdown
data class FileActivity(
    val fileName: String,
    val fullPath: String,
    val project: String,
    val language: String,
    val editor: String,
    val timeSpent: Long, // in seconds
    val lastModified: Double
)

class FileActivityBuilder(
    private val fileName: String,
    private val fullPath: String,
    private val project: String,
    private val language: String,
    private val editor: String,
    initialTime: Double
) {
    private var totalTime: Long = 0
    private var lastTime: Double = initialTime
    
    fun addTime(time: Double) {
        // Simple time accumulation - in real implementation you'd want
        // to be smarter about calculating actual time spent
        totalTime += 30 // Assume 30 seconds per heartbeat
        if (time > lastTime) lastTime = time
    }
    
    fun build(): FileActivity {
        return FileActivity(
            fileName = fileName,
            fullPath = fullPath,
            project = project,
            language = language,
            editor = editor,
            timeSpent = totalTime,
            lastModified = lastTime
        )
    }
}