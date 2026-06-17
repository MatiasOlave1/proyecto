package com.camposocampoolavevargas.proyecto.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.camposocampoolavevargas.proyecto.UserManager

@Composable
fun UserRegisterScreen(
    onBackToLogin: () -> Unit
) {

    var nombre by remember { mutableStateOf("") }
    var fechaNacimiento by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("") }
    var comuna by remember { mutableStateOf("") }
    var universidad by remember { mutableStateOf("") }
    var carrera by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmarPassword by remember { mutableStateOf("") }

    var mensaje by remember { mutableStateOf("") }
    var colorMensaje by remember { mutableStateOf(Color.Black) }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Registro de Usuario",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre") }
        )

        OutlinedTextField(
            value = fechaNacimiento,
            onValueChange = { fechaNacimiento = it },
            label = { Text("Fecha de Nacimiento") }
        )

        OutlinedTextField(
            value = region,
            onValueChange = { region = it },
            label = { Text("Región") }
        )

        OutlinedTextField(
            value = comuna,
            onValueChange = { comuna = it },
            label = { Text("Comuna") }
        )

        OutlinedTextField(
            value = universidad,
            onValueChange = { universidad = it },
            label = { Text("Universidad") }
        )

        OutlinedTextField(
            value = carrera,
            onValueChange = { carrera = it },
            label = { Text("Carrera") }
        )

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo") }
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation()
        )

        OutlinedTextField(
            value = confirmarPassword,
            onValueChange = { confirmarPassword = it },
            label = { Text("Confirmar Contraseña") },
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {

                if (
                    nombre.isBlank() ||
                    fechaNacimiento.isBlank() ||
                    region.isBlank() ||
                    comuna.isBlank() ||
                    universidad.isBlank() ||
                    carrera.isBlank() ||
                    correo.isBlank() ||
                    password.isBlank() ||
                    confirmarPassword.isBlank()
                ) {

                    mensaje = "Debe completar todos los campos"
                    colorMensaje = Color.Red

                } else if (password != confirmarPassword) {

                    mensaje = "Las contraseñas no coinciden"
                    colorMensaje = Color.Red

                } else {

                    mensaje = "Usuario registrado correctamente"
                    colorMensaje = Color.Blue
                    UserManager.correoRegistrado = correo

                    UserManager.passwordRegistrada = password
                    scope.launch {

                        delay(2000)

                        onBackToLogin()

                    }
                }
            }
        ) {
            Text("Guardar Usuario")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = mensaje,
            color = colorMensaje
        )
    }
}