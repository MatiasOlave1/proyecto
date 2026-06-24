package com.camposocampoolavevargas.proyecto.diario.util

import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * Funciones de utilidad para el mÃ³dulo Diario de Preocupaciones (SPEC-07).
 * Enfocadas en el manejo preciso de timestamps ISO 8601 UTC y la lÃ³gica
 * de ventana nocturna. Cero dependencias externas.
 */
object DiarioUtils {

    // â”€â”€ Formateadores â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    private val formatterFecha     = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val formatterHoraLocal = DateTimeFormatter.ofPattern("HH:mm")
    private val formatterCompleto  = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")

    /**
     * Genera el timestamp actual en formato ISO 8601 UTC estricto.
     * Ejemplo: "2026-06-24T14:00:00Z"
     */
    fun obtenerTimestampUtc(): String = Instant.now().toString()

    /**
     * Convierte un timestamp ISO 8601 UTC a fecha legible en zona local.
     * Ejemplo: "24/06/2026"
     */
    fun formatearFecha(isoTimestamp: String): String {
        return try {
            val instant = Instant.parse(isoTimestamp)
            val zdt     = instant.atZone(ZoneId.systemDefault())
            zdt.format(formatterFecha)
        } catch (e: DateTimeParseException) {
            "--/--/----"
        }
    }

    /**
     * Convierte un timestamp ISO 8601 UTC a hora local legible.
     * Ejemplo: "23:45"
     */
    fun formatearHora(isoTimestamp: String): String {
        return try {
            val instant = Instant.parse(isoTimestamp)
            val zdt     = instant.atZone(ZoneId.systemDefault())
            zdt.format(formatterHoraLocal)
        } catch (e: DateTimeParseException) {
            "--:--"
        }
    }

    /**
     * Convierte un timestamp ISO 8601 UTC a formato completo legible.
     * Ejemplo: "24 Jun 2026, 23:45"
     */
    fun formatearCompleto(isoTimestamp: String): String {
        return try {
            val instant = Instant.parse(isoTimestamp)
            val zdt     = instant.atZone(ZoneId.systemDefault())
            zdt.format(formatterCompleto)
        } catch (e: DateTimeParseException) {
            "Fecha invÃ¡lida"
        }
    }

    /**
     * Determina si el momento actual se encuentra dentro de la ventana
     * nocturna definida (por defecto 21:00 â€“ 06:00 hora local).
     *
     * La lÃ³gica maneja el cruce de medianoche:
     * - Si [horaInicio] > [horaFin]: la ventana cruza medianoche
     *   (ej. 21:00 â†’ 06:00 del dÃ­a siguiente).
     * - Si [horaInicio] < [horaFin]: ventana dentro del mismo dÃ­a.
     */
    fun esVentanaNocturna(
        horaInicio: Int = 21,
        horaFin: Int    = 6
    ): Boolean {
        val ahora = LocalTime.now(ZoneId.systemDefault())
        val inicio = LocalTime.of(horaInicio, 0)
        val fin    = LocalTime.of(horaFin, 0)

        return if (inicio.isAfter(fin)) {
            // Cruza medianoche: 21:00 â†’ 06:00
            ahora.isAfter(inicio) || ahora.isBefore(fin)
        } else {
            // Dentro del mismo dÃ­a
            ahora.isAfter(inicio) && ahora.isBefore(fin)
        }
    }

    /**
     * Calcula los milisegundos hasta las 06:00 del dÃ­a siguiente (hora local).
     * Ãštil para programar el WorkManager con el delay correcto.
     */
    fun milisHasta06h00(): Long {
        val ahora      = ZonedDateTime.now(ZoneId.systemDefault())
        var objetivo   = ahora.toLocalDate().atTime(6, 0)
            .atZone(ZoneId.systemDefault())

        if (!ahora.toLocalTime().isBefore(java.time.LocalTime.of(6, 0))) {
            objetivo = objetivo.plusDays(1)
        }

        return java.time.Duration.between(ahora, objetivo).toMillis()
            .coerceAtLeast(0L)
    }
}
