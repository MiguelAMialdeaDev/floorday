---
paths:
  - "**/src/commonMain/**/*.kt"
  - "core/domain/**/*.kt"
---

# Código común (commonMain y :core:domain)

Este código compila para Android e iOS. Lo que no compile en iOS rompe marzo.

- Solo Kotlin estándar, kotlinx (coroutines, datetime, serialization) y librerías KMP del catálogo. Nada de `android.*`, `androidx.core.*`, `java.*` ni `javax.*` (lo bloquean el hook y detekt `ForbiddenImport`).
- APIs de plataforma: interfaz en `:core:domain/platform` (`ReminderScheduler`, `NotificationGateway`, `PermissionController`, `WidgetRefresher`, `HealthGateway`, `DeviceInfo`, `DateFormatter`) e implementación en `:androidApp`, registrada en `androidPlatformModule`.
- expect/actual solo para `databaseBuilder()`, `dataStorePath()`, `DateFormatter` e `is24HourFormat()`. Uno nuevo necesita ADR.
- Tiempo: `kotlin.time.Clock` y `TimeZone` por constructor. `Clock.System` y `TimeZone.currentSystemDefault()` solo en `di/`.
- Identificadores: `kotlin.uuid.Uuid`. Cantidades de medicación en cuartos (`Int`: 4 = 1, 2 = ½).
- Nada de `String.format`: números con el formateador propio por locale; fechas con `DateFormatter`.
- `:core:domain` no conoce Room, Compose ni Koin. Las reglas son funciones puras o clases con dependencias explícitas y se prueban al 100 % en `commonTest`.
- Concurrencia: `suspend` y `Flow`. Dispatchers inyectados si hacen falta; nunca `GlobalScope` ni `runBlocking` en producción.
