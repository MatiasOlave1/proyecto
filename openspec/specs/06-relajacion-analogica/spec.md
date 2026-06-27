# SPEC-06: Módulo de Relajación Analógica (Offline-First)

## Metadata
- **ID**: 06-relajacion-analogica
- **Versión**: 1.0.0
- **Estado**: DRAFT
- **Dependencias**: 01-auth-perfil (sesión activa), 05-alarmas-regulacion (ventana nocturna)

---

## Descripción
Módulo multimedia con recursos analógicos de relajación accesibles sin internet.
Provee guías visuales de respiración rítmica y reproducción de audio local de
frecuencias estables, ejecutando todo en local sin consumo de ancho de banda.

---

## Modelo de Datos

### EntidadSesionRelajacion
```
uuid: String          // UUID v4 generado en cliente
userId: String        // FK → PerfilUsuario.uuid
tipo: Enum            // RESPIRACION | AUDIO
subtipo: String       // "4-7-8" | "BOX" | "COHERENTE" | "RUIDO_BLANCO" | "RUIDO_MARRON"
duracionSegundos: Int
completada: Boolean
audioActivo: Boolean  // true si había audio simultáneo durante RESPIRACION
iniciadoEn: String    // ISO 8601 UTC
finalizadoEn: String? // ISO 8601 UTC — null si fue interrumpida
```
---
## Capacidades

### CAP-06-A: Guía Visual de Respiración Rítmica

**GIVEN** el usuario abre el módulo de relajación
  AND el dispositivo no tiene conexión a internet
**WHEN** selecciona el método "4-7-8"
**THEN** la app renderiza una animación interactiva local con 3 fases:
  - INHALAR: 4 segundos (expansión del círculo, color #4A90D9)
  - RETENER: 7 segundos (círculo estático, pulso suave)
  - EXHALAR: 8 segundos (contracción del círculo, color #7EC8A4)
  AND la animación corre completamente en el hilo de UI sin llamadas a red
  AND el ciclo se repite automáticamente hasta que el usuario pause o cierre
  AND se registra una EntidadSesionRelajacion con tipo=RESPIRACION, subtipo="4-7-8"

**GIVEN** el usuario está en una sesión de respiración activa
**WHEN** la app pasa a background (onPause)
**THEN** la animación se pausa y persiste el timestamp ISO 8601 UTC de la interrupción
  AND al volver a foreground (onResume) la animación retoma desde el inicio del ciclo actual

**GIVEN** el módulo de respiración está activo
**WHEN** el usuario completa ≥ 3 ciclos completos del método seleccionado
**THEN** completada=true se persiste en EntidadSesionRelajacion
  AND se emite evento interno "SESION_RELAJACION_COMPLETADA" para que CAP-04
      (gamificación) evalúe triggers de logros

---

### CAP-06-B: Reproductor de Audio Local (Frecuencias Estables)

**GIVEN** los assets de audio están empaquetados en /assets/audio/ del APK
  (ruido_blanco.mp3, ruido_marron.mp3 — máx. 5 MB cada uno, loop sin costura)
**WHEN** el usuario selecciona "Ruido Blanco" o "Ruido Marrón"
**THEN** el audio inicia reproducción en ≤ 300ms usando MediaPlayer local
  AND el loop es continuo y sin costura (seamless loop)
  AND el volumen inicial se setea al 70% del volumen del sistema
  AND la reproducción continúa con pantalla apagada vía Foreground Service
      (igual que CAP-05: notificación persistente visible, prioridad PRIORITY_LOW)

**GIVEN** el audio está reproduciéndose en Foreground Service
**WHEN** otra app solicita el foco de audio (AudioFocus)
**THEN** la app aplica ducking (reducción al 30%) durante la interrupción
  AND recupera el volumen original al recuperar el foco de audio
  AND si la interrupción supera 60 segundos, la reproducción se pausa
      y notifica al usuario con "Audio pausado — toca para continuar"

**GIVEN** el usuario activa simultáneamente audio Y una guía de respiración
**WHEN** ambos módulos están activos
**THEN** el audio corre en Foreground Service y la animación en el hilo de UI
  AND no existe conflicto de recursos; la sesión registra tipo=RESPIRACION
      con campo audioActivo=true en los metadatos

**GIVEN** el dispositivo entra en Doze Mode (igual que CAP-05)
**WHEN** el Foreground Service de audio está activo
**THEN** la reproducción NO se interrumpe (Foreground Services son inmunes a Doze)
  AND el timer de duración sigue acumulando tiempo correctamente

---

### CAP-06-C: Integración con Ventana Nocturna (CAP-05)

**GIVEN** CAP-05 ha activado el recordatorio de "ventana de desconexión nocturna"
**WHEN** el usuario abre el módulo de relajación dentro de esa ventana
**THEN** la app sugiere el método 4-7-8 como primera opción destacada
  AND muestra un banner contextual: "Modo noche activo — pantalla en mínimo brillo"
  AND reduce el brillo de la pantalla al 20% automáticamente (restaura al salir)

---

## Restricciones Técnicas

| Restricción | Valor |
|---|---|
| Latencia inicio de audio | ≤ 300ms |
| Tamaño máximo por asset de audio | 5 MB |
| Acceso a red requerido | NINGUNO |
| Servicio en background | Foreground Service (obligatorio para audio) |
| Persistencia local | Room DB — EntidadSesionRelajacion |
| Formato timestamps | ISO 8601 UTC estricto |
| UUID generación | Cliente (no servidor) |

---

## Criterios de Aceptación (QA Checklist)

- [ ] El módulo carga y es funcional con WiFi y datos móviles desactivados
- [ ] La animación 4-7-8 completa un ciclo exacto de 19 segundos (4+7+8)
- [ ] El audio inicia en ≤ 300ms desde la acción del usuario
- [ ] El loop de audio no tiene corte audible entre repeticiones
- [ ] El Foreground Service persiste con pantalla apagada por ≥ 30 minutos
- [ ] Al interrumpir con llamada telefónica, el audio aplica ducking correctamente
- [ ] EntidadSesionRelajacion se persiste con UUID de cliente y timestamp UTC
- [ ] El evento "SESION_RELAJACION_COMPLETADA" dispara evaluación en CAP-04

---

## Archivos a crear (propuesta inicial de tareas)

```
openspec/specs/06-relajacion-analogica/
└── spec.md

openspec/changes/CHANGE-006/
├── proposal.md
└── tasks.md

app/src/main/
├── java/.../relajacion/
│   ├── RelajacionViewModel.kt
│   ├── BreathingAnimationView.kt    ← Custom View animación
│   ├── AudioPlayerService.kt        ← Foreground Service
│   └── SesionRelajacionDao.kt
├── assets/audio/
│   ├── ruido_blanco.mp3
│   └── ruido_marron.mp3
└── res/layout/
    └── fragment_relajacion.xml
```