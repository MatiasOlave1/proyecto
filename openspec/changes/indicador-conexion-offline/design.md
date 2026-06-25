## Context

La aplicación DormiBienU es offline-first. Para evitar la pérdida de información acumulada cuando el dispositivo está sin internet y dar visibilidad al usuario, se diseñará un panel superior informativo y un motor de sincronización automática y segura al recuperar la conectividad.

## Goals / Non-Goals

**Goals:**
- Crear la clase utilitaria `NetworkMonitor` que expone la conectividad de red mediante un `Flow<Boolean>` reactivo.
- Agregar query directa `getUserByIdDirect` en `UserDao` e implementar la sincronización de sesión diferida `ensureUserSessionSynced` en `SyncRepository`.
- Disparar la sincronización de fondo `syncRepository.syncAll` desde `DashboardViewModel` ante cualquier transición al estado Online.
- Diseñar la insignia de conectividad en `StreaksScreen.kt` arriba de la racha.

**Non-Goals:**
- Implementar una cola de peticiones con reintentos exponenciales complejos (se delega en el ciclo de vida de la aplicación y la detección de cambios de red).

## Decisions

### 1. Sincronización Diferida de Cuentas Offline
- **Decisión**: Si el usuario inició sesión/se registró de forma offline, al volver a estar online se enviarán los datos locales del perfil en `UserEntity` para intentar registrarlo en la base de datos remota. Si falla porque ya está registrado (ej. el correo ya existe en el servidor), se intentará un inicio de sesión silencioso con las credenciales locales cifradas para obtener el token Sanctum.
- **Razón**: Sin token Sanctum, todas las llamadas de sincronización fallarán con `401 Unauthorized`. Este paso de sincronización de credenciales asegura que el flujo de sincronización posterior sea exitoso.

### 2. Disparador Automático de Sincronización en DashboardViewModel
- **Decisión**: `DashboardViewModel` recolectará el flujo de `NetworkMonitor.isOnline`. Si transiciona a `true`, llamará a `syncRepository.syncAll` en una corrutina.
- **Razón**: El Dashboard es la pantalla de inicio del flujo de pestañas principal, por lo que es el lugar ideal para alojar la sincronización automática de fondo sin interrumpir la experiencia del usuario.

## Risks / Trade-offs

- [Riesgo] -> Ejecutar sincronizaciones múltiples si el estado de conexión oscila rápidamente.
- [Mitigación] -> El interceptor de Retrofit y el estado de `SyncStatus.PENDING` protegen la integridad de los datos, ya que sólo los registros marcados como pendientes se empaquetarán y enviarán, cambiando a `SYNCED` una vez procesados.
