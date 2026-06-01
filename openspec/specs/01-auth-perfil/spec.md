# Capacidad: Autenticación y Perfil de Usuario

## Purpose
Esta capacidad gestiona el registro, inicio de sesión y almacenamiento local de la información del perfil del estudiante universitario, con soporte prioritario para escenarios offline.

### Contexto de Persistencia
El sistema gestionará los datos localmente y sincronizará en la tabla `USUARIO`:

| Campo | Tipo | Restricción | Descripción |
| :--- | :--- | :--- | :--- |
| `id` | TEXT | PRIMARY KEY (UUID generado en cliente) | Identificador único del usuario |
| `nombre_completo` | TEXT | NOT NULL | Nombre y apellido del estudiante |
| `fecha_nacimiento` | TEXT | NOT NULL | Fecha en formato ISO 8601 (YYYY-MM-DD) |
| `region` | TEXT | NOT NULL | Región de residencia actual |
| `comuna` | TEXT | NOT NULL | Comuna de residencia actual |
| `universidad` | TEXT | NOT NULL | Universidad de estudio del estudiante |
| `carrera` | TEXT | NOT NULL | Carrera de pregrado cursada |
| `email` | TEXT | UNIQUE, NOT NULL | Correo institucional o personal del estudiante |
| `password_hash` | TEXT | NOT NULL | Contraseña cifrada localmente (AES-256) |
| `creado_at` | TEXT | NOT NULL | Timestamp de creación en formato ISO 8601 UTC |
| `actualizado_at` | TEXT | NOT NULL | Timestamp de última edición en formato ISO 8601 UTC |

## Requirements

### Requirement: Registro de usuario y manejo de datos offline
El sistema SHALL permitir el registro de usuarios y la persistencia de sus perfiles incluso en condiciones sin conexión a internet.

#### Scenario: Registro de usuario exitoso en modo Offline
- **GIVEN** un estudiante universitario sin cuenta previa.
- **AND** el dispositivo se encuentra sin conectividad a internet (`NetworkStatus == Offline`).
- **WHEN** el usuario ingresa todos los campos obligatorios válidos (email, password, nombre_completo, region, comuna, universidad, carrera).
- **THEN** el sistema DEBERÁ generar un UUID local para el campo `id`.
- **AND** DEBERÁ encriptar la contraseña localmente usando cifrado AES-256 antes de guardarla en `password_hash`.
- **AND** guardar el registro en la base de datos local con timestamps en formato ISO 8601 UTC.
- **AND** redirigir al usuario al flujo principal de la aplicación.

#### Scenario: Intento de registro con campos obligatorios vacíos
- **GIVEN** el formulario de registro de usuario.
- **WHEN** el usuario intenta guardar el registro y algún campo obligatorio es nulo o vacío.
- **THEN** el sistema NO DEBE persistir los datos.
- **AND** arrojará una excepción del tipo `ValidationError`.
- **AND** mostrará un mensaje descriptivo en la interfaz indicando los campos faltantes.

#### Scenario: Intento de registro con formato de email inválido
- **GIVEN** el formulario de registro de usuario.
- **WHEN** el correo electrónico ingresado no cumple con la expresión regular estándar de correo:
  $$\text{regex} = \text{^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}\$}$$
- **THEN** el sistema NO DEBE persistir los datos.
- **AND** se gatillará la excepción `InvalidEmailException`.
- **AND** mostrará un mensaje de error indicando formato de correo electrónico inválido.
