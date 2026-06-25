## ADDED Requirements

### Requirement: Indicador visual de estado de conexión en la pantalla de rachas
El sistema SHALL mostrar en tiempo real el estado de conectividad a internet del dispositivo (En Línea u Offline) en la pantalla de seguimiento de rachas (`StreaksScreen.kt`).

#### Scenario: Dispositivo con conectividad a internet activa
- **GIVEN** que el dispositivo del usuario tiene conexión de red y salida a internet activa.
- **WHEN** el usuario visualiza la pantalla de rachas.
- **THEN** el sistema DEBERÁ renderizar una etiqueta o tarjeta con un indicador de estado en verde y el texto "En Línea • Sincronizado".

#### Scenario: Dispositivo sin conectividad a internet (Offline)
- **GIVEN** que el dispositivo no tiene acceso a internet.
- **WHEN** el usuario visualiza la pantalla de rachas o se corta la conexión en tiempo de ejecución.
- **THEN** el sistema DEBERÁ actualizar la interfaz de manera inmediata mostrando una etiqueta de alerta en naranja/rojo y el texto "Modo Offline • Guardando localmente".

### Requirement: Sincronización automática al detectar transición de Offline a Online
El sistema SHALL empaquetar y sincronizar de manera automatizada en segundo plano todos los datos locales no sincronizados (registros de sueño pendientes, metas semanales, rachas y logros) al transicionar del estado Offline a Online, garantizando la consistencia y evitando pérdida de información.

#### Scenario: Sincronización automática de datos tras volver a estar En Línea
- **GIVEN** registros de sueño locales marcados con `syncStatus == PENDING`.
- **AND** el dispositivo detecta una transición de estado de red de Offline a Online.
- **WHEN** el sistema dispara la rutina de sincronización de fondo.
- **THEN** el sistema DEBERÁ primero comprobar la existencia de un token de sesión activo.
- **AND** si no existe token pero el usuario está logueado localmente, deberá registrar o loguear al usuario en el servidor para obtener su token.
- **AND** enviar el paquete de registros pendientes (`syncSleepRecords`), las metas semanales, las rachas y los logros al servidor.
- **AND** actualizar el estado de los registros locales correspondientes a `SYNCED` una vez confirmada la recepción exitosa del servidor.
