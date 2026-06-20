## MODIFIED Requirements

### Requirement: Alarma de alta confiabilidad y recordatorios preventivos
El sistema SHALL ejecutar las alarmas configuradas superando el estado de suspensión profunda (Doze Mode) del sistema operativo y emitir avisos de desconexión nocturna.

#### Scenario: Activación de la Alarma con Integración de Spotify
- **GIVEN** una alarma activa programada a una hora específica (ej: 07:00 AM).
- **AND** el dispositivo entra en reposo profundo (Doze Mode).
- **WHEN** el reloj del sistema alcanza la hora fijada.
- **THEN** el sistema DEBERÁ despertar el hilo de ejecución mediante un Foreground Service nativo en un tiempo de respuesta:
  $$t_{\text{despertar}} < 500\text{ ms}$$
- **AND** verificar si existe una URI o link válido en `spotify_playlist` para intentar jugar la música vía el SDK de Spotify.
- **BUT WHEN** no hay conectividad a internet o falla la inicialización/reproducción del SDK de Spotify.
- **THEN** el sistema DEBERÁ usar por seguridad el tono de alarma de respaldo almacenado de manera local en el almacenamiento interno del dispositivo.

#### Scenario: Recordatorio automático de "Ventana de Desconexión"
- **GIVEN** la `hora_limite_acostarse` configurada en la meta semanal activa (ej: 23:00).
- **WHEN** el tiempo actual del sistema se sitúa exactamente 90 minutos antes de dicha hora:
  $$t_{\text{alerta}} = t_{\text{límite\_acostarse}} - 90\text{ minutos}$$
- **THEN** el sistema DEBERÁ disparar de manera automatizada una notificación push de baja luminancia sugiriendo al usuario iniciar el proceso de desconexión y activar el filtro de luz azul del dispositivo.
- **AND** al tocar la notificación, el sistema DEBERÁ redirigir al usuario a la pantalla de Recordatorio de Desconexión (`DisconnectReminderScreen`).
