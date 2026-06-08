package com.camposocampoolavevargas.proyecto.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sleep_records")
data class SleepRecord(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val fecha: String,

    val horaDormir: String,

    val horaDespertar: String,

    val horasDormidas: Double,

    val calidadSueno: String
)

