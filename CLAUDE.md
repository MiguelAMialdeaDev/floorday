@AGENTS.md

# Claude Code en Floorday

Lo anterior es común a todos los agentes. Esto es solo para Claude Code.

## Cómo trabajas aquí

1. **Antes de tocar código**: lee el issue (`gh issue view <n>`), resume los criterios de aceptación y propone un plan corto (ficheros, tests, riesgos). Para cambios de más de un módulo o con decisión de diseño, usa modo plan y espera el visto bueno de Miguel.
2. **Implementa en pasos pequeños**, con el test primero cuando la lógica es de dominio.
3. **Verifica** con `bash .claude/hooks/verify.sh` antes de decir que algo está hecho. Si no se puede ejecutar, dilo.
4. **Commitea** con la skill `/commit`: separa por zonas, Conventional Commits y trailer de coautoría solo en la zona asistida.
5. **Resume** al final: qué cambió, cómo se verificó, qué queda abierto y qué debe revisar Miguel con más atención.

## Reglas de comportamiento

- No inventes APIs ni versiones. Si dudas de una API de Android, KMP, Room 3, Koin o Nav3, compruébala en la documentación oficial (dominios permitidos en `.claude/settings.json`) y cita la fuente en el PR.
- No amplíes el alcance: si ves algo mejorable fuera del issue, apúntalo en el resumen; no lo arregles de paso.
- No desactives un test, una regla de detekt ni un check del CI para que algo pase. Si una regla parece errónea, explícalo y pregunta.
- La zona humana (IA y CI/CD) no la editas ni la commiteas: los permisos te lo impiden. Si crees que una regla, un hook, una skill o el CI deben cambiar, propón el diff en el chat con el motivo y Miguel lo aplica y lo firma.
- Si un hook te bloquea, lee el motivo y corrige la causa. No busques rodeos.
- Cuando algo te salga mal y Miguel lo corrija, propón la entrada para `docs/ai/errores-aprendidos.md`.

## Herramientas del proyecto

| Tipo | Nombre | Para qué |
|---|---|---|
| Skill | `/nueva-feature-kmp` | módulo `:feature:x` completo con plugin de convención, Koin y navegación |
| Skill | `/pantalla-mvi` | State, Intent, Effect, Reducer, ViewModel, Route, Screen y tests |
| Skill | `/migracion-room` | subir versión de la BD con migración y test |
| Skill | `/cambio-alarmas` | cualquier cambio en recordatorios, con checklist de fiabilidad |
| Skill | `/cumplimiento-play` | auditoría de política de Play antes de un PR o release |
| Skill | `/release` | preparar versión, changelog y tag (solo la lanza Miguel) |
| Skill | `/adr` | registro de decisión de arquitectura |
| Skill | `/design-doc` | documento de diseño antes de una feature grande |
| Skill | `/revision-pr` | revisión de un PR con el subagente `revisor-kmp` |
| Skill | `/commit` | commits que cumplen la política de autoría |
| Agente | `revisor-kmp` | revisión de código, solo lectura |
| Agente | `auditor-play` | cumplimiento de Play y privacidad, solo lectura |
| Agente | `escritor-tests` | escribe y ejecuta tests, no toca código de producción |

Las reglas por ruta de `.claude/rules/` se cargan solas al abrir ficheros de cada zona.
