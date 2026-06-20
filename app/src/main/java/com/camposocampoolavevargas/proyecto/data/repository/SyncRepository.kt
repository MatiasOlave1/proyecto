package com.camposocampoolavevargas.proyecto.data.repository

import android.content.Context
import android.util.Log
import com.camposocampoolavevargas.proyecto.data.local.UserSession
import com.camposocampoolavevargas.proyecto.data.local.dao.*
import com.camposocampoolavevargas.proyecto.data.local.entity.*
import com.camposocampoolavevargas.proyecto.data.local.model.SyncStatus
import com.camposocampoolavevargas.proyecto.data.remote.ApiService
import com.camposocampoolavevargas.proyecto.data.remote.model.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import com.camposocampoolavevargas.proyecto.data.local.HashUtils
import com.camposocampoolavevargas.proyecto.util.DateUtils

@Singleton
class SyncRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiService: ApiService,
    private val userDao: UserDao,
    private val sleepRecordDao: SleepRecordDao,
    private val weeklyGoalDao: WeeklyGoalDao,
    private val achievementDao: AchievementDao,
    private val streakDataDao: StreakDataDao,
    private val userSession: UserSession
) {
    private val tag = "SyncRepository"

    /**
     * Authenticates a user on the Laravel API, saves credentials locally, and stores the Sanctum token.
     */
    suspend fun login(emailOrPhone: String, password: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.login(LoginRequest(emailOrPhone, password))
            if (response.isSuccessful && response.body() != null) {
                val authData = response.body()!!
                
                // Save to local session
                userSession.login(authData.user.id)
                userSession.saveToken(authData.accessToken)

                // Save/update user entity locally
                val userEntity = UserEntity(
                    userId = authData.user.id,
                    email = authData.user.email,
                    passwordHash = HashUtils.hashPassword(password), // Store proper hash for offline auth
                    phone = authData.user.phone,
                    name = authData.user.name,
                    birthDate = authData.user.birthDate,
                    region = authData.user.region,
                    commune = authData.user.commune,
                    university = authData.user.university,
                    career = authData.user.career
                )
                userDao.insertUser(userEntity)

                // Pull and sync remote data
                syncAll(authData.user.id)

                Result.success(authData.user.id)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Credenciales incorrectas"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(tag, "Login error", e)
            Result.failure(e)
        }
    }

    /**
     * Registers a new user on the Laravel API, saves user locally, and stores the Sanctum token.
     */
    suspend fun register(
        userId: String,
        email: String,
        passwordHash: String,
        phone: String? = null,
        name: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val request = RegisterRequest(
                id = userId,
                email = email,
                password = passwordHash,
                phone = phone,
                name = name
            )
            val response = apiService.register(request)
            if (response.isSuccessful && response.body() != null) {
                val authData = response.body()!!
                
                userSession.login(authData.user.id)
                userSession.saveToken(authData.accessToken)

                val userEntity = UserEntity(
                    userId = authData.user.id,
                    email = authData.user.email,
                    passwordHash = passwordHash,
                    phone = authData.user.phone,
                    name = authData.user.name
                )
                userDao.insertUser(userEntity)

                Result.success(authData.user.id)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error en registro"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(tag, "Register error", e)
            Result.failure(e)
        }
    }

    /**
     * Saves a sleep record locally as PENDING, and immediately attempts to sync to API.
     */
    suspend fun saveSleepRecord(record: SleepRecordEntity) = withContext(Dispatchers.IO) {
        // Insert locally first
        sleepRecordDao.insertRecord(record.copy(syncStatus = SyncStatus.PENDING))

        // Sync immediately
        try {
            val dto = SleepRecordDto(
                id = record.recordId,
                sleepTime = record.sleepTime,
                wakeTime = record.wakeTime,
                durationMinutes = record.durationMinutes,
                quality = record.quality,
                date = record.date
            )
            val response = apiService.saveSleepRecord(dto)
            if (response.isSuccessful) {
                sleepRecordDao.insertRecord(record.copy(syncStatus = SyncStatus.SYNCED))
                Log.d(tag, "Sleep record synced successfully: ${record.recordId}")
            }
        } catch (e: Exception) {
            Log.w(tag, "Failed to sync sleep record: ${record.recordId}, kept as PENDING", e)
        }
    }

    /**
     * Syncs all pending sleep records to API, and pulls records from API to local storage.
     */
    suspend fun syncSleepRecords(userId: String) = withContext(Dispatchers.IO) {
        // 1. Push pending
        val pendingRecords = sleepRecordDao.getRecordsByUserIdDirect(userId)
            .filter { it.syncStatus == SyncStatus.PENDING }

        if (pendingRecords.isNotEmpty()) {
            try {
                val dtos = pendingRecords.map {
                    SleepRecordDto(
                        id = it.recordId,
                        sleepTime = it.sleepTime,
                        wakeTime = it.wakeTime,
                        durationMinutes = it.durationMinutes,
                        quality = it.quality,
                        date = it.date
                    )
                }
                val response = apiService.syncSleepRecords(SleepSyncRequest(dtos))
                if (response.isSuccessful && response.body() != null) {
                    val syncedIds = response.body()!!.syncedIds
                    for (record in pendingRecords) {
                        if (syncedIds.contains(record.recordId)) {
                            sleepRecordDao.insertRecord(record.copy(syncStatus = SyncStatus.SYNCED))
                        }
                    }
                    Log.d(tag, "Synced ${syncedIds.size} pending sleep records")
                }
            } catch (e: Exception) {
                Log.e(tag, "Error pushing pending sleep records", e)
            }
        }

        // 2. Pull remote records
        try {
            val response = apiService.getSleepRecords()
            if (response.isSuccessful) {
                val remoteRecords = response.body()
                if (remoteRecords != null) {
                    for (dto in remoteRecords) {
                        if (dto.id == null || dto.date == null) continue
                        val localRecord = sleepRecordDao.getRecordByDateDirect(userId, dto.date)
                        if (localRecord == null) {
                            sleepRecordDao.insertRecord(
                                SleepRecordEntity(
                                    recordId = dto.id,
                                    userId = userId,
                                    sleepTime = dto.sleepTime,
                                    wakeTime = dto.wakeTime,
                                    durationMinutes = dto.durationMinutes,
                                    quality = dto.quality,
                                    date = dto.date,
                                    syncStatus = SyncStatus.SYNCED
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Error pulling sleep records from server", e)
        }
    }

    /**
     * Saves a weekly goal locally and pushes to API.
     */
    suspend fun saveWeeklyGoal(goal: WeeklyGoalEntity) = withContext(Dispatchers.IO) {
        weeklyGoalDao.insertGoal(goal)
        try {
            val dto = WeeklyGoalDto(
                id = goal.goalId,
                isoWeek = goal.isoWeek,
                isoYear = goal.isoYear,
                minHours = goal.minHours,
                requiredDays = goal.requiredDays,
                bedtimeLimitMillis = goal.bedtimeLimitMillis
            )
            apiService.saveWeeklyGoal(dto)
        } catch (e: Exception) {
            Log.w(tag, "Error syncing weekly goal, saved locally only", e)
        }
    }

    /**
     * Pulls goals from API and stores them locally.
     */
    suspend fun syncWeeklyGoals(userId: String) = withContext(Dispatchers.IO) {
        // Push local goals
        val localGoals = weeklyGoalDao.getGoalsByUserIdDirect(userId)
        if (localGoals.isNotEmpty()) {
            try {
                val dtos = localGoals.map {
                    WeeklyGoalDto(
                        id = it.goalId,
                        isoWeek = it.isoWeek,
                        isoYear = it.isoYear,
                        minHours = it.minHours,
                        requiredDays = it.requiredDays,
                        bedtimeLimitMillis = it.bedtimeLimitMillis
                    )
                }
                apiService.syncWeeklyGoals(GoalSyncRequest(dtos))
            } catch (e: Exception) {
                Log.e(tag, "Error syncing local weekly goals", e)
            }
        }

        // Pull remote goals
        try {
            val (currentWeek, currentYear) = DateUtils.getIsoWeekYear()
            val response = apiService.getCurrentGoal(currentWeek, currentYear)
            if (response.isSuccessful) {
                val dto = response.body()
                if (dto != null && dto.id != null) {
                    val existing = weeklyGoalDao.getCurrentGoalDirect(userId, dto.isoWeek, dto.isoYear)
                    if (existing == null) {
                        weeklyGoalDao.insertGoal(
                            WeeklyGoalEntity(
                                goalId = dto.id,
                                userId = userId,
                                isoWeek = dto.isoWeek,
                                isoYear = dto.isoYear,
                                minHours = dto.minHours,
                                requiredDays = dto.requiredDays,
                                bedtimeLimitMillis = dto.bedtimeLimitMillis
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Error pulling weekly goals", e)
        }
    }

    /**
     * Saves achievements locally and pushes to API.
     */
    suspend fun saveAchievement(achievement: AchievementEntity) = withContext(Dispatchers.IO) {
        achievementDao.insertAchievement(achievement)
        if (achievement.unlocked) {
            // Also call unlock to update local DB if pre-seeded row already exists
            achievementDao.unlockAchievement(
                achievement.userId,
                achievement.type,
                achievement.unlockedAt ?: System.currentTimeMillis(),
                achievement.points
            )
            try {
                val dto = AchievementDto(
                    id = achievement.achievementId,
                    type = achievement.type,
                    unlocked = achievement.unlocked,
                    unlockedAt = achievement.unlockedAt,
                    points = achievement.points
                )
                apiService.unlockAchievement(dto)
            } catch (e: Exception) {
                Log.w(tag, "Error syncing achievement unlock", e)
            }
        }
    }

    /**
     * Syncs achievements locally and remotely.
     */
    suspend fun syncAchievements(userId: String) = withContext(Dispatchers.IO) {
        // Push local achievements
        val localAchievements = achievementDao.getAchievementsByUserIdDirect(userId)
        if (localAchievements.isNotEmpty()) {
            try {
                val dtos = localAchievements.map {
                    AchievementDto(
                        id = it.achievementId,
                        type = it.type,
                        unlocked = it.unlocked,
                        unlockedAt = it.unlockedAt,
                        points = it.points
                    )
                }
                apiService.syncAchievements(AchievementSyncRequest(dtos))
            } catch (e: Exception) {
                Log.e(tag, "Error pushing achievements", e)
            }
        }

        // Pull achievements
        try {
            val response = apiService.getAchievements()
            if (response.isSuccessful && response.body() != null) {
                val remoteAchievements = response.body()!!
                for (dto in remoteAchievements) {
                    if (dto.id == null) continue
                    val local = achievementDao.getAchievementByTypeDirect(userId, dto.type)
                    if (local == null) {
                        achievementDao.insertAchievement(
                            AchievementEntity(
                                achievementId = dto.id,
                                userId = userId,
                                type = dto.type,
                                unlocked = dto.unlocked,
                                unlockedAt = dto.unlockedAt,
                                points = dto.points
                            )
                        )
                    } else if (!local.unlocked && dto.unlocked) {
                        achievementDao.unlockAchievement(userId, dto.type, dto.unlockedAt ?: System.currentTimeMillis(), dto.points)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Error pulling achievements", e)
        }
    }

    /**
     * Saves streak data locally and pushes to API.
     */
    suspend fun saveStreak(streak: StreakDataEntity) = withContext(Dispatchers.IO) {
        streakDataDao.insertOrUpdateStreak(streak)
        try {
            val dto = StreakDataDto(
                currentStreak = streak.currentStreak,
                maxStreak = streak.maxStreak,
                lastUpdatedDate = streak.lastUpdatedDate
            )
            apiService.saveStreak(dto)
        } catch (e: Exception) {
            Log.w(tag, "Error syncing streak data", e)
        }
    }

    /**
     * Pulls streak data from API and saves locally.
     */
    suspend fun syncStreak(userId: String) = withContext(Dispatchers.IO) {
        // Push local streak
        val localStreak = streakDataDao.getStreakByUserDirect(userId)
        if (localStreak != null) {
            try {
                val dto = StreakDataDto(
                    currentStreak = localStreak.currentStreak,
                    maxStreak = localStreak.maxStreak,
                    lastUpdatedDate = localStreak.lastUpdatedDate
                )
                apiService.saveStreak(dto)
            } catch (e: Exception) {
                Log.e(tag, "Error pushing streak data", e)
            }
        }

        // Pull remote streak
        try {
            val response = apiService.getStreak()
            if (response.isSuccessful) {
                val dto = response.body()
                if (dto != null && dto.lastUpdatedDate != null) {
                    val existing = streakDataDao.getStreakByUserDirect(userId)
                    if (existing == null) {
                        streakDataDao.insertOrUpdateStreak(
                            StreakDataEntity(
                                userId = userId,
                                currentStreak = dto.currentStreak,
                                maxStreak = dto.maxStreak,
                                lastUpdatedDate = dto.lastUpdatedDate
                            )
                        )
                    } else {
                        val newCurrent = maxOf(dto.currentStreak, existing.currentStreak)
                        val newMax = maxOf(dto.maxStreak, existing.maxStreak)
                        if (newCurrent != existing.currentStreak || newMax != existing.maxStreak) {
                            streakDataDao.insertOrUpdateStreak(
                                existing.copy(
                                    currentStreak = newCurrent,
                                    maxStreak = newMax,
                                    lastUpdatedDate = dto.lastUpdatedDate
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Error pulling streak data", e)
        }
    }

    /**
     * Syncs all data in order (first credentials, then sleep, goals, streaks, achievements).
     */
    suspend fun syncAll(userId: String) {
        try {
            syncSleepRecords(userId)
            syncWeeklyGoals(userId)
            syncStreak(userId)
            syncAchievements(userId)
        } catch (e: Exception) {
            Log.e(tag, "Error in general syncAll", e)
        }
    }
}
