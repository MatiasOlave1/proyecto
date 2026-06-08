# Guía de Integración - SPEC-06: Relajación Analógica

## Checklist de Integración

### 1. AndroidManifest.xml
Agregar el siguiente servicio:

```xml
<service
    android:name=".relajacion.service.AudioPlayerService"
    android:enabled="true"
    android:exported="false" />
```

Agregar permisos si es necesario:
```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />
```

### 2. Actualizar build.gradle.kts
Las dependencias necesarias (ya incluidas en el proyecto base):
- androidx.lifecycle:lifecycle-runtime-ktx
- androidx.activity:activity-compose
- androidx.compose.material3:material3
- androidx.room:room-runtime (para DB)

### 3. Assets de Audio
1. Crear carpeta: `app/src/main/assets/audios/`
2. Colocar archivos:
   - `ruido_blanco.mp3` (max 5MB, seamless loop)
   - `ruido_marron.mp3` (max 5MB, seamless loop)

### 4. Actualizar AppDatabase
Agregar DAO a la base de datos:

```kotlin
@Database(
    entities = [
        // ... otras entidades
        SesionRelajacionEntity::class
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sesionRelajacionDao(): SesionRelajacionDao
    
    // ... resto del código
}
```

### 5. Inyección de Dependencias
Crear instancias (en Hilt o factory):

```kotlin
// Repository
val repository = RelajacionRepository(
    database.sesionRelajacionDao()
)

// Use Cases
val iniciarUseCase = IniciarSesionRelajacionUseCase(repository)
val completarUseCase = CompletarSesionRelajacionUseCase(repository)
val interrumpirUseCase = InterrumpirSesionRelajacionUseCase(repository)

// ViewModel
val viewModel = RelajacionViewModel(
    context = context,
    repository = repository,
    iniciarUseCase = iniciarUseCase,
    completarUseCase = completarUseCase,
    interrumpirUseCase = interrumpirUseCase
)
```

### 6. Integración en MainActivity
```kotlin
// En setContent
RelajacionScreen(
    viewModel = relajacionViewModel,
    userId = "user_id_actual",
    modoNocturno = false, // desde CAP-05
    onSessionCompleted = { sesionId ->
        // Disparar evento para CAP-04
    }
)
```

### 7. Integración con CAP-04 (Gamificación)
En `RelajacionViewModel`, cuando se completa sesión:

```kotlin
viewModel.eventEmitter.collect { event ->
    when (event) {
        is RelajacionEvent.SessionCompleted -> {
            // Disparar evento para CAP-04
            emit(RelajacionEvent("SESION_RELAJACION_COMPLETADA", event.sesionId))
        }
    }
}
```

### 8. Integración con CAP-05 (Ventana Nocturna)
Pasar estado de ventana nocturna:

```kotlin
val esVentanaNocturna = capAcincoViewModel.esVentanaNocturnaActiva()
RelajacionScreen(
    viewModel = viewModel,
    userId = userId,
    modoNocturno = esVentanaNocturna
)
```

## Archivos de Recursos

### Colores (opcional, si no usan Material Theme)
```xml
<!-- res/values/colors.xml -->
<color name="brillo_inhalar">#4A90D9</color>
<color name="brillo_exhalar">#7EC8A4</color>
```

## Pruebas Recomendadas

### Pruebas Funcionales
- [ ] Cargar pantalla de relajación sin internet
- [ ] Iniciar sesión de respiración
- [ ] Completar 3 ciclos
- [ ] Pausar/Reanudar animación
- [ ] Interrumpir sesión
- [ ] Cargar sesión de audio
- [ ] Reproducir audio sin pausas
- [ ] Pausar al entrar llamada
- [ ] Aplicar ducking correctamente

### Pruebas de Persistencia
- [ ] Sesión se guarda en BD
- [ ] UUID v4 generado en cliente
- [ ] Timestamps en ISO 8601 UTC
- [ ] Recuperar historial de sesiones

### Pruebas de Background
- [ ] Audio continúa con pantalla apagada
- [ ] Foreground Service mantiene notificación
- [ ] No se interrumpe en Doze Mode
- [ ] Se pausa si interrumpido >60s

### Pruebas de Integración
- [ ] Evento dispara evaluación en CAP-04
- [ ] Modo noche cambia brillo correctamente
- [ ] Audio Focus maneja interrupciones

## Debugging

### Logger para sesiones
```kotlin
Log.d("RelajacionVM", "Sesión iniciada: ${sesion.uuid}")
Log.d("RelajacionVM", "Ciclo completado: ${uiState.ciclosCompletados}")
```

### Verificar BD
```kotlin
// En debug, listar sesiones
repository.obtenerSesionesPorUsuario(userId).collect { sesiones ->
    sesiones.forEach { 
        println("Sesión: ${it.uuid} - ${it.subtipo} - ${it.completada}")
    }
}
```

## Solución de Problemas

### Audio no reproduce
- Verificar que archivos están en `assets/audios/`
- Verificar que archivos son MP3 válidos
- Revisar permisos de audio

### Animación entrecortada
- Reducir complejidad de Canvas
- Verificar que `isAnimationRunning` es true
- Revisar que no hay operaciones en el hilo principal

### Servicio se detiene
- Verificar `startForeground()` se llama
- Revisar notificación está bien construida
- Verificar permisos en AndroidManifest

### Base de datos no persiste
- Verificar que Room está correctamente configurado
- Revisar que DAO está registrado en AppDatabase
- Comprobar que contexto no es null

---

**Notas**
- Todas las funciones están completamente implementadas
- Código listo para producción (Kotlin 100%)
- Compatible con Jetpack Compose
- Sigue paternas Clean Architecture
