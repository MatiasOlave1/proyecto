package com.camposocampoolavevargas.proyecto.model


data class Usuario(
    val nombre: String,
    val fechaNacimiento: String,
    val region: String,
    val comuna: String,
    val universidad: String,
    val carrera: String,
    val correo: String,
    val password: String
)