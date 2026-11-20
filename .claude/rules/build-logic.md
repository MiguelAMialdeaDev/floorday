---
paths:
  - "build-logic/**"
  - "**/*.gradle.kts"
  - "gradle/libs.versions.toml"
  - "gradle.properties"
---

# Build (Gradle, convention plugins y catálogo)

- Toda configuración repetida va en un plugin de `build-logic/convention`. Un `build.gradle.kts` de módulo solo aplica plugins y declara dependencias.
- Versiones solo en `gradle/libs.versions.toml`, verificadas en la fuente oficial (enlace en el PR). Nada de versiones dinámicas (`+`, `latest.release`) ni alphas salvo ADR.
- Editar el catálogo, `settings.gradle.kts` o `build-logic/` pide permiso a Miguel (regla `ask`).
- Configuration cache y build cache activos: nada de `project` durante la ejecución de tareas y nada de leer ficheros en configuración sin `providers`.
- `floorday.quality` contiene las guardas: detekt (`ForbiddenImport` en commonMain, `ForbiddenMethodCall` de `setExpedited` y `setForeground`), kover, lint, `checkModuleGraph`, `verifyMergedManifest` y `check16KbAlignment`. No se relajan para que algo pase.
- Después de tocar build: `bash .claude/hooks/verify.sh --full`.
