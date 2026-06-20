# Capacidad: Alarmas Inteligentes, Desconexión y Diario

## Purpose
Esta capacidad proporciona alarmas inteligentes con integración musical, soporte para ejecutarse en suspensión profunda (Doze Mode) mediante servicios nativos en primer plano, y recordatorios adaptativos para preparar la desconexión pre-sueño.

### Contexto de Persistencia
El sistema gestionará las alarmas del usuario en la tabla `ALARMA`:

| Campo | Tipo | Restricción | Descripción |
| :--- | :--- | :--- | :--- |
| `id` | TEXT | PRIMARY KEY (UUID) | Identificador único de la alarma |
| `usuario_id` | TEXT | FOREIGN KEY -> `USUARIO.id` | Referencia al usuario propietario de la alarma |
| `hora_despertar` | TEXT | NOT NULL | Hora programada para sonar (formato HH:MM) |
| `frecuencia` | TEXT | NOT NULL | Días de repetición separados por comas (ej: `"1,2,3,4,5"` donde 1=Lunes, 7=Domingo) |
| `activa` | INTEGER | NOT NULL | Indica si la alarma está activa (1 = Activa, 0 = Inactiva) |
| `spotify_playlist` | TEXT | NULLABLE | URI o enlace de la playlist de Spotify seleccionada |
| `creado_at` | TEXT | NOT NULL | Timestamp de creación en formato ISO 8601 UTC |
## Requirements
### Requirement: Alarma de alta confiabilidad y recordatorios preventivos
El sistema SHALL ejecutar las alarmas configuradas superando el estado de suspensión profunda (Doze Mode) del sistema operativo y emitir avisos de desconexión nocturna basados en la fuente de tiempo seleccionada (Manual o Racha Semanal).

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
- **GIVEN** la hora de acostarse determinada por la fuente seleccionada (Manual o Racha Semanal).
- **WHEN** el tiempo actual del sistema se sitúa exactamente 90 minutos antes de dicha hora:
  $$t_{\text{alerta}} = t_{\text{límite\_acostarse}} - 90\text{ minutos}$$
- **THEN** el sistema DEBERÁ disparar de manera automatizada una notificación push de baja luminancia sugiriendo al usuario iniciar el proceso de desconexión y activar el filtro de luz azul del dispositivo.
- **AND** al tocar la notificación, el sistema DEBERÁ redirigir al usuario a la pantalla de Recordatorio de Desconexión (`DisconnectReminderScreen`).

### Requirement: Configuración manual de la hora de acostarse
El sistema SHALL permitir al usuario configurar manualmente una hora específica para acostarse desde la pantalla de recordatorio de desconexión (`DisconnectReminderScreen`).

#### Scenario: Usuario cambia la hora de acostarse manualmente
- **WHEN** el usuario interactúa con el selector de hora en la `DisconnectReminderScreen`.
- **AND** selecciona una nueva hora (ej: 22:30).
- **THEN** el sistema DEBERÁ persistir esta hora como la "Hora de Acostarse Manual".
- **AND** reprogramar la notificación de desconexión para dispararse 90 minutos antes de la nueva hora configurada.

### Requirement: Selección de la fuente para la hora de desconexión
El sistema SHALL permitir al usuario alternar entre usar la "Hora de Acostarse Manual" o la hora derivada de la "Racha Semanal" (u objetivo semanal activo mientras la racha no esté disponible).

#### Scenario: Cambio a modo manual
- **GIVEN** que el sistema está configurado para usar la racha semanal.
- **WHEN** el usuario selecciona la opción "Manual".
- **THEN** el sistema DEBERÁ utilizar la hora configurada manualmente para calcular el momento de la notificación.

#### Scenario: Cambio a modo racha semanal
- **GIVEN** que el sistema está configurado para usar el modo manual.
- **WHEN** el usuario selecciona la opción "Racha Semanal".
- **THEN** el sistema DEBERÁ utilizar la `hora_limite_acostarse` de la meta semanal activa (como fallback mientras la racha no esté implementada) para calcular el momento de la notificación.

