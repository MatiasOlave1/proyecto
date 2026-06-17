package com.camposocampoolavevargas.proyecto.screens


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.camposocampoolavevargas.proyecto.model.SleepManager

@Composable
fun SleepHistoryScreen() {

    val registros = SleepManager.obtenerRegistros()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Historial de Sueño",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (registros.isEmpty()) {

            Text("No existen registros")

        } else {

            LazyColumn {

                items(registros) { registro ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {

                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {

                            Text("Fecha: ${registro.fecha}")

                            Text("Hora dormir: ${registro.horaDormir}")

                            Text("Hora despertar: ${registro.horaDespertar}")

                            Text("Horas dormidas: ${registro.horasDormidas}")

                            Text("Calidad: ${registro.calidadSueno}")
                        }
                    }
                }
            }
        }
    }
}
}