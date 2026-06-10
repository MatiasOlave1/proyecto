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
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
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
        composable(Screen.RelaxLibrary.route) { RelaxLibraryScreen(navController) }
    }
}

