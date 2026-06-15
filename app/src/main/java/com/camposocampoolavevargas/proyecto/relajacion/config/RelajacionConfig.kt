package com.dormibienu.app.relajacion.config

/**
 * Configuración centralizada y de solo lectura para el módulo SPEC-06.
 * Agrupa todas las restricciones técnicas, tiempos y constantes de estilo visual.
 */
object RelajacionConfig {

    // --- CONFIGURACIÓN DE RESPIRACIÓN (MÉTODO 4-7-8) ---
    object Respiracion478 {
        const val TIEMPO_INHALAR_MS = 4000L
        const val TIEMPO_RETENER_MS = 7000L
        const val TIEMPO_EXHALAR_MS = 8000L
        const val TOTAL_CICLO_MS = TIEMPO_INHALAR_MS + TIEMPO_RETENER_MS + TIEMPO_EXHALAR_MS // 19 Segundos
        
        // Códigos de color hexadecimales estipulados por especificación
        const val COLOR_INHALAR_HEX = 0xFF4A90D9 // #4A90D9 (Azul Expansión)
        const val COLOR_EXHALAR_HEX = 0xFF7EC8A4 // #7EC8A4 (Verde Contracción)
        const val COLOR_RETENER_HEX = 0xFF6C7A89 // Color neutro para pulso suave
        
        const val MIN_CICLOS_COMPLETADOS = 3
    }

    // --- CONFIGURACIÓN DE AUDIO LOCAL ---
    object Audio {
        const val PATH_RUIDO_BLANCO = "audio/ruido_blanco.mp3"
        const val PATH_RUIDO_MARRON = "audio/ruido_marron.mp3"
        
        const val VOLUMEN_INICIAL_FACTOR = 0.70f // 70% del volumen del sistema
        const val VOLUMEN_DUCKING_FACTOR = 0.30f  // 30% del volumen durante interrupción
        
        const val MAX_TIEMPO_INTERRUPCION_FOCO_SEG = 60L
        const val LATENCIA_MAX_INICIO_PERMITIDA_MS = 300L
    }

    // --- INTEGRACIÓN VENTANA NOCTURNA (CAP-05) ---
    object VentanaNocturna {
        const val BRILLO_PANTALLA_REDUCIDO = 0.20f // 20% de brillo automático
        const val BANNER_TEXTO = "Modo noche activo — pantalla en mínimo brillo"
    }

    // --- NOTIFICACIONES DEL FOREGROUND SERVICE ---
    object Notification {
        const val CHANNEL_ID = "relajacion_audio_channel"
        const val CHANNEL_NAME = "Frecuencias de Relajación"
        const val NOTIFICATION_ID = 4006
    }
}