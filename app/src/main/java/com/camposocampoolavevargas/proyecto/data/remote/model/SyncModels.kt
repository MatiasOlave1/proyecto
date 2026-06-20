package com.camposocampoolavevargas.proyecto.data.remote.model

import com.camposocampoolavevargas.proyecto.data.local.model.SleepQuality
import com.camposocampoolavevargas.proyecto.data.local.model.AchievementType
import com.google.gson.annotations.SerializedName

data class SleepRecordDto(
    @SerializedName("id") val id: String,
    @SerializedName("sleep_time") val sleepTime: Long,
    @SerializedName("wake_time") val wakeTime: Long,
    @SerializedName("duration_minutes") val durationMinutes: Int,
    @SerializedName("quality") val quality: SleepQuality,
    @SerializedName("date") val date: String
)

data class SleepSyncRequest(
    @SerializedName("records") val records: List<SleepRecordDto>
)

data class SleepSyncResponse(
    @SerializedName("synced_ids") val syncedIds: List<String>
)

data class WeeklyGoalDto(
    @SerializedName("id") val id: String,
    @SerializedName("iso_week") val isoWeek: Int,
    @SerializedName("iso_year") val isoYear: Int,
    @SerializedName("min_hours") val minHours: Float,
    @SerializedName("required_days") val requiredDays: Int,
    @SerializedName("bedtime_limit_millis") val bedtimeLimitMillis: Long
)

data class GoalSyncRequest(
    @SerializedName("goals") val goals: List<WeeklyGoalDto>
)

data class GoalSyncResponse(
    @SerializedName("synced_ids") val syncedIds: List<String>
)

data class AchievementDto(
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: AchievementType,
    @SerializedName("unlocked") val unlocked: Boolean,
    @SerializedName("unlocked_at") val unlockedAt: Long?,
    @SerializedName("points") val points: Int
)

data class AchievementSyncRequest(
    @SerializedName("achievements") val achievements: List<AchievementDto>
)

data class AchievementSyncResponse(
    @SerializedName("synced_ids") val syncedIds: List<String>
)

data class StreakDataDto(
    @SerializedName("current_streak") val currentStreak: Int,
    @SerializedName("max_streak") val maxStreak: Int,
    @SerializedName("last_updated_date") val lastUpdatedDate: String
)
