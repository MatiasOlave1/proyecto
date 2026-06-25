## 1. Utilidades y Red

- [ ] 1.1 Crear la clase [NetworkMonitor.kt](file:///home/seba/AndroidStudioProjects/proyecto/app/src/main/java/com/camposocampoolavevargas/proyecto/util/NetworkMonitor.kt) en el paquete `com.camposocampoolavevargas.proyecto.util` para observar el estado de red de manera reactiva con corrutinas y callbacks del sistema.

## 2. Persistencia y Sincronización de Sesión (Data & Repository)

- [ ] 2.1 Agregar la consulta suspendida `getUserByIdDirect` en [UserDao.kt](file:///home/seba/AndroidStudioProjects/proyecto/app/src/main/java/com/camposocampoolavevargas/proyecto/data/local/dao/UserDao.kt) para recuperar el perfil del usuario local sin flows.
- [ ] 2.2 Implementar la función `ensureUserSessionSynced` en [SyncRepository.kt](file:///home/seba/AndroidStudioProjects/proyecto/app/src/main/java/com/camposocampoolavevargas/proyecto/data/repository/SyncRepository.kt) para registrar o iniciar sesión silenciosamente en el servidor remoto al usuario que fue creado localmente offline, obteniendo su token de sesión Sanctum.
- [ ] 2.3 Modificar `syncAll` en [SyncRepository.kt](file:///home/seba/AndroidStudioProjects/proyecto/app/src/main/java/com/camposocampoolavevargas/proyecto/data/repository/SyncRepository.kt) para llamar a `ensureUserSessionSynced` al inicio y abortar la sincronización si no se puede establecer una sesión válida en el servidor.

## 3. Lógica de negocio (ViewModel & Coordinator)

- [ ] 3.1 Inyectar `NetworkMonitor` y exponer un `StateFlow<Boolean>` llamado `isOnline` en [StreaksViewModel.kt](file:///home/seba/AndroidStudioProjects/proyecto/app/src/main/java/com/camposocampoolavevargas/proyecto/ui/screens/StreaksViewModel.kt).
- [ ] 3.2 Crear la clase `SyncCoordinator.kt` e inyectarla en `MainActivity.kt` para suscribirse de forma debounceada a `NetworkMonitor.isOnline` y disparar `syncRepository.syncAll(userId)` al volver a estar online en segundo plano.

## 4. Interfaz de Usuario (UI)

- [ ] 4.1 Actualizar [StreaksScreen.kt](file:///home/seba/AndroidStudioProjects/proyecto/app/src/main/java/com/camposocampoolavevargas/proyecto/ui/screens/StreaksScreen.kt) para observar el flujo `isOnline` expuesto por el ViewModel.
- [ ] 4.2 Diseñar y agregar la tarjeta del indicador de conexión ("En Línea" vs "Modo Offline") por encima del card de la racha de fuego en [StreaksScreen.kt](file:///home/seba/AndroidStudioProjects/proyecto/app/src/main/java/com/camposocampoolavevargas/proyecto/ui/screens/StreaksScreen.kt).
- [ ] 4.3 Actualizar la vista previa de `StreaksScreenPreview` en [StreaksScreen.kt](file:///home/seba/AndroidStudioProjects/proyecto/app/src/main/java/com/camposocampoolavevargas/proyecto/ui/screens/StreaksScreen.kt) con valores simulados de prueba.

## 5. Pruebas y Compilación

- [ ] 5.1 Compilar la aplicación ejecutando `./gradlew compileDebugKotlin` para validar que compile correctamente.
