package com.dormibienu.app.relajacion.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dormibienu.app.relajacion.domain.model.SesionRelajacion
import com.dormibienu.app.relajacion.domain.model.TipoSesion

/**
 * Entidad de Room que representa la tabla de sesiones de relajación en la base de datos local.
 */
@Entity(tableName = "sesiones_relajacion")
data class SesionRelajacionEntity(
    @PrimaryKey
    @ColumnInfo(name = "uuid")
    val uuid: String,
    
    @ColumnInfo(name = "user_id")
    val userId: String,
    
    @ColumnInfo(name = "tipo")
    val tipo: String, // Se guarda como String (RESPIRACION | AUDIO)
    
    @ColumnInfo(name = "subtipo")
    val subtipo: String,
    
    @ColumnInfo(name = "duracion_segundos")
    val duracionSegundos: Int,
    
    @ColumnInfo(name = "completada")
    val completada: Boolean,
    
    @ColumnInfo(name = "audio_activo")
    val audioActivo: Boolean,
    
    @ColumnInfo(name = "iniciado_en")
    val iniciadoEn: String, // ISO 8601 UTC
    
    @ColumnInfo(name = "finalizado_en")
    val finalizadoEn: String? // ISO 8601 UTC (null si fue interrumpida)
)

/**
 * Función de extensión para mapear la entidad de la base de datos al modelo de dominio.
 */
fun SesionRelajacionEntity.toDomain(): SesionRelajacion {
    return SesionRelajacion(
        uuid = this.uuid,
        userId = this.userId,
        tipo = try {
            TipoSesion.valueOf(this.tipo)
        } catch (e: IllegalArgumentException) {
            TipoSesion.RESPIRACION // Fallback seguro
        },
        subtipo = this.subtipo,
        duracionSegundos = this.duracionSegundos,
        completada = this.completada,
        audioActivo = this.audioActivo,
        iniciadoEn = this.iniciadoEn,
        finalizadoEn = this.finalizadoEn
    )
}

/**
 * Función de extensión para mapear un modelo de dominio a la entidad de la base de datos.
 */
fun SesionRelajacion.toEntity(): SesionRelajacionEntity {
    return SesionRelajacionEntity(
        uuid = this.uuid,
        userId = this.userId,
        tipo = this.tipo.name,
        subtipo = this.subtipo,
        duracionSegundos = this.duracionSegundos,
        completada = this.completada,
        audioActivo = this.audioActivo,
        iniciadoEn = this.iniciadoEn,
        finalizadoEn = this.finalizadoEn
    )
}