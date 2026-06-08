package com.camposocampoolavevargas.proyecto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.camposocampoolavevargas.proyecto.screens.LoginScreen
import com.camposocampoolavevargas.proyecto.screens.UserRegisterScreen
import com.camposocampoolavevargas.proyecto.ui.theme.ProyectoTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ProyectoTheme {

                var mostrarRegistro by remember {
                    mutableStateOf(false)
                }

                if (mostrarRegistro) {

                    UserRegisterScreen(
                        onBackToLogin = {
                            mostrarRegistro = false
                        }
                    )

                } else {

                    LoginScreen(
                        onRegisterClick = {
                            mostrarRegistro = true
                        }
                    )
                }
            }
        }
    }
}