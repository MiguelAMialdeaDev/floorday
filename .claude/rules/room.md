---
paths:
  - "core/data/**"
---

# Persistencia (Room 3 y DataStore)

- Room 3: paquete `androidx.room3`, `BundledSQLiteDriver`, builder por expect/actual. Nada de versiones alpha.
- `exportSchema = true` y esquemas en `core/data/schemas`, versionados. **Nunca** se editan a mano (lo bloquean el hook y los permisos). Usa `/migracion-room`.
- Toda subida de `version` lleva migración explícita (`Migration`, o `@AutoMigration` con spec cuando aplique), test de migración desde cada versión publicada y entrada en el CHANGELOG.
- DAOs: `Flow` para observar y `suspend` para escribir. Las escrituras compuestas van en una única transacción de escritura.
- Las entidades de Room no salen de `:core:data`: los repositorios mapean a modelos de `:core:domain`.
- Columnas de tiempo: `Instant` como epoch millis, `LocalDate` como ISO `yyyy-MM-dd`, `LocalTime` como minutos del día. Converters con test.
- Tests de DAO en `androidHostTest`, con BD en memoria y `BundledSQLiteDriver`.
- DataStore: dos ficheros separados (ajustes de usuario y estado interno). Claves tipadas en un solo objeto por fichero.
