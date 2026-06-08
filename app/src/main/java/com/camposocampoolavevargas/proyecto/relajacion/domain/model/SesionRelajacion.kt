package com.camposocampoolavevargas.proyecto.relajacion.domain.model

import java.time.Instant

data class SesionRelajacion(
    val uuid: String,
    val userId: String,
    val tipo: TipoRelajacion,
    val subtipo: String,
    val duracionSegundos: Int,
    val completada: Boolean,
    val audioActivo: Boolean,
    val iniciadoEn: Instant,
    val finalizadoEn: Instant? = null
)

enum class TipoRelajacion {
    RESPIRACION,
    AUDIO
}

enum class SubtipoRelajacion(val displayName: String, val duracionCiclo: Int) {
    RESPIRACION_4_7_8("Respiración 4-7-8", 19),
    RESPIRACION_BOX("Box Breathing", 16),
    RESPIRACION_COHERENTE("Respiración Coherente", 12),
    AUDIO_RUIDO_BLANCO("Ruido Blanco", 0),
    AUDIO_RUIDO_MARRON("Ruido Marrón", 0);

    val isAudio: Boolean get() = this.name.startsWith("AUDIO_")
    val isRespiracion: Boolean get() = this.name.startsWith("RESPIRACION_")
}
