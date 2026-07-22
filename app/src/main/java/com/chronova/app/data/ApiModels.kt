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

// ==================== Goals ====================

data class GoalsResponse(@SerializedName("data") val data: List<Goal>)

data class Goal(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("type") val type: String,
    @SerializedName("seconds") val seconds: Double,
    @SerializedName("delta") val delta: String,
    @SerializedName("is_enabled") val isEnabled: Boolean,
    @SerializedName("chart_data") val chartData: List<GoalChartData>?,
    @SerializedName("status") val status: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("language") val language: String?,
    @SerializedName("project") val project: String?,
    @SerializedName("category") val category: String?,
    @SerializedName("difficulty") val difficulty: String?,
    @SerializedName("priority") val priority: Int?,
    @SerializedName("tags") val tags: String?
)

data class GoalChartData(
    @SerializedName("date") val date: String,
    @SerializedName("actual_seconds") val actualSeconds: Double,
    @SerializedName("goal_seconds") val goalSeconds: Double,
    @SerializedName("range_start") val rangeStart: String?,
    @SerializedName("range_end") val rangeEnd: String?
)

data class GoalCreateRequest(
    @SerializedName("title") val title: String,
    @SerializedName("type") val type: String,
    @SerializedName("seconds") val seconds: Double,
    @SerializedName("delta") val delta: String,
    @SerializedName("language") val language: String? = null,
    @SerializedName("project") val project: String? = null,
    @SerializedName("category") val category: String? = null,
    @SerializedName("difficulty") val difficulty: String? = null,
    @SerializedName("priority") val priority: Int? = null
)

data class GoalResponse(
    @SerializedName("id") val id: String,
    @SerializedName("chart_data") val chartData: List<GoalChartData>?
)

data class DeleteGoalResponse(@SerializedName("message") val message: String)

data class GoalSuggestionsResponse(@SerializedName("data") val data: List<GoalSuggestion>)

data class GoalSuggestion(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("type") val type: String,
    @SerializedName("seconds") val seconds: Double,
    @SerializedName("delta") val delta: String,
    @SerializedName("confidence") val confidence: Double,
    @SerializedName("reasoning") val reasoning: String,
    @SerializedName("language") val language: String?,
    @SerializedName("project") val project: String?,
    @SerializedName("category") val category: String?
)

// ==================== Leaderboard ====================

data class LeadersResponse(
    @SerializedName("current_user") val currentUser: LeaderEntry?,
    @SerializedName("data") val data: List<LeaderEntry>,
    @SerializedName("page") val page: Int,
    @SerializedName("total_pages") val totalPages: Int,
    @SerializedName("range") val range: String,
    @SerializedName("language") val language: String?
)

data class LeaderEntry(
    @SerializedName("rank") val rank: Int,
    @SerializedName("running_total") val runningTotal: LeaderRunningTotal,
    @SerializedName("user") val leaderUser: LeaderUser
)

data class LeaderRunningTotal(
    @SerializedName("total_seconds") val totalSeconds: Double,
    @SerializedName("human_readable_total") val humanReadableTotal: String,
    @SerializedName("daily_average") val dailyAverage: Double,
    @SerializedName("human_readable_daily_average") val humanReadableDailyAverage: String,
    @SerializedName("range") val range: String
)

data class LeaderUser(
    @SerializedName("id") val id: String,
    @SerializedName("username") val username: String,
    @SerializedName("display_name") val displayName: String?,
    @SerializedName("avatar_url") val avatarUrl: String?,
    @SerializedName("subscriptionStatus") val subscriptionStatus: String?,
    @SerializedName("isProComped") val isProComped: Boolean?
)

// ==================== AI Analytics ====================

data class AiAnalyticsResponse(@SerializedName("data") val data: AiAnalyticsData)

data class AiAnalyticsData(
    @SerializedName("adoptionTimeline") val adoptionTimeline: List<AiAdoptionPoint>,
    @SerializedName("contributionShare") val contributionShare: AiContributionShare,
    @SerializedName("comparison") val comparison: AiComparison,
    @SerializedName("languageMatrix") val languageMatrix: List<LanguageAiMatrixEntry>,
    @SerializedName("projectDependency") val projectDependency: List<ProjectAiDependencyEntry>,
    @SerializedName("efficiencyTrend") val efficiencyTrend: List<AiEfficiencyTrendPoint>
)

