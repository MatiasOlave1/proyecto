## 1. Configuración de Android y Permisos

- [x] 1.1 Añadir permisos `POST_NOTIFICATIONS` y `SCHEDULE_EXACT_ALARM` en `AndroidManifest.xml`.
- [x] 1.2 Declarar el `DisconnectReminderReceiver` en el `AndroidManifest.xml`.

## 2. Infraestructura de Notificaciones

- [x] 2.1 Crear el canal de notificaciones `DISCONNECT_CHANNEL` en la clase Application o mediante un NotificationManager inyectado.
- [x] 2.2 Implementar `DisconnectReminderReceiver` para construir y mostrar la notificación push de baja luminancia.

## 3. Lógica de Programación (Scheduling)

- [x] 3.1 Crear el componente `DisconnectAlarmScheduler` para gestionar el `AlarmManager`.
- [x] 3.2 Implementar la lógica de cálculo: `t_alerta = t_meta - 90 minutos`.
- [x] 3.3 Configurar Hilt para proveer la instancia de `DisconnectAlarmScheduler`.

## 4. Integración con el Dominio y Persistencia

- [x] 4.1 Integrar el llamado a `DisconnectAlarmScheduler` en el flujo de guardado de `WeeklyGoalRepository`.
- [x] 4.2 Implementar `BootReceiver` para reprogramar la notificación si el dispositivo se reinicia.

## 5. Validación y UI

- [x] 5.1 Verificar la navegación desde la notificación hacia `DisconnectReminderScreen`.
- [x] 5.2 Realizar pruebas de integración para asegurar que la notificación se dispara en el tiempo correcto.
