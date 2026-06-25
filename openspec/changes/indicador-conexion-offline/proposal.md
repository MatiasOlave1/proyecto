## Why

La aplicación DormiBienU es de tipo "Offline-First". Sin embargo, el usuario carece de visibilidad respecto al estado actual de su conectividad y no existe una sincronización automatizada para evitar la pérdida de información que haya sido registrada offline (registros de sueño, metas semanales, rachas y logros) una vez que el dispositivo recupera la conexión a internet.

## What Changes

- **Clase de Monitoreo**: Implementar una clase de utilidad (`NetworkMonitor`) en Kotlin que use `ConnectivityManager` de Android para observar el estado de red como un flujo continuo (`Flow`).
- **Lógica de Sincronización Automática**:
  - Modificar `UserDao` para obtener un usuario por su ID sin usar flows.
  - Implementar en `SyncRepository` la función `ensureUserSessionSynced` para verificar si un usuario registrado localmente de forma offline debe ser registrado o logueado en el servidor remoto para obtener su token Sanctum.
  - Actualizar `syncAll` en `SyncRepository` para asegurar la autenticación del usuario antes de sincronizar.
  - En `DashboardViewModel`, suscribirse al flujo de conexión y disparar `syncAll` automáticamente en segundo plano en la transición de Offline a Online.
- **Indicador de Conectividad en la UI**: Agregar una barra/tarjeta informativa premium arriba del sistema de visualización de rachas en la pantalla `StreaksScreen.kt` que muestre si la aplicación se encuentra "En Línea" (Online) o en "Modo Offline" (Offline) con su respectiva iconografía de red y colores de estado HSL.

## Capabilities

### New Capabilities

- Ninguna

### Modified Capabilities

- `03-metas-progreso`: Se añade a la pantalla de seguimiento de rachas la visualización en tiempo real del estado de conexión (Online/Offline) del dispositivo usando el sistema nativo y se garantiza la sincronización automática de datos acumulados localmente.

## Impact

- `NetworkMonitor.kt` (nueva clase de utilidad).
- `UserDao.kt` (nueva query directa `getUserByIdDirect`).
- `SyncRepository.kt` (registro/login automático de usuario offline y llamada inicial en `syncAll`).
- `DashboardViewModel.kt` (disparador automático de sincronización al detectar cambio a Online).
- `StreaksViewModel.kt` (para consumir el flujo de estado de red).
- `StreaksScreen.kt` (renderizar el indicador visual de conexión arriba de la racha).
