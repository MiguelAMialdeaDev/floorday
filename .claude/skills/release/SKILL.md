---
name: release
description: Prepara la release que propone release-please (auditoría de cumplimiento, notas de versión ES y EN, verificación y checklist). La versión, el CHANGELOG y el tag los gestiona release-please; la promoción a Play la hace el job publish de main.yml con aprobación de Miguel. Nunca crea tags, nunca ejecuta fastlane y nunca publica.
argument-hint: "[número del PR de release-please]"
disable-model-invocation: true
---

# Preparar la release (PR de release-please: #$0)

Último tag, versión propuesta y commits desde entonces:
- !`git describe --tags --abbrev=0`
- !`cat version.txt`
- !`git log --oneline $(git describe --tags --abbrev=0)..origin/main`

## Lo que haces tú

1. **No toques** `CHANGELOG.md`, `version.txt` ni `.release-please-manifest.json` (zona release: los escribe release-please). El `versionCode` lo calcula el build de `main` a partir del número de commits.
2. **Auditoría**: ejecuta `/cumplimiento-play` sobre el rango desde el último tag. Si el veredicto no es APTO, para aquí y lista lo que falta.
3. **Notas de versión** para `fastlane/metadata/android/{es-ES,en-US}/changelogs/default.txt`, máximo 500 caracteres, en lenguaje de usuario (sin jerga técnica, sin "tratamiento" ni "terapia"). `fastlane/` es zona humana: **déjalas propuestas en el chat**. Van en un PR normal de Miguel antes de fusionar el PR de release, nunca en la rama de release-please (la reescribe y las borraría).
4. **Verificación completa**: `bash .claude/hooks/verify.sh --full`.

## Lo que hace Miguel (déjaselo escrito al final)

```
[ ] PR humano con las notas de fastlane, fusionado por rebase
[ ] docs/play-release-checklist.md completa
[ ] Formularios de Play actualizados si la auditoría lo pide
[ ] Fusionar el PR de release-please (crea el tag y el GitHub Release)
[ ] Aprobar el environment play-production en el job publish de main.yml
[ ] Producción: rollout.yml 0.2 -> 0.5 -> 1 según docs/engineering/slo-and-vitals.md
```
