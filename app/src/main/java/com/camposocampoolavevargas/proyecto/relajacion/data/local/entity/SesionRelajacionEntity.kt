package com.camposocampoolavevargas.proyecto.relajacion.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sesion_relajacion")
data class SesionRelajacionEntity(
    @PrimaryKey
    val uuid: String,
    val userId: String,
    val tipo: String, // RESPIRACION | AUDIO
    val subtipo: String, // "4-7-8", "BOX", "COHERENTE", "RUIDO_BLANCO", "RUIDO_MARRON"
    val duracionSegundos: Int,
    val completada: Boolean,
    val audioActivo: Boolean,
    val iniciadoEn: String, // ISO 8601 UTC
    val finalizadoEn: String?, // ISO 8601 UTC — null si fue interrumpida
    val createdAt: Long = System.currentTimeMillis()
)
