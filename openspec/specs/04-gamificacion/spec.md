# Capacidad: Sistema de Gamificación (Logros y Recompensas)

## Purpose
Esta capacidad introduce mecánicas de juego en la aplicación mediante un sistema de logros desbloqueables basados en el cumplimiento continuo de las metas de sueño del usuario.

### Contexto de Persistencia
El sistema gestionará la gamificación a través de las tablas `LOGRO` y `LOGROS_USUARIO`:

#### Tabla: LOGRO
Esta tabla almacena los logros definidos de manera estática en el sistema:

| Campo | Tipo | Restricción | Descripción |
| :--- | :--- | :--- | :--- |
| `id` | TEXT | PRIMARY KEY | Identificador único estático (ej: `'DISCIPLINA_5D'`) |
| `nombre` | TEXT | NOT NULL | Nombre legible del logro |
| `descripcion` | TEXT | NOT NULL | Descripción de la condición para desbloquearlo |

#### Tabla: LOGROS_USUARIO
Esta tabla almacena las instancias de logros desbloqueados por los usuarios:

| Campo | Tipo | Restricción | Descripción |
| :--- | :--- | :--- | :--- |
| `id` | TEXT | PRIMARY KEY (UUID) | Identificador único del desbloqueo |
| `usuario_id` | TEXT | FOREIGN KEY -> `USUARIO.id` | Referencia al usuario que desbloqueó el logro |
| `logro_id` | TEXT | FOREIGN KEY -> `LOGRO.id` | Referencia al logro desbloqueado |
| `fecha_desbloqueo` | TEXT | NOT NULL | Fecha del desbloqueo en formato ISO 8601 (YYYY-MM-DD) |

## Requirements

### Requirement: Otorgamiento de logros y notificaciones
El sistema SHALL evaluar el progreso del usuario y desbloquear logros en base a su consistencia, notificándole correspondientemente.

#### Scenario: Desbloqueo del logro 'Disciplina' por inserción
- **GIVEN** la inserción exitosa de un registro en `REGISTRO_SUENO`.
- **WHEN** el sistema ejecuta el trigger o función de evaluación y detecta que el usuario acumula 5 días consecutivos cumpliendo la meta de `horas_minimas_noche` (es decir, una racha activa $\ge$ 5).
- **AND** no existe previamente un registro en `LOGROS_USUARIO` que asocie a dicho `usuario_id` con el `logro_id` `'DISCIPLINA_5D'`.
- **THEN** el sistema DEBERÁ insertar un nuevo registro en `LOGROS_USUARIO` con su respectivo `id` (UUID local) y la fecha de hoy.
- **AND** disparar de forma inmediata una notificación push local interactiva en el dispositivo, mostrando la medalla virtual obtenida.
