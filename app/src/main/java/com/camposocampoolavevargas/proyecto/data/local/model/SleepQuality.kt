package com.camposocampoolavevargas.proyecto.data.local.model

/**
 * Enum representing the quality of sleep recorded by the user.
 */
enum class SleepQuality(val displayName: String) {
    VERY_BAD("Muy Mal"),
    BAD("Mal"),
    REGULAR("Regular"),
    GOOD("Bien"),
    EXCELLENT("Excelente")
}