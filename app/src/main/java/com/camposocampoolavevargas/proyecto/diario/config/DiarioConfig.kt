package com.camposocampoolavevargas.proyecto.diario.config

/**
 * ConfiguraciÃ³n centralizada y de solo lectura para el mÃ³dulo SPEC-07.
 * Agrupa todas las restricciones tÃ©cnicas, constantes de negocio y mensajes estÃ¡ndar.
 */
object DiarioConfig {

    // â”€â”€ LÃMITES DE VISUALIZACIÃ“N â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    object Visualizacion {
        /** Caracteres mÃ¡ximos a mostrar en la vista previa del historial (CAP-07-B). */
        const val MAX_CHARS_PREVIEW = 50

        /** MÃ¡ximo de caracteres permitidos en el campo de texto de entrada. */
        const val MAX_CHARS_ENTRADA = 2000
    }

    // â”€â”€ AUTO-ELIMINACIÃ“N (CAP-07-C) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    object AutoEliminacion {
        /** Hora de ejecuciÃ³n del WorkManager para auto-eliminar entradas (hora local). */
        const val HORA_EJECUCION = 6    // 06:00

        /** Nombre Ãºnico del trabajo periÃ³dico de WorkManager. */
        const val WORK_NAME = "DiarioAutoEliminarWork"

        /** Retraso mÃ¡ximo garantizado en minutos (Doze Mode). */
        const val MAX_RETRASO_MINUTOS = 15L
    }

    // â”€â”€ INTEGRACIÃ“N VENTANA NOCTURNA (CAP-07-D / CAP-05) â”€â”€â”€â”€
    object VentanaNocturna {
        /** Nivel de brillo de pantalla reducido al entrar en modo noche: 20%. */
        const val BRILLO_REDUCIDO = 0.20f

        /** Banner informativo mostrado durante la ventana nocturna. */
        const val BANNER_TEXTO = "Modo noche activo â€” tu entrada se borrarÃ¡ al despertar"

        /** Hora de inicio de la ventana nocturna (hora local). */
        const val HORA_INICIO = 21   // 21:00

        /** Hora de fin de la ventana nocturna (hora local). */
        const val HORA_FIN = 6       // 06:00
    }

    // â”€â”€ NOTIFICACIONES â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    object Notificacion {
        const val CHANNEL_ID   = "diario_preocupaciones_channel"
        const val CHANNEL_NAME = "Diario de Preocupaciones"
        const val NOTIFICATION_ID = 7001

        /** Texto mostrado en la notificaciÃ³n de auto-eliminaciÃ³n matutina. */
        const val TEXTO_AUTO_ELIMINACION = "Tu diario de anoche fue borrado automÃ¡ticamente"
    }

    // â”€â”€ MENSAJES DE UI â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    object Mensajes {
        const val ENTRADA_GUARDADA   = "Tu entrada fue guardada de forma segura"
        const val ENTRADA_VACIA      = "Escribe algo antes de guardar"
        const val ENTRADA_ELIMINADA  = "Entrada eliminada"
        const val CONFIRMAR_ELIMINAR = "Â¿Eliminar esta entrada del diario?"
    }
}
