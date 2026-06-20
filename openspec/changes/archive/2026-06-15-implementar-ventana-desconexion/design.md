|## Context

La funcionalidad de "Ventana de Desconexión" requiere disparar una notificación en un momento preciso del día (90 minutos antes de la hora de dormir). Dado que el dispositivo puede estar en modo de ahorro de energía (Doze Mode) en ese momento, se requiere una solución robusta para la programación de tareas en segundo plano en Android.

## Goals / Non-Goals

**Goals:**
- Disparar la notificación exactamente 90 minutos antes de la meta de sueño.
- Asegurar la persistencia del recordatorio tras reinicios del dispositivo.
- Implementar un diseño de notificación que use colores tenues (baja luminancia).

**Non-Goals:**
- Implementar la activación automática del filtro de luz azul del sistema (solo se sugerirá al usuario).
- Modificar el comportamiento de las alarmas de despertar.

## Decisions

### 1. Uso de AlarmManager + BroadcastReceiver
Se ha decidido usar `AlarmManager` con `setExactAndAllowWhileIdle` en lugar de `WorkManager`.
- **Razón**: `WorkManager` está orientado a tareas que pueden esperar, mientras que `AlarmManager` es ideal para eventos que deben ocurrir en un momento específico de tiempo de pared (wall-clock time).
- **Alternativa**: `WorkManager` con `setInitialDelay`. Se descartó porque Android puede retrasar la ejecución para agrupar tareas y ahorrar batería, lo cual podría hacer que el recordatorio llegue tarde.

### 2. Integración en el Repositorio de Metas
La lógica de programación se activará cada vez que se guarde una meta semanal.
- **Razón**: Centraliza la fuente de verdad. Si la meta cambia, el recordatorio se recalcula automáticamente.

### 3. Canal de Notificaciones de Baja Prioridad
Se creará un canal con `IMPORTANCE_LOW`.
- **Razón**: Evita sonidos intrusivos o vibraciones fuertes que podrían interrumpir la fase de pre-sueño, alineándose con el concepto de "baja luminancia".

## Risks / Trade-offs

- **[Riesgo] Restricciones de batería (Doze Mode)** → **Mitigación**: Uso de `setExactAndAllowWhileIdle` y solicitud del permiso `SCHEDULE_EXACT_ALARM`.
- **[Riesgo] Reinicio del dispositivo** → **Mitigación**: Registrar un `BOOT_COMPLETED` receiver para reprogramar las alertas si existe una meta activa.
- **[Riesgo] Precisión horaria** → **Mitigación**: La lógica manejará correctamente el desbordamiento de medianoche (ej: acostarse a las 00:30 implica alerta a las 23:00 del día anterior).
