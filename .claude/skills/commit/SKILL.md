---
name: commit
description: Crea commits que cumplen la política de autoría de Floorday. Separa los cambios en zona humana (IA y CI/CD, sin coautoría) y zona asistida (código, con trailer Co-Authored-By de Claude), escribe mensajes Conventional Commits y nunca mezcla zonas. Úsala siempre que vayas a hacer git commit en este repo.
argument-hint: "[mensaje o ámbito opcional]"
allowed-tools: Bash(git status *) Bash(git diff *) Bash(git add *) Bash(git restore --staged *) Bash(bash .githooks/authorship-policy.sh *)
---

# Commit con política de autoría

Contexto actual:
- Estado: !`git status --short`
- Clasificación de lo que hay en el índice y fuera de él: !`git status --porcelain | cut -c4- | xargs -r bash .githooks/authorship-policy.sh classify`

## Reglas

- **Zona humana** (`AGENTS.md`, `CLAUDE.md`, `.claude/**`, `.githooks/**`, `docs/ai/**`, `.github/**`, `fastlane/**`, `Gemfile*`, `renovate.json*`): **tú no la commiteas**. Deja esos ficheros fuera del índice y, al final, propón a Miguel el mensaje para que lo haga él.
- **Zona asistida** (todo lo demás): commit con trailer de coautoría.
- **Nunca** un commit que mezcle zonas. **Nunca** `--no-verify`, `-n` ni `git config`.
- Un commit = un cambio lógico que compila y pasa tests por sí solo. Si la zona asistida tiene dos cambios lógicos, son dos commits.

## Pasos

1. Revisa la clasificación de arriba. Si hay ficheros `human`, sácalos del índice: `git restore --staged <ruta>`.
2. Agrupa los ficheros `assisted` por cambio lógico. Para cada grupo:
   1. `git add <rutas del grupo>` (rutas explícitas; nunca `git add -A` ni `git add .`).
   2. Comprueba: `git diff --cached --name-only | bash .githooks/authorship-policy.sh category` debe imprimir `assisted`.
   3. Escribe el mensaje:
      - Asunto: `tipo(ámbito): descripción` en minúsculas, imperativo, máximo 72 caracteres. Tipos: `feat fix perf refactor test build docs chore style revert a11y i18n` (`ci` es zona humana).
      - Ámbitos habituales: `domain`, `data`, `today`, `history`, `settings`, `weight`, `reminders`, `designsystem`, `testing`, `app`, `build`.
      - Cuerpo: el porqué y lo no obvio, en líneas de 72 caracteres. Referencia al issue: `Refs #42` o `Closes #42`.
      - Trailer, tras una línea en blanco: `Co-Authored-By: Claude <modelo exacto> <noreply@anthropic.com>`.
   4. Ejecuta el commit con heredoc para conservar el formato:
      ```bash
      git commit -F - <<'MSG'
      feat(reminders): restart retry chain when snoozing

      Snoozing cancels the pending retry and schedules a single SNOOZE alarm,
      so at most one alarm per dose is active at any time.

      Closes #57

      Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>
      MSG
      ```
3. Si un hook rechaza el commit, lee el motivo, corrige la causa y repite. No reintentes con variaciones para esquivarlo.
4. Si quedan cambios de zona humana, termina con un bloque para Miguel:
   ```
   Pendiente de tu commit (zona humana):
     git add <rutas>
     git commit -m "ci: <descripción>"
   ```

## Comprobación final

`git log -3 --format='%h %s%n%(trailers:key=Co-authored-by)'` muestra el trailer solo en los commits de código.
