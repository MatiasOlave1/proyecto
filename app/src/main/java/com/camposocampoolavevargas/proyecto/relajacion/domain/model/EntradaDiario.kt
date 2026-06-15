package com.dormibienu.app.diario.domain.model

/**
 * Modelo de dominio puro para una entrada en el Diario de Preocupaciones.
 * Completamente desacoplado de anotaciones de frameworks (Room, Gson, etc.).
 */
data class EntradaDiario(
    val uuid: String,            // UUID v4 generado en cliente
    val userId: String,          // Identificador del perfil de usuario activo
    val contenido: String,       // Texto libre sin análisis semántico
    val fechaEntrada: String,    // ISO 8601 UTC
    val autoEliminar: Boolean,   // Bandera para purga matutina automática
    val eliminada: Boolean,      // Flag para Soft Delete
    val creadoEn: String,        // ISO 8601 UTC
    val eliminadoEn: String?     // ISO 8601 UTC (null por defecto)
) {
    /**
     * Devuelve una muestra recortada del contenido para optimizar el rendimiento de renderizado
     * en listados o componentes de vista previa del historial.
     */
    fun obtenerMuestraContenido(limite: Int = 50): String {
        return if (contenido.length <= limite) {
            contenido
        } else {
            contenido.take(limite) + "..."
        }
    }
}