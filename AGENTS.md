# AGENTS.md: Floorday

Instrucciones para cualquier agente de código (Claude Code, Codex, Gemini CLI, Copilot, Cursor) y para personas.
Este fichero es la fuente única. `CLAUDE.md` lo importa y solo añade lo específico de Claude Code.
Cómo se trabaja con IA en este repo, y por qué: `docs/ai/README.md`.

## Qué es Floorday

App de constancia con "Día Mínimo": de 3 a 5 checks diarios, recordatorios de medicación con alarmas insistentes, rachas indulgentes y peso. Offline, sin cuentas y sin red. Android primero (Play, cuenta "Mialdea Studio"), iOS en marzo de 2027 con el mismo código común.

- Paquete: `studio.mialdea.floorday`. minSdk 28, targetSdk 36.
- Kotlin Multiplatform + Compose Multiplatform. MVI con StateFlow, Koin 4.2, Room 3 (`androidx.room3`, `BundledSQLiteDriver`), Navigation 3, kotlinx-datetime.
- Versiones: solo las de `gradle/libs.versions.toml`. Nunca añadas una versión a mano en un `build.gradle.kts`.

## Mapa de módulos

| Módulo | Contiene | Puede depender de |
|---|---|---|
| `:androidApp` | Application, MainActivity, manifest, receivers de alarmas, canales, WorkManager, Glance, Health Connect, `androidPlatformModule` | todo |
| `:shared` | `FloordayApp()` (NavDisplay raíz), `initKoin()`, `MainViewController()` | core, feature |
| `:core:domain` | `model/`, reglas, casos de uso, interfaces de repositorio, `platform/` | nada (Kotlin puro + kotlinx) |
| `:core:data` | Room 3, DataStore, repositorios, `ReminderSync`, export/import JSON | `:core:domain` |
| `:core:designsystem` | tema, tokens, componentes, `MviViewModel` | `:core:domain` |
| `:core:testing` | `FakeClock`, fakes de plataforma, repos en memoria, builders | `:core:domain` |
| `:feature:*` | pantallas (Route + Screen + ViewModel + Reducer) | core; **nunca otro feature** |

Paquetes: `studio.mialdea.floorday.<módulo>.<capa>`, por ejemplo `studio.mialdea.floorday.feature.today.ui`.
Plugins de convención en `build-logic/convention`: `floorday.kmp.library`, `floorday.kmp.compose`, `floorday.kmp.feature`, `floorday.android.application`, `floorday.room`, `floorday.quality`.

## Comandos

```bash
./gradlew assembleDebug                         # compilar la app
bash .claude/hooks/verify.sh                    # rápido: detekt + tests de los módulos tocados
bash .claude/hooks/verify.sh --full             # lo mismo que el CI de PR
./gradlew :core:domain:testAndroidHostTest      # tests comunes de un módulo en la JVM
./gradlew detekt lint koverVerify               # calidad
./gradlew checkModuleGraph verifyMergedManifest # guardas de arquitectura y permisos
./gradlew :core:data:assemble                   # KSP regenera core/data/schemas tras cambiar entidades
bash .githooks/authorship-policy.sh metrics     # porcentaje de commits asistidos por IA
```

Primera vez en un clon: `git config core.hooksPath .githooks` (activa `commit-msg` y `prepare-commit-msg`).

## Convenciones canónicas

**MVI.** Cada pantalla tiene `XState` (data class inmutable), `XIntent` y `XEffect` (sealed), un `XReducer` puro (`reduce(state, event): State`) y un `XViewModel : MviViewModel<S, I, E>`. La UI se divide en `XRoute` (obtiene el ViewModel con Koin, recoge `state` con `collectAsStateWithLifecycle()` y `effects` en un `LaunchedEffect`) y `XScreen(state, onIntent)` sin estado, que es la que se previsualiza y se testea.

**Koin.** Un módulo Koin por módulo Gradle (`xModule`), declarado en `di/`. Constructores con dependencias explícitas; nada de `KoinComponent` ni `get()` fuera de `di/`. `Clock` y `TimeZone` se proveen solo en `di/`.

**Room 3.** Room es la fuente de verdad. Entidades y DAOs en `:core:data`. Esquemas exportados en `core/data/schemas` y versionados. Toda subida de versión lleva migración y test de migración.

**Navigation 3.** Claves de destino `@Serializable` en el feature que las posee. El back stack vive en `:shared`. Un feature navega emitiendo un efecto `Navigate`, nunca importando otro feature.

**Tiempo.** `kotlin.time.Instant` y `kotlin.time.Clock` (importados explícitamente), `LocalDate`/`LocalTime` de kotlinx-datetime. Las horas de recordatorio son `LocalTime` flotantes. El día lógico empieza a la hora configurada (04:00 por defecto).

**Errores.** El dominio devuelve tipos sellados (`Result` propio o sealed interface), no lanza excepciones para flujos esperados.

**Estilo.** ktlint vía detekt-formatting. Nombres en inglés en el código; comentarios solo cuando el porqué no es obvio.

