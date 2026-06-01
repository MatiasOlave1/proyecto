# Capacidad: Registro y Análisis de Sueño

## Purpose
Esta capacidad se encarga del registro diario del sueño por parte del usuario, calculando automáticamente las horas de descanso, evaluando métricas como el Jet Lag Social y detectando anomalías.

### Contexto de Persistencia
El sistema persistirá los registros de sueño en la tabla `REGISTRO_SUENO`:

| Campo | Tipo | Restricción | Descripción |
| :--- | :--- | :--- | :--- |
| `id` | TEXT | PRIMARY KEY (UUID generado en cliente) | Identificador único del registro de sueño |
| `usuario_id` | TEXT | FOREIGN KEY -> `USUARIO.id` | Referencia al usuario propietario del registro |
| `fecha_registro` | TEXT | NOT NULL | Fecha del registro de sueño en formato ISO 8601 (YYYY-MM-DD) |
| `hora_dormir` | TEXT | NOT NULL | Timestamp ISO 8601 UTC de la hora en que el usuario se durmió |
| `hora_despertar` | TEXT | NOT NULL | Timestamp ISO 8601 UTC de la hora en que el usuario despertó |
| `horas_totales_calculadas` | REAL | NOT NULL | Cantidad de horas totales calculadas en formato decimal |
| `calidad_sueno` | TEXT | NOT NULL | Calidad percibida (Enum: `Muy malo`, `Malo`, `Regular`, `Buena`, `Excelente`) |
| `creado_at` | TEXT | NOT NULL | Timestamp de creación en formato ISO 8601 UTC |
| `actualizado_at` | TEXT | NOT NULL | Timestamp de última edición en formato ISO 8601 UTC |

## Requirements

### Requirement: Registro de sueño diario y alertas
El sistema SHALL permitir registrar las horas de sueño, calcular la duración y alertar sobre jet lag social o duraciones inválidas.

#### Scenario: Cálculo automático de horas de sueño
- **GIVEN** un usuario autenticado.
- **WHEN** ingresa un registro con `hora_dormir` y `hora_despertar`.
- **THEN** el sistema DEBERÁ calcular automáticamente la diferencia de tiempo en horas:
  $$\Delta t = \text{Hora de despertar} - \text{Hora de dormir}$$
- **AND** almacenar el valor decimal resultante en `horas_totales_calculadas` (ej: 8.0).

#### Scenario: Alertas de Jet Lag Social
- **GIVEN** los registros de sueño de la última semana del usuario (últimos 7 días).
- **WHEN** el sistema calcula el promedio de la hora de despertar de Lunes a Viernes ($\bar{X}_{\text{Semana}}$) y lo compara contra el promedio de Sábado y Domingo ($\bar{X}_{\text{FinSemana}}$).
- **AND** la diferencia absoluta entre ambos promedios es mayor a 2 horas:
  $$\left| \bar{X}_{\text{Semana}} - \bar{X}_{\text{FinSemana}} \right| > 2$$
- **THEN** el sistema DEBERÁ activar un estado de alerta circadiana.
- **AND** desplegar la tarjeta informativa de alerta de "Jet Lag Social" en la interfaz.

#### Scenario: Intento de registro con inconsistencia cronológica
- **GIVEN** un usuario autenticado.
- **WHEN** el usuario ingresa una `hora_despertar` que es anterior o igual a la `hora_dormir`.
- **THEN** el sistema rechazará el registro.
- **AND** arrojará la excepción `InvalidSleepDurationException`.
- **AND** mostrará un mensaje de error indicando que la hora de despertar debe ser posterior a la hora de acostarse.

#### Scenario: Registro duplicado para la misma fecha
- **GIVEN** un usuario autenticado que ya tiene un registro de sueño para una `fecha_registro` específica (YYYY-MM-DD).
- **WHEN** el usuario intenta insertar un nuevo registro para la misma `fecha_registro`.
- **THEN** la operación actuará como un *Upsert* (sobrescritura), aplicando la política *Last-Write-Wins* (RNF01) sobre los datos existentes en la base de datos local.
