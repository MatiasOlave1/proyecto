package com.camposocampoolavevargas.proyecto.util

import java.time.LocalDate
import java.time.temporal.WeekFields

object DateUtils {
    data class IsoWeekYear(val week: Int, val year: Int)

    /**
     * Computes the ISO 8601 week number and week-based year for the current local date.
     */
    fun getIsoWeekYear(): IsoWeekYear {
        val today = LocalDate.now()
        val weekFields = WeekFields.ISO
        val week = today.get(weekFields.weekOfWeekBasedYear())
        val year = today.get(weekFields.weekBasedYear())
        return IsoWeekYear(week, year)
    }
}
