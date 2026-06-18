package com.camposocampoolavevargas.proyecto.ui.screens

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.camposocampoolavevargas.proyecto.data.local.UserSession
import com.camposocampoolavevargas.proyecto.data.local.dao.AchievementDao
import com.camposocampoolavevargas.proyecto.data.local.dao.StreakDataDao
import com.camposocampoolavevargas.proyecto.data.local.entity.AchievementEntity
import com.camposocampoolavevargas.proyecto.data.local.model.AchievementType
import com.camposocampoolavevargas.proyecto.ui.BaseViewModel
import com.camposocampoolavevargas.proyecto.util.NotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Achievements tab (RF08).
 * Manages achievement loading, DB seeding, and combines data with active user streaks.
 */
@HiltViewModel
class AchievementsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val achievementDao: AchievementDao,
    private val streakDataDao: StreakDataDao,
    private val userSession: UserSession
) : BaseViewModel() {

    private val _userId = userSession.getActiveUserId() ?: ""

    // Flow for active user's streak statistics
    private val streakFlow = streakDataDao.getStreakByUser(_userId)

    // Flow for active user's achievements list
    private val achievementsFlow = achievementDao.getAchievementsByUser(_userId)

    /**
     * UI State class grouping achievements, current/max streaks and loading status.
     */
    data class AchievementsUiState(
        val achievements: List<AchievementEntity> = emptyList(),
        val currentStreak: Int = 0,
        val maxStreak: Int = 0,
        val isLoading: Boolean = true
    )

    // Combine flows to produce a single StateFlow of AchievementsUiState
    val uiState: StateFlow<AchievementsUiState> = combine(
        achievementsFlow,
        streakFlow
    ) { achievements, streakData ->
        AchievementsUiState(
            achievements = achievements,
            currentStreak = streakData?.currentStreak ?: 0,
            maxStreak = streakData?.maxStreak ?: 0,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AchievementsUiState(isLoading = true)
    )

    init {
        initializeAchievementsIfNeeded()
    }

    /**
     * Seed logic: if no achievements exist for the active user, pre-populate them as locked.
     */
    private fun initializeAchievementsIfNeeded() {
        viewModelScope.launch {
            if (_userId.isEmpty()) return@launch

            try {
                // Get the first emitted list to check if achievements need seeding
                val achievements = achievementsFlow.first()
                if (achievements.isEmpty()) {
                    for (type in AchievementType.values()) {
                        val points = when (type) {
                            AchievementType.FIRST_RECORD -> 50
                            AchievementType.DISCIPLINE_5_DAYS -> 100
                            AchievementType.PERFECT_WEEK -> 150
                            AchievementType.STREAK_10 -> 200
                            AchievementType.STREAK_30 -> 500
                            AchievementType.EASTER_EGG -> 0
                        }
                        achievementDao.insertAchievement(
                            AchievementEntity(
                                userId = _userId,
                                type = type,
                                unlocked = false,
                                unlockedAt = null,
                                points = points
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private val clickTimestamps = mutableListOf<Long>()

    /**
     * Handles clicks on the test button.
     * Evaluates if the button was clicked 5 times within a 10-second window.
     */
    fun onTestButtonClick() {
        val now = System.currentTimeMillis()
        // Remove clicks older than 10 seconds (10,000 milliseconds)
        clickTimestamps.removeAll { now - it > 10000 }
        clickTimestamps.add(now)

        if (clickTimestamps.size >= 5) {
            unlockEasterEgg()
            clickTimestamps.clear()
        }
    }

    /**
     * Unlocks the Easter Egg testing achievement and increments its count (stored in points).
     * Since it can be unlocked multiple times, it increments the count on each successful cycle.
     */
    private fun unlockEasterEgg() {
        viewModelScope.launch {
            if (_userId.isEmpty()) return@launch
            try {
                // Ensure the row exists in database (due to seed, it should, but insert as ignore for safety)
                achievementDao.insertAchievement(
                    AchievementEntity(
                        userId = _userId,
                        type = AchievementType.EASTER_EGG,
                        unlocked = false,
                        points = 0
                    )
                )

                // Fetch current points to increment the claim count (each claim adds 10 points)
                val achievements = achievementsFlow.first()
                val egg = achievements.firstOrNull { it.type == AchievementType.EASTER_EGG }
                val currentPoints = egg?.points ?: 0
                val newPoints = currentPoints + 10

                // Unlock/update achievement
                achievementDao.unlockAchievement(
                    userId = _userId,
                    type = AchievementType.EASTER_EGG,
                    unlockedAt = System.currentTimeMillis(),
                    points = newPoints
                )

                // Trigger push notification
                val claimsCount = newPoints / 10
                NotificationHelper.showAchievementNotification(
                    context = context,
                    title = "¡Easter Egg Desbloqueado! 🥚",
                    message = "Has reclamado el logro de prueba. Total: $claimsCount veces."
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
