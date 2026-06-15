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
                text = "Registro Diario de Sueño",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = fecha,
                onValueChange = { fecha = it },
                label = { Text("Fecha (dd/MM/yyyy)") },
                placeholder = { Text("15/06/2026") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = horaDormir,
                onValueChange = { horaDormir = it },
                label = { Text("Hora de dormir (HH:mm)") },
                placeholder = { Text("23:00") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = horaDespertar,
                onValueChange = { horaDespertar = it },
                label = { Text("Hora de despertar (HH:mm)") },
                placeholder = { Text("07:00") },
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

                        val partesDormir = horaDormir.split(":")
                        val partesDespertar = horaDespertar.split(":")

                        if (
                            partesDormir.size != 2 ||
                            partesDespertar.size != 2
                        ) {

                            mensaje = "Formato de hora inválido"
                            colorMensaje = Color.Red

                        } else {

                            val horaDormirInt = partesDormir[0].toIntOrNull()
                            val minutoDormirInt = partesDormir[1].toIntOrNull()

                            val horaDespertarInt = partesDespertar[0].toIntOrNull()
                            val minutoDespertarInt = partesDespertar[1].toIntOrNull()

                            if (
                                horaDormirInt == null ||
                                minutoDormirInt == null ||
                                horaDespertarInt == null ||
                                minutoDespertarInt == null ||
                                horaDormirInt !in 0..23 ||
                                horaDespertarInt !in 0..23 ||
                                minutoDormirInt !in 0..59 ||
                                minutoDespertarInt !in 0..59
                            ) {

                                mensaje = "Ingrese horas válidas"
                                colorMensaje = Color.Red

                            } else {

                                val minutosDormir =
                                    horaDormirInt * 60 + minutoDormirInt

                                val minutosDespertar =
                                    horaDespertarInt * 60 + minutoDespertarInt

                                var diferencia =
                                    minutosDespertar - minutosDormir

                                if (diferencia < 0) {
                                    diferencia += 24 * 60
                                }

                                val horasDormidas =
                                    diferencia / 60.0

                                val registro = SleepRecord(
                                    fecha = fecha,
                                    horaDormir = horaDormir,
                                    horaDespertar = horaDespertar,
                                    horasDormidas = horasDormidas,
                                    calidadSueno = calidad
                                )

                                SleepManager.agregarRegistro(registro)

                                mensaje =
                                    "Registro guardado (${String.format("%.1f", horasDormidas)} horas)"

                                colorMensaje = Color.Blue

                                fecha = ""
                                horaDormir = ""
                                horaDespertar = ""
                                calidad = ""
                            }
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
}