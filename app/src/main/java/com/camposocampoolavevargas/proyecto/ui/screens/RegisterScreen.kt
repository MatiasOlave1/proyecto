package com.camposocampoolavevargas.proyecto.ui.screens

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.camposocampoolavevargas.proyecto.navigation.Screen
import com.camposocampoolavevargas.proyecto.ui.BaseViewModel.UiState
import com.camposocampoolavevargas.proyecto.ui.theme.DormiBienUTheme
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Screen for Registration (RF01 — Registro).
 * Hooks into RegisterViewModel, validates form fields, and saves to database.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val registerState by viewModel.registerState.collectAsState()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var birthDateText by remember { mutableStateOf("") } // YYYY-MM-DD
    var region by remember { mutableStateOf("") }
    var commune by remember { mutableStateOf("") }
    var university by remember { mutableStateOf("") }
    var career by remember { mutableStateOf("") }
    
    var localError by remember { mutableStateOf<String?>(null) }

    // Redirect to Home when registration is successful
    LaunchedEffect(registerState) {
        if (registerState is UiState.Success && (registerState as UiState.Success<String>).data.isNotEmpty()) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = "Registro Universitario", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Text(
                        text = "Crea tu Cuenta",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 30.sp
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                    
                    Text(
                        text = "Registra tus datos locales para gestionar tu higiene de sueño de forma 100% offline.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it; localError = null },
                                label = { Text("Nombre Completo") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it; localError = null },
                                label = { Text("Correo Institucional / Personal") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                            )

                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it; localError = null },
                                label = { Text("Contraseña") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                            )

                            OutlinedTextField(
                                value = birthDateText,
                                onValueChange = { birthDateText = it; localError = null },
                                label = { Text("Fecha de Nacimiento (AAAA-MM-DD)") },
                                placeholder = { Text("Ej: 2002-05-15") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = region,
                                onValueChange = { region = it; localError = null },
                                label = { Text("Región") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = commune,
                                onValueChange = { commune = it; localError = null },
                                label = { Text("Comuna") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = university,
                                onValueChange = { university = it; localError = null },
                                label = { Text("Universidad") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = career,
                                onValueChange = { career = it; localError = null },
                                label = { Text("Carrera") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Show error messages
                    val currentError = localError ?: (registerState as? UiState.Error)?.message
                    if (currentError != null) {
                        Text(
                            text = currentError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.padding(bottom = 8.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }

                    if (registerState is UiState.Loading) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    } else {
                        Button(
                            onClick = {
                                // Simple local date parsing validation
                                val birthDateMillis = try {
                                    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                    format.parse(birthDateText.trim())?.time ?: 0L
                                } catch (e: Exception) {
                                    -1L
                                }

                                if (birthDateMillis == -1L) {
                                    localError = "El formato de fecha debe ser AAAA-MM-DD (ej: 2001-12-30)."
                                } else {
                                    viewModel.register(
                                        name = name,
                                        birthDate = birthDateMillis,
                                        region = region,
                                        commune = commune,
                                        university = university,
                                        career = career,
                                        email = email,
                                        password = password
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(text = "Registrar cuenta", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.padding(bottom = 24.dp)
                    ) {
                        Text(text = "¿Ya tienes cuenta? Iniciar Sesión", color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun RegisterScreenPreview() {
    DormiBienUTheme {
        RegisterScreen(navController = rememberNavController())
    }
}
