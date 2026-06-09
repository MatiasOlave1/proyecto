package com.camposocampoolavevargas.proyecto.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.camposocampoolavevargas.proyecto.model.SleepManager
import com.camposocampoolavevargas.proyecto.model.SleepRecord

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepRegisterScreen() {

    var fecha by remember { mutableStateOf("") }
    var horaDormir by remember { mutableStateOf("") }
    var horaDespertar by remember { mutableStateOf("") }

    var calidad by remember { mutableStateOf("") }

    var mensaje by remember { mutableStateOf("") }
    var colorMensaje by remember { mutableStateOf(Color.White) }

    var expanded by remember { mutableStateOf(false) }

    val opcionesCalidad = listOf(
        "Muy malo",
        "Malo",
        "Regular",
        "Buena",
        "Excelente"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Registro Diario de Sueño",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = fecha,
            onValueChange = { fecha = it },
            label = { Text("Fecha") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = horaDormir,
            onValueChange = { horaDormir = it },
            label = { Text("Hora de dormir (0-23)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = horaDespertar,
            onValueChange = { horaDespertar = it },
            label = { Text("Hora de despertar (0-23)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {

            OutlinedTextField(
                value = calidad,
                onValueChange = {},
                readOnly = true,
                label = { Text("Calidad del sueño") },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {

                opcionesCalidad.forEach { opcion ->

                    DropdownMenuItem(
                        text = {
                            Text(opcion)
                        },
                        onClick = {
                            calidad = opcion
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {

                if (
                    fecha.isBlank() ||
                    horaDormir.isBlank() ||
                    horaDespertar.isBlank() ||
                    calidad.isBlank()
                ) {

                    mensaje = "Debe completar todos los campos"
                    colorMensaje = Color.Red

                } else {

                    val dormir = horaDormir.toIntOrNull()
                    val despertar = horaDespertar.toIntOrNull()

                    if (
                        dormir == null ||
                        despertar == null ||
                        dormir !in 0..23 ||
                        despertar !in 0..23
                    ) {

                        mensaje = "Ingrese horas válidas"
                        colorMensaje = Color.Red

                    } else {

                        var horasDormidas = despertar - dormir

                        if (horasDormidas < 0) {
                            horasDormidas += 24
                        }

                        val registro = SleepRecord(
                            fecha = fecha,
                            horaDormir = horaDormir,
                            horaDespertar = horaDespertar,
                            horasDormidas = horasDormidas.toDouble(),
                            calidadSueno = calidad
                        )

                        SleepManager.agregarRegistro(registro)

                        mensaje =
                            "Registro guardado ($horasDormidas horas)"
                        colorMensaje = Color.Blue

                        fecha = ""
                        horaDormir = ""
                        horaDespertar = ""
                        calidad = ""
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar Registro")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = mensaje,
            color = colorMensaje
        )
    }
}