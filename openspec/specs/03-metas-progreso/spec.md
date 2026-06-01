# Capacidad: Metas Semanales y Dashboard Analítico

## Purpose
Esta capacidad permite al usuario configurar objetivos de sueño semanales, visualizar resúmenes mensuales en un dashboard interactivo y hacer seguimiento de sus rachas de cumplimiento.

### Contexto de Persistencia
El sistema gestionará las metas del usuario en la tabla `META_SEMANAL`:

| Campo | Tipo | Restricción | Descripción |
| :--- | :--- | :--- | :--- |
| `id` | TEXT | PRIMARY KEY (UUID) | Identificador único de la meta |
| `usuario_id` | TEXT | FOREIGN KEY -> `USUARIO.id` | Referencia al usuario propietario de la meta |
| `fecha_inicio` | TEXT | NOT NULL | Fecha de inicio de la semana (Lunes) en formato ISO 8601 (YYYY-MM-DD) |
| `horas_minimas_noche` | TEXT | NOT NULL | Cantidad de horas mínimas a dormir por noche (almacenado como texto) |
| `dias_consecutivos_requeridos` | INTEGER | NOT NULL | Cantidad de días consecutivos requeridos para cumplir la meta |
| `hora_limite_acostarse` | TEXT | NOT NULL | Hora máxima recomendada para acostarse (formato HH:MM) |
| `activa` | INTEGER | NOT NULL | Indica si la meta está activa (booleano local: 1 = Activa, 0 = Inactiva) |
| `creado_at` | TEXT | NOT NULL | Timestamp de creación en formato ISO 8601 UTC |

## Requirements

### Requirement: Dashboard mensual y seguimiento de metas
El sistema SHALL calcular métricas mensuales de sueño y realizar seguimiento diario de las rachas de cumplimiento del usuario.

#### Scenario: Inicialización de Dashboard Mensual
- **GIVEN** un usuario con registros históricos de sueño en el mes calendario actual.
- **WHEN** accede a la pantalla de Dashboard.
- **THEN** el sistema DEBERÁ calcular el promedio aritmético mensualizado de horas reales dormidas:
  $$\bar{X} = \frac{\sum_{i=1}^{n} t_i}{n}$$
- **AND** obtener el valor más frecuente (Moda) de la columna `calidad_sueno`.

#### Scenario: Seguimiento y Ruptura de Rachas diarias
- **GIVEN** el contador visual de racha actual en la interfaz de usuario.
- **WHEN** se procesa el cambio de día y el usuario guardó un registro que cumple con la `hora_limite_acostarse` y `horas_minimas_noche` de su meta activa.
- **THEN** el contador incrementará en $+1$.
- **BUT WHEN** pasa un día sin registrar sueño, o bien el registro no cumple con alguno de los dos parámetros de la meta activa.
- **THEN** el contador se reiniciará estrictamente a $0$.
