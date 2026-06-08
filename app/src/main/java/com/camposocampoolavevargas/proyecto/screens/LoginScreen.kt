package com.camposocampoolavevargas.proyecto.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    onRegisterClick: () -> Unit
) {

    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "DormiBienU",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Button(
                onClick = {

                    if (
                        correo == "admin@ucsc.cl" &&
                        password == "1234"
                    ) {
                        mensaje = "Login correcto"
                    } else {
                        mensaje = "Correo o contraseña incorrectos"
                    }

                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Ingresar")
            }

            OutlinedButton(
                onClick = {
                    onRegisterClick()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Registrar")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = mensaje)
    }
}