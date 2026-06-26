package com.camposocampoolavevargas.proyecto.navigation

/**
 * Sealed class representing all screen navigation routes in the DormiBienU application.
 */
sealed class Screen(val route: String) {
    // Auth
    object Register : Screen("register")
    object Login : Screen("login")

    // Main
    object Home : Screen("home")

    // Tracking
    object SleepLog : Screen("sleep_log")
    object SleepHistory : Screen("sleep_history")

    // Goals & Gamification
    object WeeklyGoals : Screen("weekly_goals")
    object Achievements : Screen("achievements")
    object Streaks : Screen("streaks")

    // Analytics
    object Dashboard : Screen("dashboard")
    object CircadianAlert : Screen("circadian_alert")

    // Wellness
    object AlarmCalculator : Screen("alarm_calculator")
    object DisconnectReminder : Screen("disconnect_reminder")
    object Journal : Screen("journal")

    object Diario : Screen("diario")
    object RelaxLibrary : Screen("relax_library")
}

