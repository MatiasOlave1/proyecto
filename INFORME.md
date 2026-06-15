# Informe Técnico de Desarrollo: Rama `ccampos` - DormiBienU

Este documento detalla el trabajo de ingeniería de software implementado en la rama `ccampos` para la aplicación Android **DormiBienU**. El objetivo del desarrollo ha sido establecer una base sólida para la persistencia local de datos, inyección de dependencias, un sistema de navegación fluido con Compose y una interfaz de usuario pulida y responsiva alineada con las especificaciones del diseño.

---

## 1. Arquitectura de Datos y Persistencia Local (Room & SQLite)

Se implementó una base de datos local robusta utilizando la biblioteca **Room** de Jetpack. Esto asegura el cumplimiento del requisito de funcionamiento offline.

### 1.1. Base de Datos (`AppDatabase.kt`)
Clase abstracta que extiende `RoomDatabase`, configurada con versión `1` y que define los siguientes DAOs para el acceso a datos. Cuenta con un convertidor de tipos (`Converters.kt`) para transformar tipos complejos como `LocalDateTime`, `SleepQuality`, `SyncStatus`, `AchievementType` y listas de `String` en primitivos SQLite.

### 1.2. Entidades y Modelos de Datos (`data/local/entity` y `data/local/model`)
Se crearon las siguientes tablas relacionales con sus respectivas claves primarias y campos tipados:

1. **`UserEntity` (Tabla `usuarios`)**:
   - `id` (UUID - TEXT, Primary Key)
   - `nombreCompleto` (TEXT)
   - `fechaNacimiento` (TEXT)
   - `region` (TEXT)
   - `comuna` (TEXT)
   - `universidad` (TEXT)
   - `carrera` (TEXT)
   - `email` (TEXT, Unique)
   - `passwordHash` (TEXT - encriptado con AES-256 localmente)
   - `createdAt` y `updatedAt` (TEXT - Timestamps ISO 8601 UTC)

2. **`SleepRecordEntity` (Tabla `sleep_records`)**:
   - `id` (UUID - TEXT, Primary Key)
   - `userId` (TEXT, Foreign Key opcional/referencia a `UserEntity`)
   - `startTime` y `endTime` (TEXT - fecha y hora de sueño)
   - `durationMinutes` (INTEGER - cálculo automático)
   - `quality` (TEXT - enum `SleepQuality`: `EXCELLENT`, `GOOD`, `FAIR`, `POOR`)
   - `notes` (TEXT)
   - `syncStatus` (TEXT - enum `SyncStatus`: `SYNCED`, `PENDING_INSERT`, `PENDING_UPDATE`)
   - `createdAt` y `updatedAt` (TEXT)

3. **`WeeklyGoalEntity` (Tabla `weekly_goals`)**:
   - `id` (UUID, Primary Key)
   - `userId` (TEXT)
   - `targetSleepHours` (DOUBLE)
   - `targetBedtime` (TEXT)
   - `startDate` y `endDate` (TEXT)
   - `isAchieved` (BOOLEAN)
   - `createdAt` y `updatedAt` (TEXT)

4. **`StreakDataEntity` (Tabla `streaks`)**:
   - `id` (UUID, Primary Key)
   - `userId` (TEXT)
   - `currentStreak` y `longestStreak` (INTEGER)
   - `lastActiveDate` (TEXT)
   - `updatedAt` (TEXT)

5. **`AchievementEntity` (Tabla `achievements`)**:
   - `id` (UUID, Primary Key)
   - `userId` (TEXT)
   - `type` (TEXT - enum `AchievementType`: `FIRST_LOG`, `STREAK_7_DAYS`, `STREAK_30_DAYS`, `GOAL_MET`)
   - `title` y `description` (TEXT)
   - `unlockedAt` (TEXT - nulo si no está desbloqueado)
   - `isUnlocked` (BOOLEAN)

6. **`JournalEntryEntity` (Tabla `journal_entries`)**:
   - `id` (UUID, Primary Key)
   - `userId` (TEXT)
   - `entryDate` (TEXT)
   - `content` (TEXT)
   - `moodEmoji` (TEXT)
   - `stressLevel` (INTEGER)
   - `createdAt` (TEXT)

7. **`CircadianAlertEntity` (Tabla `circadian_alerts`)**:
   - `id` (UUID, Primary Key)
   - `userId` (TEXT)
   - `alertTime` (TEXT)
   - `message` (TEXT)
   - `alertType` (TEXT)
   - `isActive` (BOOLEAN)
   - `createdAt` (TEXT)

---

## 2. Inyección de Dependencias (Hilt)

Se configuró el framework **Dagger Hilt** para proveer componentes desacoplados de manera automática a lo largo de la aplicación.

