package com.camposocampoolavevargas.proyecto.data.remote

import com.camposocampoolavevargas.proyecto.data.remote.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<AuthResponse>

    @POST("login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthResponse>

    @GET("sleep-records")
    suspend fun getSleepRecords(): Response<List<SleepRecordDto>>

    @POST("sleep-records")
    suspend fun saveSleepRecord(
        @Body record: SleepRecordDto
    ): Response<SleepRecordDto>

    @POST("sleep-records/sync")
    suspend fun syncSleepRecords(
        @Body request: SleepSyncRequest
    ): Response<SleepSyncResponse>

    @GET("weekly-goals/current")
    suspend fun getCurrentGoal(
        @Query("iso_week") isoWeek: Int?,
        @Query("iso_year") isoYear: Int?
    ): Response<WeeklyGoalDto?>

    @POST("weekly-goals")
    suspend fun saveWeeklyGoal(
        @Body goal: WeeklyGoalDto
    ): Response<WeeklyGoalDto>

    @POST("weekly-goals/sync")
    suspend fun syncWeeklyGoals(
        @Body request: GoalSyncRequest
    ): Response<GoalSyncResponse>

    @GET("achievements")
    suspend fun getAchievements(): Response<List<AchievementDto>>

    @POST("achievements/unlock")
    suspend fun unlockAchievement(
        @Body achievement: AchievementDto
    ): Response<AchievementDto>

    @POST("achievements/sync")
    suspend fun syncAchievements(
        @Body request: AchievementSyncRequest
    ): Response<AchievementSyncResponse>

    @GET("streaks")
    suspend fun getStreak(): Response<StreakDataDto>

    @POST("streaks")
    suspend fun saveStreak(
        @Body streak: StreakDataDto
    ): Response<StreakDataDto>
}