## Reglas duras (el build, los hooks o el CI las hacen cumplir)

1. Nada de `android.*`, `androidx.core.*`, `java.*` ni `javax.*` en `commonMain` ni `commonTest`. expect/actual solo para `databaseBuilder()`, `dataStorePath()`, `DateFormatter` e `is24HourFormat()`; todo lo demás, interfaces en `:core:domain/platform` resueltas por Koin.
2. `Clock` y `TimeZone` siempre inyectados. Prohibido `Clock.System` y `TimeZone.currentSystemDefault()` fuera de `di/`.
3. Alarmas: Room (`scheduled_alarm`) es la fuente de verdad y `ReminderSync` es el único punto que llama a `ReminderScheduler`. Idempotente y en transacción.
4. Permisos prohibidos: `USE_EXACT_ALARM`, `USE_FULL_SCREEN_INTENT`, `FOREGROUND_SERVICE*`, `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`, `INTERNET`, `SYSTEM_ALERT_WINDOW`. Sin foreground services: prohibido `setExpedited`, `setForeground` y `setForegroundAsync`.
5. Sin red: ninguna dependencia que necesite Internet. Toda dependencia nueva revisa "Seguridad de los datos" de Play.
6. Textos de UI en Compose resources, ES (`values-es`) y EN (`values`), con plurales cuando toque. Notificaciones y widget en `:androidApp/res/values(-es)`. Nunca un literal en un `Text`.
7. Accesibilidad: áreas táctiles de 48 dp, `contentDescription` o semántica en todo lo interactivo, el estado nunca depende solo del color, contraste AA.
8. Un feature nunca depende de otro feature. `:core:domain` no depende de Room ni de Compose.
9. Vocabulario: "recordatorio", "toma", "cantidad". Nunca "tratamiento", "terapia" ni "dosis recomendada". Floorday no es un dispositivo médico.
10. Nunca se editan a mano: esquemas Room, `build/`, código generado, wrapper de Gradle, keystores.

## Definición de hecho

Un cambio está hecho cuando:
- [ ] Cumple los criterios de aceptación del issue, uno por uno.
- [ ] Tiene tests: lógica en `commonTest` (con `FakeClock`), DAOs y receivers en `androidHostTest`, pantalla con test de reducer y de ViewModel (Turbine).
- [ ] `bash .claude/hooks/verify.sh` pasa (o `--full` si toca build, manifest, alarmas o Room).
- [ ] Strings en ES y EN; accesibilidad revisada; sin literales de UI.
- [ ] Si es una decisión de arquitectura, tiene ADR en `docs/adr/`.
- [ ] Si cambia permisos, dependencias o datos tratados: checklist de Play revisada (`docs/play-release-checklist.md`).
- [ ] Commits según la política de autoría y Conventional Commits. PR con la plantilla completa.

## Política de autoría de commits

Detalle y motivo: `docs/ai/politica-autoria.md`. Fuente ejecutable: `.githooks/authorship-policy.sh`.

- **Zona humana**: `AGENTS.md` y `CLAUDE.md` (en cualquier carpeta), `.claude/**`, `.mcp.json`, `.githooks/**`, `.github/**`, `docs/ai/**`, `fastlane/**`, `Gemfile*`, `release-please-config.json`, `.gitattributes`. Los commitea Miguel, **sin** trailer de IA. Tú no puedes editarlos.
- **Zona release**: `CHANGELOG.md`, `version.txt`, `.release-please-manifest.json`. Solo release-please.
- **Zona asistida**: todo lo demás. Cada commit lleva `Co-Authored-By: Claude <modelo> <noreply@anthropic.com>`.
- **Prohibido mezclar** zonas en un commit. Prohibido `--no-verify`.
- Lo aplican cinco capas: Claude no puede editar la zona humana (deny en `.claude/settings.json`), hook de Claude Code, permisos, hooks `prepare-commit-msg`, `commit-msg` y `pre-push` de git, y el check obligatorio `commit-policy` del CI, que es la fuente de verdad.

## Qué no se toca nunca sin permiso explícito de Miguel

- Permisos del manifest, `targetSdk`, `minSdk`, `applicationId` y firma.
- `gradle/libs.versions.toml` (versiones) y `build-logic/`.
- Esquema de Room y migraciones ya publicadas.
- Cualquier fichero de la zona humana (IA y CI/CD).
- ADRs aceptados (se crea uno nuevo que sustituye al anterior).
- Textos legales: disclaimer médico, política de privacidad, ficha de Play.
- Publicar: tags, releases, fastlane, Play Console, merge de PRs.

Ante la duda: parar, explicar las opciones y preguntar.

## Flujo de trabajo

Rama `tipo/<issue>-descripcion` (por ejemplo `feat/42-dose-snooze-chain`). Un PR por cambio lógico, con la plantilla rellena. Los `fixup!` se aplican con `git rebase -i --autosquash` antes de pedir merge. Merge solo por rebase (historia lineal, se conserva la autoría de cada commit). Proceso completo: `docs/ai/README.md`.
