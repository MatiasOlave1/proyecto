package com.camposocampoolavevargas.proyecto.ui.screens

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.Row
import com.camposocampoolavevargas.proyecto.navigation.Screen
import com.camposocampoolavevargas.proyecto.ui.theme.DormiBienUTheme

enum class HomeTab {
    Dashboard, Noche, Historial, Logros
}

@Composable
fun HomeScreen(
    navController: NavController,
    windowSizeClass: WindowSizeClass,
    viewModel: HomeViewModel = hiltViewModel()
) {
    HomeScreenContent(
        navController = navController,
        windowSizeClass = windowSizeClass,
        onLogout = { viewModel.logout() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    navController: NavController,
    windowSizeClass: WindowSizeClass,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(HomeTab.Dashboard) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val isExpanded = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (isExpanded) {
            // --- TABLET: NavigationRail lateral ---
            Scaffold(
                topBar = { HomeTopBar(selectedTab, onProfileClick = { showLogoutDialog = true }) }
            ) { paddingValues ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    NavigationRail {
                        NavigationRailItem(
                            selected = selectedTab == HomeTab.Dashboard,
                            onClick = { selectedTab = HomeTab.Dashboard },
                            icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                            label = { Text("Dashboard") }
                        )
                        NavigationRailItem(
                            selected = selectedTab == HomeTab.Noche,
                            onClick = { selectedTab = HomeTab.Noche },
                            icon = {
                                MoonIcon(
                                    color = if (selectedTab == HomeTab.Noche)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            label = { Text("Noche") }
                        )
                        NavigationRailItem(
                            selected = selectedTab == HomeTab.Historial,
                            onClick = { selectedTab = HomeTab.Historial },
                            icon = { Icon(Icons.Default.List, contentDescription = "Historial") },
                            label = { Text("Historial") }
                        )
                        NavigationRailItem(
                            selected = selectedTab == HomeTab.Logros,
                            onClick = { selectedTab = HomeTab.Logros },
                            icon = { Icon(Icons.Default.Star, contentDescription = "Logros") },
                            label = { Text("Logros") }
                        )
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        when (selectedTab) {
                            HomeTab.Dashboard -> DashboardTabContent(navController)
                            HomeTab.Noche -> NocheTabContent(navController)
                            HomeTab.Historial -> HistorialTabContent(navController)
                            HomeTab.Logros -> LogrosTabContent(navController)
                        }
                    }
                }
            }
        } else {
            // --- TELÉFONO: NavigationBar inferior ---
            Scaffold(
                topBar = { HomeTopBar(selectedTab, onProfileClick = { showLogoutDialog = true }) },
                bottomBar = {
                    NavigationBar {
                        NavigationBarItem(
                            selected = selectedTab == HomeTab.Dashboard,
                            onClick = { selectedTab = HomeTab.Dashboard },
                            icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                            label = { Text("Dashboard") }
                        )
                        NavigationBarItem(
                            selected = selectedTab == HomeTab.Noche,
                            onClick = { selectedTab = HomeTab.Noche },
                            icon = {
                                MoonIcon(
                                    color = if (selectedTab == HomeTab.Noche)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            label = { Text("Noche") }
                        )
                        NavigationBarItem(
                            selected = selectedTab == HomeTab.Historial,
                            onClick = { selectedTab = HomeTab.Historial },
                            icon = { Icon(Icons.Default.List, contentDescription = "Historial") },
                            label = { Text("Historial") }
                        )
                        NavigationBarItem(
                            selected = selectedTab == HomeTab.Logros,
                            onClick = { selectedTab = HomeTab.Logros },
                            icon = { Icon(Icons.Default.Star, contentDescription = "Logros") },
                            label = { Text("Logros") }
                        )
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    when (selectedTab) {
                        HomeTab.Dashboard -> DashboardTabContent(navController)
                        HomeTab.Noche -> NocheTabContent(navController)
                        HomeTab.Historial -> HistorialTabContent(navController)
                        HomeTab.Logros -> LogrosTabContent(navController)
                    }
                }
            }
        }

        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text("Cerrar Sesión", fontWeight = FontWeight.Bold) },
                text = { Text("¿Estás seguro de que deseas cerrar tu sesión actual?") },
                confirmButton = {
                    TextButton(onClick = {
                        showLogoutDialog = false
                        onLogout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }) {
                        Text("Cerrar Sesión", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(selectedTab: HomeTab, onProfileClick: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = when (selectedTab) {
                    HomeTab.Dashboard -> "DormiBienU"
                    HomeTab.Noche -> "Noche"
                    HomeTab.Historial -> "Historial"
                    HomeTab.Logros -> "Logros"
                },
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        actions = {
            IconButton(onClick = onProfileClick) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Perfil",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
fun MoonIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(24.dp)) {
        val path = Path().apply {
            moveTo(size.width * 0.8f, size.height * 0.15f)
            cubicTo(
                size.width * 0.35f, size.height * 0.05f,
                size.width * 0.15f, size.height * 0.45f,
                size.width * 0.35f, size.height * 0.8f
            )
            cubicTo(
                size.width * 0.5f, size.height * 0.92f,
                size.width * 0.75f, size.height * 0.9f,
                size.width * 0.85f, size.height * 0.75f
            )
            cubicTo(
                size.width * 0.6f, size.height * 0.65f,
                size.width * 0.5f, size.height * 0.35f,
                size.width * 0.8f, size.height * 0.15f
            )
            close()
        }
        drawPath(path, color = color)
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun HomeScreenPreview() {
    DormiBienUTheme {
        // Preview no puede usar WindowSizeClass real, usamos teléfono por defecto
    }
}