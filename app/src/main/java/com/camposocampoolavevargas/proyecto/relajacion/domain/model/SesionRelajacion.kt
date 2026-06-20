package com.dormibienu.app.relajacion.domain.model

/**
 * Representa los tipos de sesiones de relajación analógica disponibles.
 */
enum class TipoSesion {
    RESPIRACION,
    AUDIO
}

/**
 * Modelo de dominio puro para una sesión de relajación.
 * Libre de anotaciones de persistencia o dependencias de plataforma.
 */
data class SesionRelajacion(
    val uuid: String,
    val userId: String,
    val tipo: TipoSesion,
    val subtipo: String,
    val duracionSegundos: Int,
    val completada: Boolean,
    val audioActivo: Boolean,
    val iniciadoEn: String, // Formato ISO 8601 UTC
    val finalizadoEn: String? // Formato ISO 8601 UTC (null si fue interrumpida)
) {
    companion object {
        // Constantes estandarizadas para evitar strings mágicos en la app
        const val SUBTIPO_RESPIRACION_478 = "4-7-8"
        const val SUBTIPO_RESPIRACION_BOX = "BOX"
        const val SUBTIPO_RESPIRACION_COHERENTE = "COHERENTE"
        
        const val SUBTIPO_AUDIO_BLANCO = "RUIDO_BLANCO"
        const val SUBTIPO_AUDIO_MARRON = "RUIDO_MARRON"
    }
}