# SPEC-07: Diario de Preocupaciones (Offline-First)

## Metadata
- **ID**: 07-diario-preocupaciones
- **Versión**: 1.0.0
- **Estado**: DRAFT
- **Dependencias**: 01-auth-perfil (sesión activa), 05-alarmas-regulacion (ventana nocturna)

---

## Descripción
Herramienta terapéutica de descarga cognitiva previa al descanso. Permite al
usuario registrar texto libre asociado a la fecha actual, sin análisis semántico.
Actúa como contenedor seguro de desahogo con almacenamiento local y opción de
auto-eliminación matutina.

---

## Modelo de Datos

### EntidadEntradaDiario
```
uuid: String           // UUID v4 generado en cliente
userId: String         // FK → PerfilUsuario.uuid
contenido: String      // Texto libre ingresado por el usuario
fechaEntrada: String   // ISO 8601 UTC — fecha del registro
autoEliminar: Boolean  // true = se elimina automáticamente en la mañana
eliminada: Boolean     // false por defecto
creadoEn: String       // ISO 8601 UTC
eliminadoEn: String?   // ISO 8601 UTC — null si no fue eliminada
```

---

## Capacidades

### CAP-07-A: Registro de Entrada de Texto

**GIVEN** el usuario abre el módulo Diario de Preocupaciones
  AND el dispositivo no tiene conexión a internet
**WHEN** el usuario ingresa texto libre y confirma el guardado
**THEN** la app persiste una EntidadEntradaDiario en Room DB
  AND el uuid es generado en cliente con UUID v4
  AND fechaEntrada y creadoEn se registran en ISO 8601 UTC
  AND se muestra confirmación visual: "Tu entrada fue guardada de forma segura"
  AND el campo de texto se limpia automáticamente tras el guardado

**GIVEN** el usuario está escribiendo en el diario
**WHEN** la app pasa a background (onPause)
**THEN** el texto en progreso se persiste en estado temporal (no como EntidadEntradaDiario)
  AND al volver a foreground (onResume) el texto en progreso se restaura

**GIVEN** el usuario intenta guardar
**WHEN** el campo de texto está vacío
**THEN** la app NO persiste ninguna entidad
  AND muestra mensaje: "Escribe algo antes de guardar"

---

### CAP-07-B: Consulta de Entradas Anteriores

**GIVEN** el usuario abre la sección de historial del diario
**WHEN** solicita ver entradas anteriores
**THEN** la app muestra lista de EntidadEntradaDiario ordenadas por fechaEntrada DESC
  AND solo muestra entradas donde eliminada=false
  AND cada entrada muestra: fecha formateada + primeras 50 caracteres del contenido

**GIVEN** el usuario selecciona una entrada del historial
**WHEN** toca para expandir
**THEN** se muestra el contenido completo de la entrada
  AND se ofrece opción de eliminar manualmente esa entrada

**GIVEN** el usuario elige eliminar una entrada manualmente
**WHEN** confirma la acción
**THEN** eliminada=true y eliminadoEn=timestamp ISO 8601 UTC se persisten
  AND la entrada desaparece del historial inmediatamente
  AND el contenido NO se borra físicamente de la BD (soft delete)

---

### CAP-07-C: Auto-eliminación Matutina

**GIVEN** el usuario activó autoEliminar=true al crear una entrada
**WHEN** el dispositivo detecta que son las 06:00 hora local del día siguiente
**THEN** todas las entradas con autoEliminar=true y eliminada=false
      se marcan como eliminada=true y eliminadoEn=timestamp UTC
  AND el usuario recibe notificación: "Tu diario de anoche fue borrado automáticamente"
  AND los datos NO se borran físicamente de la BD (soft delete)

**GIVEN** el dispositivo está en Doze Mode a las 06:00
**WHEN** se activa la ventana de mantenimiento de Doze
**THEN** el WorkManager ejecuta la tarea de auto-eliminación
  AND se garantiza ejecución con retraso máximo de 15 minutos

---

### CAP-07-D: Integración con Ventana Nocturna (CAP-05)

**GIVEN** CAP-05 ha activado el recordatorio de "ventana de desconexión nocturna"
**WHEN** el usuario abre el módulo de diario dentro de esa ventana
**THEN** la app sugiere activar autoEliminar=true como opción destacada
  AND muestra banner: "Modo noche activo — tu entrada se borrará al despertar"
  AND reduce el brillo de la pantalla al 20% automáticamente (restaura al salir)

---

## Restricciones Técnicas

| Restricción | Valor |
|---|---|
| Acceso a red requerido | NINGUNO |
| Análisis semántico del texto | NINGUNO |
| Persistencia local | Room DB — EntidadEntradaDiario |
| Eliminación | Soft delete (eliminada=true, nunca DELETE físico) |
| Auto-eliminación background | WorkManager |
| Formato timestamps | ISO 8601 UTC estricto |
| UUID generación | Cliente (no servidor) |
| Texto en progreso | Estado temporal en ViewModel (no Room) |

---

## Criterios de Aceptación (QA Checklist)

- [ ] El módulo carga y es funcional sin conexión a internet
- [ ] El texto guardado persiste con UUID de cliente y timestamp UTC
- [ ] El campo se limpia tras guardar correctamente
- [ ] El texto en progreso se restaura tras volver de background
- [ ] El historial muestra entradas ordenadas por fecha DESC
- [ ] El soft delete oculta la entrada del historial sin borrar de BD
- [ ] La auto-eliminación se ejecuta a las 06:00 vía WorkManager
- [ ] La auto-eliminación funciona aunque el dispositivo esté en Doze Mode
- [ ] La integración con CAP-05 sugiere autoEliminar en ventana nocturna

---

## Archivos a crear (propuesta inicial de tareas)

```
openspec/specs/07-diario-preocupaciones/
└── spec.md
openspec/changes/CHANGE-007/
├── proposal.md
└── tasks.md
app/src/main/
├── java/.../diario/
│   ├── config/
│   │   └── DiarioConfig.kt
│   ├── data/
│   │   ├── local/
│   │   │   ├── dao/
│   │   │   │   └── EntradaDiarioDao.kt
│   │   │   └── entity/
│   │   │       └── EntradaDiarioEntity.kt
│   │   └── repository/
│   │       └── DiarioRepository.kt
│   ├── domain/
│   │   ├── model/
│   │   │   └── EntradaDiario.kt
│   │   └── usecase/
│   │       └── DiarioUseCases.kt
│   ├── service/
│   │   └── AutoEliminarWorker.kt
│   ├── ui/
│   │   ├── components/
│   │   │   ├── EntradaDiarioItem.kt
│   │   │   └── DiarioInputField.kt
│   │   ├── screens/
│   │   │   └── DiarioScreen.kt
│   │   └── viewmodel/
│   │       └── DiarioViewModel.kt
│   └── util/
│       └── DiarioUtils.kt
```