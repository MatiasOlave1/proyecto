package com.camposocampoolavevargas.proyecto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.camposocampoolavevargas.proyecto.screens.SleepRegisterScreen
import com.camposocampoolavevargas.proyecto.ui.theme.ProyectoTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ProyectoTheme {
                SleepRegisterScreen()
            }
        }
    }
}