- **`DormiBienUApplication.kt`**: Clase de aplicación anotada con `@HiltAndroidApp` que inicializa el grafo de dependencias en el arranque de la app.
- **`DatabaseModule.kt`**: Módulo Hilt (`@Module`, `@InstallIn(SingletonComponent::class)`) que provee la instancia única (`@Singleton`) de `AppDatabase` construida sobre el contexto de la aplicación, así como instancias específicas de cada uno de los DAOs (`UserDao`, `SleepRecordDao`, etc.).
- **`MainActivity`**: Anotada con `@AndroidEntryPoint` para permitir la inyección de dependencias en la actividad principal.

---

## 3. Sistema de Navegación (Jetpack Compose)

Se implementó un grafo de navegación tipado y desacoplado utilizando `navigation-compose`.

- **`Screen.kt`**: Clase sellada (`sealed class`) que define las rutas para las **14 pantallas** de la aplicación:
  - `Login`, `Register` (Autenticación)
  - `Home` (Contenedor principal con Bottom Bar)
  - `SleepLog`, `SleepHistory` (Registro e Historial de Sueño)
  - `WeeklyGoals`, `Achievements`, `Streaks` (Metas, Logros y Rachas)
  - `Dashboard`, `CircadianAlert` (Analíticas y Alertas de Ritmo Circadiano)
  - `AlarmCalculator`, `DisconnectReminder`, `Journal`, `RelaxLibrary` (Bienestar y Relajación)
- **`AppNavigation.kt`**: Define el `NavHost` y asocia cada ruta de `Screen` con su respectiva pantalla `composable`, gestionando el paso de parámetros y la pila de navegación.

---

## 4. Rediseño del Frontend y Componentes UI (Material 3 Dark Theme)

El frontend fue rediseñado de forma exhaustiva para ofrecer una experiencia visual de alta calidad que simula interfaces modernas de tipo "glassmorphic" y paletas oscuras armoniosas basadas en HSL.

### 4.1. Estructura de Pestañas del Home (`HomeScreen.kt`)
La pantalla principal actúa como contenedor dinámico con una barra de navegación inferior (`NavigationBar`) que intercambia 4 secciones (Tabs) principales sin destruir la pila de navegación:

1. **Dashboard (`DashboardScreen.kt`)**:
   - Visualización de estadísticas de sueño semanales/mensuales mediante gráficos personalizados en Compose (`Canvas`).
   - Métricas clave: Promedio de horas de sueño, eficiencia de sueño y horas ideales.
   - Tarjetas informativas de alertas circadianas.

2. **Noche / Relajación (`RelaxLibraryScreen.kt`)**:
   - Biblioteca de sonidos relajantes (Lluvia, Ruido Blanco, Bosque, etc.).
   - Reproductor multimedia integrado (diseño premium con controles de reproducción, barra de progreso y temporizador de apagado).
   - Listado de lecturas recomendadas para mejorar la higiene del sueño.

3. **Historial (`SleepHistoryScreen.kt`)**:
   - Lista detallada de los registros de sueño almacenados.
   - Filtros rápidos por rango de fechas y calidad de sueño.
   - Gráficos de tendencias integrados para analizar el sueño a lo largo del tiempo.

4. **Logros / Gamificación (`AchievementsScreen.kt`)**:
   - Sistema de gamificación con una barra de progreso que indica el porcentaje de logros desbloqueados.
   - Cuadrícula responsiva (`LazyVerticalGrid`) que renderiza tarjetas de logros premium, diferenciando visualmente los logros bloqueados de los desbloqueados mediante opacidades, bordes degradados e íconos específicos.

### 4.2. Estilo Visual y Tematización
- **`Theme.kt`**: Configura un esquema de colores oscuros Material 3 enriquecido, estableciendo tonos primarios naranjas cálidos, fondos gris profundo y acentos de color de estado (éxito, advertencia, información).
- **`Color.kt`**: Paleta de colores refinada (naranja atardecer, gris oscuro antracita, etc.) evitando colores por defecto del sistema.
- **`Type.kt`**: Tipografía premium configurada usando la familia de fuentes *Outfit* / *Inter* (configurada mediante Google Fonts si está disponible en línea, o fuentes por defecto escaladas).
- **Vectores personalizados**: Implementación de íconos personalizados dibujados dinámicamente mediante `Canvas` en Compose (por ejemplo, el ícono `MoonIcon` en la barra de navegación).

---

## 5. Infraestructura y Configuración de Compilación (Gradle & KSP)

Se corrigieron múltiples fallos de dependencias en Gradle para garantizar la estabilidad del proyecto:

1. **Migración a KSP (Kotlin Symbol Processing)**:
   - Se reemplazó el plugin heredado `kapt` por `ksp` para Room y Hilt, reduciendo significativamente los tiempos de compilación.
   - Configuración agregada en `build.gradle.kts` raíz y de la aplicación.
2. **Corrección de Crashes en Tiempo de Ejecución**:
   - Se alinearon las versiones de las librerías de Room (`2.6.1`), Hilt (`2.50`), y Jetpack Compose Compiler.
   - Se configuró la anotación `@AndroidEntryPoint` en `MainActivity` y se registró la clase `DormiBienUApplication` en el archivo `AndroidManifest.xml` (solucionando el crash por falta de inicialización del contenedor Hilt).
