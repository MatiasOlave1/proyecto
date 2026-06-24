package com.camposocampoolavevargas.proyecto.diario.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.camposocampoolavevargas.proyecto.diario.domain.model.EntradaDiario

/**
 * Entidad de Room que mapea la tabla fÃ­sica de almacenamiento para el diario.
 * Implementa la estructura de la SPEC-07 con soporte integrado para Soft Delete.
 */
@Entity(tableName = "entradas_diario")
data class EntradaDiarioEntity(
    @PrimaryKey
    @ColumnInfo(name = "uuid")
    val uuid: String,
    
    @ColumnInfo(name = "user_id")
    val userId: String,
    
    @ColumnInfo(name = "contenido")
    val contenido: String,
    
    @ColumnInfo(name = "fecha_entrada")
    val fechaEntrada: String, // ISO 8601 UTC
    
    @ColumnInfo(name = "auto_eliminar")
    val autoEliminar: Boolean,
    
    @ColumnInfo(name = "eliminada")
    val eliminada: Boolean = false, // Soft Delete por defecto en false
    
    @ColumnInfo(name = "creado_en")
    val creadoEn: String, // ISO 8601 UTC
    
    @ColumnInfo(name = "eliminado_en")
    val eliminadoEn: String? = null // ISO 8601 UTC (null si estÃ¡ activa)
)

/**
 * Mapeador de conversiÃ³n: Capa de Datos -> Capa de Dominio (Desacoplamiento).
 */
fun EntradaDiarioEntity.toDomain(): EntradaDiario {
    return EntradaDiario(
        uuid = this.uuid,
        userId = this.userId,
        contenido = this.contenido,
        fechaEntrada = this.fechaEntrada,
        autoEliminar = this.autoEliminar,
        eliminada = this.eliminada,
        creadoEn = this.creadoEn,
        eliminadoEn = this.eliminadoEn
    )
}

/**
 * Mapeador de conversiÃ³n: Capa de Dominio -> Capa de Datos (Persistencia).
 */
fun EntradaDiario.toEntity(): EntradaDiarioEntity {
    return EntradaDiarioEntity(
        uuid = this.uuid,
        userId = this.userId,
        contenido = this.contenido,
        fechaEntrada = this.fechaEntrada,
        autoEliminar = this.autoEliminar,
        eliminada = this.eliminada,
        creadoEn = this.creadoEn,
        eliminadoEn = this.eliminadoEn
    )
}
