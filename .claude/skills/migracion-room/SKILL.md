---
name: migracion-room
description: Cambia el esquema de Room 3 en Floorday de forma segura. Sube la versión de la base de datos, escribe la migración, regenera el esquema exportado con KSP y añade el test de migración desde cada versión publicada. Úsala para cualquier cambio en entidades, columnas, índices o tablas de :core:data.
argument-hint: "<descripción del cambio> [#issue]"
---

# Migración de Room 3

Versión actual y esquemas exportados:
- !`grep -rn "version *=" core/data/src/commonMain --include=*Database.kt`
- !`ls core/data/schemas/*/`

Versiones ya publicadas en Play (no se pueden romper): consulta `CHANGELOG.md` o pregunta a Miguel.

## Reglas

- Los ficheros de `core/data/schemas/` **los genera KSP**. No se editan a mano (hook y permisos lo bloquean).
- Una migración publicada no se modifica nunca. Si estaba mal, se corrige con otra migración.
- Cero pérdida de datos del usuario: no hay servidor ni copia en la nube que lo arregle.

## Pasos

1. **Plan en el chat**: qué cambia, versión N a N+1, si es automática o manual y qué pasa con las filas existentes (valor por defecto, transformación). Espera el visto bueno si hay transformación de datos.
2. **Entidad**: aplica el cambio en la entidad o el DAO.
3. **Versión**: `version = N + 1` en `@Database`.
4. **Migración**:
   - Cambio aditivo simple (columna nueva con default, tabla nueva): `@AutoMigration(from = N, to = N + 1)`.
   - Renombrar o borrar: `@AutoMigration` con `spec` (`@RenameColumn`, `@DeleteColumn`...).
   - Transformar datos: `Migration(N, N + 1)` manual en `migrations/MigrationNtoN1.kt`, con SQL explícito y probado.
5. **Regenerar el esquema**: `./gradlew :core:data:assemble`. Debe aparecer `core/data/schemas/<db>/<N+1>.json` nuevo y el anterior intacto.
6. **Test de migración** en `androidHostTest` con `BundledSQLiteDriver`:
   - Crea la BD en la versión N con datos representativos (incluidas fechas, cuartos de cantidad y alarmas programadas).
   - Ejecuta la migración y valida el esquema.
   - Comprueba que los datos se conservan o transforman como se espera.
   - Repite desde cada versión publicada anterior si la cadena es larga.
7. **Efectos colaterales**:
   - ¿Toca `scheduled_alarm`? Tras migrar, `ReminderSync.resyncAll()` debe reconstruir las alarmas (test).
   - ¿Toca export/import JSON? Sube la versión del formato y prueba importar un JSON antiguo.
   - ¿Toca backup y restauración? Revisa `backup_rules` y la detección de restauración.
8. **Verificar**: `bash .claude/hooks/verify.sh --full`.
9. **Commit** con `/commit`: `feat(data): <cambio> (db v<N+1>)`, que incluya entidad, migración, esquema generado y test en el mismo commit.
10. **CHANGELOG**: entrada en "Sin publicar" con la versión de la BD.
