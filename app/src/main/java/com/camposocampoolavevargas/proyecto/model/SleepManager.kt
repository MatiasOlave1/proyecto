package com.camposocampoolavevargas.proyecto.model

object SleepManager {

    private val registros = mutableListOf<SleepRecord>()

    fun agregarRegistro(registro: SleepRecord) {
        registros.add(registro)
    }

    fun obtenerRegistros(): List<SleepRecord> {
        return registros
    }
}

