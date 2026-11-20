---
name: revision-pr
description: Revisa un PR o la rama actual de Floorday con el subagente revisor-kmp (solo lectura). Comprueba corrección, reglas duras del proyecto, arquitectura KMP, tests, accesibilidad, i18n y política de autoría, y devuelve hallazgos priorizados con evidencia. Úsala antes de abrir un PR y cuando Miguel pida una segunda opinión.
argument-hint: "[número de PR o rango, por defecto main...HEAD]"
context: fork
agent: revisor-kmp
---

# Revisión de código

Objetivo: `$ARGUMENTS` (si está vacío, `main...HEAD`; si es un número, el PR con `gh pr diff <n>` y `gh pr view <n>`).

Resumen del cambio:
!`git diff --stat main...HEAD`

Commits y su zona de autoría:
!`git log --format='%h %s' main..HEAD`

## Cómo revisar

1. Lee el issue enlazado (si lo hay) y comprueba cada criterio de aceptación contra el código.
2. Lee el diff completo con `git diff main...HEAD` y abre los ficheros enteros cuando el contexto lo pida.
3. Recorre la lista. Cada hallazgo necesita evidencia (`fichero:línea`) y un escenario concreto de fallo; si no lo tienes, no es un hallazgo.

### Lista

- **Corrección**: lógica, condiciones de carrera en Flows, cancelación de corrutinas, idempotencia de `ReminderSync`, zonas horarias y DST.
- **Reglas duras** de `AGENTS.md` (las 10).
- **Arquitectura**: dependencias entre módulos, nada de Room o Compose en `:core:domain`, interfaces de plataforma en su sitio, Koin solo en `di/`.
- **MVI**: reducer puro, Screen sin estado, efectos de una sola vez.
- **Tests**: ¿cubren el comportamiento nuevo y los bordes? ¿usan `FakeClock`? ¿algún test borrado o `@Ignore`?
- **UI**: textos en resources ES y EN, accesibilidad (48 dp, semántica, color), fuente al 200 %.
- **Rendimiento**: recomposiciones evitables, trabajo en el hilo principal, consultas sin índice.
- **Seguridad y privacidad**: `PendingIntent` inmutables, componentes exportados, datos en logs.
- **Autoría**: `bash .githooks/authorship-policy.sh classify` sobre los ficheros de cada commit; commits mixtos o sin trailer.

## Formato de salida

```
## Resumen
Una frase con el veredicto: listo para PR | cambios menores | cambios necesarios.

## Hallazgos
| # | Severidad | Fichero:línea | Problema | Escenario de fallo | Propuesta |
|---|-----------|---------------|----------|--------------------|-----------|
(Severidad: bloqueante, importante, menor, nit. Ordenados de mayor a menor.)

## Criterios de aceptación
| Criterio | Cumplido | Evidencia |

## Lo que revisé y lo que no
```

No propongas refactors fuera del alcance del PR salvo que sean bloqueantes; apúntalos aparte como "ideas para otro issue".
