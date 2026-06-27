package com.camposocampoolavevargas.proyecto.diario.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.camposocampoolavevargas.proyecto.diario.config.DiarioConfig
import com.camposocampoolavevargas.proyecto.diario.domain.usecase.AutoEliminarEntradasUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * Worker para la auto-eliminaciÃ³n matutina de entradas del diario (SPEC-07 / CAP-07-C).
 *
 * Se ejecuta diariamente a las 06:00 hora local, compatible con Doze Mode
 * (usa [PeriodicWorkRequestBuilder] que es Doze-aware).
 *
 * Marca con Soft Delete todas las entradas donde [autoEliminar] = true.
 * Si hay eliminaciones, emite una notificaciÃ³n informativa (canal silencioso).
 *
 * Restricciones de diseÃ±o:
 * - Cero llamadas a red: toda operaciÃ³n es local.
 * - Usa [AutoEliminarEntradasUseCase] para mantener la separaciÃ³n de capas.
 * - La polÃ­tica [ExistingPeriodicWorkPolicy.KEEP] evita duplicados.
 */
@HiltWorker
class AutoEliminarWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val autoEliminarUseCase: AutoEliminarEntradasUseCase
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val eliminadas = autoEliminarUseCase()

            if (eliminadas > 0) {
                mostrarNotificacion(eliminadas)
            }

            Result.success()
        } catch (e: Exception) {
            // Reintento automÃ¡tico por WorkManager en el siguiente ciclo
            Result.retry()
        }
    }

    /**
     * Emite una notificaciÃ³n silenciosa informando al usuario
     * cuÃ¡ntas entradas fueron auto-eliminadas esta maÃ±ana.
     */
    private fun mostrarNotificacion(cantidadEliminadas: Int) {
        val notifManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
            as NotificationManager

        // Crear canal si es Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                DiarioConfig.Notificacion.CHANNEL_ID,
                DiarioConfig.Notificacion.CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW   // Silencioso, sin sonido
            ).apply {
                description = "Notificaciones del diario de preocupaciones"
                enableVibration(false)
            }
            notifManager.createNotificationChannel(canal)
        }

        val texto = if (cantidadEliminadas == 1)
            DiarioConfig.Notificacion.TEXTO_AUTO_ELIMINACION
        else
            "$cantidadEliminadas entradas de tu diario fueron borradas automÃ¡ticamente"

        val notif = NotificationCompat.Builder(context, DiarioConfig.Notificacion.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_delete)
            .setContentTitle("DormiBienU â€” Diario")
            .setContentText(texto)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()

        notifManager.notify(DiarioConfig.Notificacion.NOTIFICATION_ID, notif)
    }

    companion object {
        /**
         * Programa el worker periÃ³dico diario de 24h.
         * Usa [ExistingPeriodicWorkPolicy.KEEP] para no duplicar trabajos existentes.
         * El Worker es Doze-aware: WorkManager respeta restricciones de baterÃ­a
         * con un margen de [DiarioConfig.AutoEliminacion.MAX_RETRASO_MINUTOS].
         */
        fun programar(context: Context) {
            val request = PeriodicWorkRequestBuilder<AutoEliminarWorker>(
                repeatInterval = 24L,
                repeatIntervalTimeUnit = TimeUnit.HOURS
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                DiarioConfig.AutoEliminacion.WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        /**
         * Cancela el worker periÃ³dico (por ejemplo, si el mÃ³dulo estÃ¡ deshabilitado).
         */
        fun cancelar(context: Context) {
            WorkManager.getInstance(context)
                .cancelUniqueWork(DiarioConfig.AutoEliminacion.WORK_NAME)
        }
    }
}
