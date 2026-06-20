package com.camposocampoolavevargas.proyecto.relajacion.config

object RelajacionConfig {
    
    // Respiración
    const val DURACION_INHALAR_4_7_8 = 4000L // ms
    const val DURACION_RETENER_4_7_8 = 7000L // ms
    const val DURACION_EXHALAR_4_7_8 = 8000L // ms
    const val DURACION_CICLO_4_7_8 = DURACION_INHALAR_4_7_8 + DURACION_RETENER_4_7_8 + DURACION_EXHALAR_4_7_8
    
    // Colores
    const val COLOR_INHALAR = 0xFF4A90D9
    const val COLOR_EXHALAR = 0xFF7EC8A4
    
    // Audio
    const val LATENCIA_AUDIO_MAX_MS = 300
    const val VOLUMEN_INICIAL = 0.7f
    const val VOLUMEN_DUCKING = 0.3f
    const val TIMEOUT_AUDIO_INTERRUMPIDO = 60000L // ms
    
    // Completación
    const val CICLOS_MINIMOS = 3
    
    // Brillo
    const val BRILLO_MODO_NOCHE = 0.2f
    const val BRILLO_NORMAL = 1f
    
    // Assets de audio (paths relativos)
    const val ASSET_RUIDO_BLANCO = "audios/ruido_blanco.mp3"
    const val ASSET_RUIDO_MARRON = "audios/ruido_marron.mp3"
}