data class AiAdoptionPoint(
    @SerializedName("date") val date: String,
    @SerializedName("aiSeconds") val aiSeconds: Double,
    @SerializedName("manualSeconds") val manualSeconds: Double
)

data class AiContributionShare(
    @SerializedName("aiPercent") val aiPercent: Double,
    @SerializedName("manualPercent") val manualPercent: Double,
    @SerializedName("aiHours") val aiHours: Double,
    @SerializedName("manualHours") val manualHours: Double
)

data class AiComparison(
    @SerializedName("ai") val ai: AiComparisonBucket,
    @SerializedName("manual") val manual: AiComparisonBucket
)

data class AiComparisonBucket(
    @SerializedName("totalHours") val totalHours: Double,
    @SerializedName("languageCount") val languageCount: Int,
    @SerializedName("projectCount") val projectCount: Int,
    @SerializedName("topLanguages") val topLanguages: List<AiTopLanguage>
)

data class AiTopLanguage(
    @SerializedName("name") val name: String,
    @SerializedName("seconds") val seconds: Double
)

data class LanguageAiMatrixEntry(
    @SerializedName("language") val language: String,
    @SerializedName("aiSeconds") val aiSeconds: Double,
    @SerializedName("manualSeconds") val manualSeconds: Double,
    @SerializedName("totalSeconds") val totalSeconds: Double
)

data class ProjectAiDependencyEntry(
    @SerializedName("project") val project: String,
    @SerializedName("aiPercent") val aiPercent: Double,
    @SerializedName("totalSeconds") val totalSeconds: Double,
    @SerializedName("aiSeconds") val aiSeconds: Double
)

data class AiEfficiencyTrendPoint(
    @SerializedName("date") val date: String,
    @SerializedName("aiPercent") val aiPercent: Double
)

// ==================== Focus Analytics ====================

data class FocusAnalyticsResponse(@SerializedName("data") val data: FocusAnalyticsData)

data class FocusAnalyticsData(
    @SerializedName("concentrationScore") val concentrationScore: Double,
    @SerializedName("totalCodingTime") val totalCodingTime: Double,
    @SerializedName("contextSwitches") val contextSwitches: ContextSwitches,
    @SerializedName("deepWorkBlocks") val deepWorkBlocks: DeepWorkBlocks,
    @SerializedName("projectDistribution") val projectDistribution: List<ProjectDistribution>,
    @SerializedName("topProject") val topProject: TopProject?
)

data class ContextSwitches(
    @SerializedName("total") val total: Int,
    @SerializedName("perHour") val perHour: Double,
    @SerializedName("recent") val recent: List<ContextSwitch>
)

data class ContextSwitch(
    @SerializedName("fromProject") val fromProject: String,
    @SerializedName("toProject") val toProject: String,
    @SerializedName("timestampFormatted") val timestampFormatted: String
)

data class DeepWorkBlocks(
    @SerializedName("blocks") val blocks: List<DeepWorkBlock>,
    @SerializedName("totalBlocks") val totalBlocks: Int,
    @SerializedName("totalDeepWorkHours") val totalDeepWorkHours: Double
)

data class DeepWorkBlock(
    @SerializedName("project") val project: String,
    @SerializedName("duration") val duration: Double,
    @SerializedName("startTimeFormatted") val startTimeFormatted: String,
    @SerializedName("endTimeFormatted") val endTimeFormatted: String
)

data class ProjectDistribution(
    @SerializedName("name") val name: String,
    @SerializedName("totalSeconds") val totalSeconds: Double,
    @SerializedName("percent") val percent: Double,
    @SerializedName("hours") val hours: Int,
    @SerializedName("minutes") val minutes: Int
)

data class TopProject(
    @SerializedName("name") val name: String,
    @SerializedName("percent") val percent: Double
)