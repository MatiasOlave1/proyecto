package com.camposocampoolavevargas.proyecto.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.camposocampoolavevargas.proyecto.ui.screens.AchievementsScreen
import com.camposocampoolavevargas.proyecto.ui.screens.AlarmCalculatorScreen
import com.camposocampoolavevargas.proyecto.ui.screens.CircadianAlertScreen
import com.camposocampoolavevargas.proyecto.ui.screens.DashboardScreen
import com.camposocampoolavevargas.proyecto.ui.screens.DisconnectReminderScreen
import com.camposocampoolavevargas.proyecto.ui.screens.HomeScreen
import com.camposocampoolavevargas.proyecto.ui.screens.JournalScreen
import com.camposocampoolavevargas.proyecto.ui.screens.LoginScreen
import com.camposocampoolavevargas.proyecto.ui.screens.RegisterScreen
import com.camposocampoolavevargas.proyecto.ui.screens.RelaxLibraryScreen
import com.camposocampoolavevargas.proyecto.ui.screens.SleepHistoryScreen
import com.camposocampoolavevargas.proyecto.ui.screens.SleepLogScreen
import com.camposocampoolavevargas.proyecto.ui.screens.StreaksScreen
import com.camposocampoolavevargas.proyecto.ui.screens.WeeklyGoalsScreen

/**
 * Main App Navigation Host defining the application routing graph.
 * Configured to start at [Screen.Login] and maps all 14 screens.
 */
@Composable
fun AppNavigation(startDestination: String? = null) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination ?: Screen.Login.route
    ) {
        // Auth
        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(Screen.Register.route) { RegisterScreen(navController) }

        // Main Hub
        composable(Screen.Home.route) { HomeScreen(navController) }

        // Tracking
        composable(Screen.SleepLog.route) { SleepLogScreen(navController) }
        composable(Screen.SleepHistory.route) { SleepHistoryScreen(navController) }

        // Goals & Gamification
        composable(Screen.WeeklyGoals.route) { WeeklyGoalsScreen(navController) }
        composable(Screen.Achievements.route) { AchievementsScreen(navController) }
        composable(Screen.Streaks.route) { StreaksScreen(navController) }

        // Analytics
        composable(Screen.Dashboard.route) { DashboardScreen(navController) }
        composable(Screen.CircadianAlert.route) { CircadianAlertScreen(navController) }

        // Wellness
        composable(Screen.AlarmCalculator.route) { AlarmCalculatorScreen(navController) }
        composable(Screen.DisconnectReminder.route) { DisconnectReminderScreen(navController) }
        composable(Screen.Journal.route) { JournalScreen(navController) }
        composable(Screen.RelaxLibrary.route) {
            val context = androidx.compose.ui.platform.LocalContext.current
            val userSession = androidx.compose.runtime.remember {
                dagger.hilt.android.EntryPointAccessors.fromApplication(context, AppNavigationEntryPoint::class.java).userSession()
            }
            val userId = userSession.getActiveUserId()
            if (userId == null) {
                androidx.compose.runtime.LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route)
                }
            } else {
                val viewModel: com.camposocampoolavevargas.proyecto.relajacion.ui.viewmodel.RelajacionViewModel = androidx.hilt.navigation.compose.hiltViewModel()
                com.camposocampoolavevargas.proyecto.relajacion.ui.screens.RelajacionScreen(
                    viewModel = viewModel,
                    userId = userId,
                    onSessionCompleted = { 
                        navController.popBackStack()
                    },
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@dagger.hilt.EntryPoint
@dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
interface AppNavigationEntryPoint {
    fun userSession(): com.camposocampoolavevargas.proyecto.data.local.UserSession
}

