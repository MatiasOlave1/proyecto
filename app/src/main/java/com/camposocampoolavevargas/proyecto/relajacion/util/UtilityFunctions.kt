package com.dormibienu.app.relajacion.util

import java.time.Instant
import java.time.Duration
import java.time.format.DateTimeParseException

/**
 * Funciones de utilidad centralizadas para el módulo de relajación,
 * enfocadas en el manejo preciso del tiempo en formato ISO 8601 UTC.
 */
object RelajacionUtils {

    /**
     * Genera un timestamp del momento actual en formato estricto ISO 8601 UTC.
     * Ejemplo: "2026-06-15T10:00:00Z"
     */
    fun obtenerTimestampActual(): String {
        return Instant.now().toString()
    }

    /**
     * Calcula la diferencia en segundos entre un timestamp de inicio y el momento actual.
     * Esencial para cuantificar el progreso acumulado en sesiones activas o interrupciones.
     */
    fun calcularDuracionHastaAhora(iniciadoEnHex: String?): Int {
        if (iniciadoEnHex.isNullOrEmpty()) return 0
        return try {
            val inicio = Instant.parse(iniciadoEnHex)
            val ahora = Instant.now()
            Duration.between(inicio, ahora).seconds.toInt()
        } catch (e: DateTimeParseException) {
            0
        }
    }

    /**
     * Calcula los segundos transcurridos exactos entre dos marcas de tiempo ISO 8601 UTC.
     */
    fun calcularDiferenciaSegundos(iniciadoEn: String, finalizadoEn: String): Int {
        return try {
            val inicio = Instant.parse(iniciadoEn)
            val fin = Instant.parse(finalizadoEn)
            Duration.between(inicio, fin).seconds.toInt()
        } catch (e: DateTimeParseException) {
            0
        }
    }
}