## Why

La aplicación cuenta con la definición técnica para la "Ventana de Desconexión" en sus especificaciones, pero esta funcionalidad no ha sido implementada en el código. Los usuarios no reciben actualmente los recordatorios preventivos necesarios para mejorar su higiene del sueño antes de acostarse.

## What Changes

- Implementación de un programador de alarmas (`AlarmManager`) para disparar notificaciones 90 minutos antes de la hora de acostarse configurada.
- Creación de un `BroadcastReceiver` para gestionar la recepción de la señal y la emisión de la notificación push.
- Configuración de un canal de notificaciones de "baja luminancia" para cumplir con los requisitos estéticos y funcionales de la desconexión.
- Integración con el repositorio de metas semanales para automatizar la programación del recordatorio cada vez que se actualiza una meta activa.

## Capabilities

### New Capabilities
- Ninguna.

### Modified Capabilities
- `05-alarmas-regulacion`: Se formaliza la implementación del escenario de "Ventana de Desconexión" ya existente en la especificación.

## Impact

- **Android System**: Requiere permisos de `SCHEDULE_EXACT_ALARM` y `POST_NOTIFICATIONS`.
- **Arquitectura**: Adición de `DisconnectReminderReceiver` y `DisconnectAlarmScheduler`.
- **Repositorios**: Modificación en `WeeklyGoalRepository` para disparar el agendamiento.
- **UI**: Conexión con `DisconnectReminderScreen`.
