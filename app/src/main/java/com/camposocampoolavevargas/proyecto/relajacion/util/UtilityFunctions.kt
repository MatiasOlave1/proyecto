package com.camposocampoolavevargas.proyecto.relajacion.util

import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object TimeUtils {
    
    private val iso8601Formatter: DateTimeFormatter = 
        DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC)
    
    fun ahora(): Instant = Instant.now()
    
    fun instantAIso8601(instant: Instant): String {
        return instant.toString()
    }
    
    fun iso8601AInstant(iso8601String: String): Instant {
        return Instant.parse(iso8601String)
    }
    
    fun formatearDuracion(segundos: Int): String {
        val minutos = segundos / 60
        val seg = segundos % 60
        return String.format("%02d:%02d", minutos, seg)
    }
}

object UUIDUtils {
    fun generarUUID(): String = java.util.UUID.randomUUID().toString()
}
