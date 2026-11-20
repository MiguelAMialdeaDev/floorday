# Política de autoría de commits

- Estado: Aceptada (registrada como ADR-0015 en `docs/adr/README.md`)
- Fecha: 2026-11-20
- Decisor: Miguel
- Sustituye a: la línea "Sin Co-Authored-By ni rastro de IA" del plan inicial

## Contexto

Floorday se desarrolla con Claude Code y quiero que eso se vea, porque el proyecto es también un ejemplo de cómo un ingeniero trabaja con IA bajo reglas. Para que el historial sea honesto y útil tiene que responder a dos preguntas sin ambigüedad: qué código ha pasado por la IA y quién escribió las reglas que la gobiernan.

## Decisión

El repositorio se divide en zonas, definidas en un único sitio ejecutable: `HUMAN_PATTERNS` y `RELEASE_PATTERNS` en `.githooks/authorship-policy.sh`.

| Zona | Rutas | Quién responde | Trailer de Claude |
|---|---|---|---|
| Humana | `AGENTS.md` y `CLAUDE.md` (en cualquier carpeta), `.claude/**`, `.mcp.json`, `.githooks/**`, `.github/**` (workflows, scripts de CI, plantillas, Renovate), `docs/ai/**`, `fastlane/**`, `Gemfile`, `Gemfile.lock`, `release-please-config.json`, `.gitattributes` | Miguel: decide, escribe o reescribe, revisa línea a línea y firma como único autor. Claude no puede editarla (`deny`) | **Prohibido** cualquier rastro de IA (`Co-Authored-By` de Claude o Anthropic, `Claude-Session`, "Generated with Claude Code") |
| Asistida | Todo lo demás: código, tests, recursos, build de la app (`build-logic/`, `gradle/`), `.gitignore`, docs de ingeniería y de producto, ADRs | Miguel con Claude en el bucle | **Obligatorio** `Co-Authored-By: Claude <modelo> <noreply@anthropic.com>` |
| Release | `CHANGELOG.md`, `version.txt`, `.release-please-manifest.json` | release-please | Prohibido |
| Bot | Autor `renovate[bot]`, solo en `gradle/libs.versions.toml`, `gradle/wrapper/**`, `gradle/verification-metadata.xml`, `.github/workflows/**`, `.github/actions/**`, `Gemfile.lock` | Renovate | Prohibido |

Reglas:
1. Un commit toca una sola zona. Los mixtos se rechazan y se separan, normalmente en dos PR.
2. Asunto en Conventional Commits, 72 caracteres como máximo (a Renovate no se le aplica el límite). En local se admiten `fixup!`, `squash!` y `amend!`; el CI los rechaza: se aplican con `git rebase -i --autosquash origin/main`.
3. `main` solo admite rebase merge y un PR no puede contener commits de merge: cada commit llega a `main` tal como se validó.
4. Los reverts se hacen en local con `git revert`: el de código asistido es asistido y lleva el trailer.
5. `claude[bot]` (Actions) solo puede tocar la zona asistida y con trailer. Cualquier otro bot falla.
6. Un commit con autor `renovate[bot]` solo vale dentro de un PR abierto por `renovate[bot]`: el CI cruza el autor del commit con el del PR, así que falsificar el nombre en local no sirve.
7. El trailer nombra el modelo real. Claude Code lo escribe solo (`attribution.commit` por defecto); a mano se usa el alias `git ai`.
8. Prohibido `--no-verify`.

Por qué la zona humana es exactamente esa: son las reglas que gobiernan a la IA y la cadena que lleva código a los usuarios. Si la IA escribiera sus propias reglas o su propio CI, los controles dejarían de ser independientes. Por eso la regla no es solo "no lleva trailer" sino "Claude no puede escribirla": así lo que dice el historial es cierto. Claude puede proponer un cambio a una regla en el chat, con su motivo; lo aplico yo y lo firmo yo.

## Cómo se aplica (defensa en profundidad)

| Capa | Fichero | Cuándo actúa | Qué hace si falla |
|---|---|---|---|
| 1. Separación de funciones | `.claude/settings.json` (`deny` de `Edit` sobre las zonas humana y release) | Siempre que Claude intenta escribir ahí, también con redirecciones, `sed` o `tee` en Bash | Denegado sin pregunta |
| 2. Hook de Claude Code | `.claude/hooks/guard-commit.sh` (`PreToolUse` con matcher `Bash`, sin `if`: filtra dentro) | Antes de cada comando Bash que mencione git | Bloquea: zona humana o release, mixto, sin trailer, `--no-verify`, `-n`, `core.hooksPath`, `commit-tree`, `update-ref`, `replace`, `GIT_CONFIG_*` |
| 3. Permisos de Claude Code | `.claude/settings.json` | Siempre | `git commit` y `git push` piden confirmación; denegados `--no-verify`, `git -c`, `git -C`, `git config`, `commit-tree`, `update-ref`, `replace` |
| 4. Hooks de git | `.githooks/prepare-commit-msg`, `commit-msg` y `pre-push` | En cada commit y push local, de cualquier herramienta o persona, también con `--amend`, `commit -v` y el primer commit del repo | Rechaza; no reescribe el mensaje en silencio |
| 5. CI | `.github/workflows/commit-policy.yml` (job `commit-policy`) en cada PR; job `commit-policy` de `.github/workflows/main.yml` en cada push a `main` | Siempre | Check obligatorio en rojo, con tabla por commit en el resumen; en `main` corta el build y la subida a Play |

La capa 5 es la fuente de verdad. En el PR se ejecuta la versión de la política (y de sus tests) que hay en la rama base, no la del PR: un cambio no puede relajar la regla que lo juzga. Las capas 1 a 4 dan respuesta rápida, pero un hook local se puede no instalar; el ruleset de GitHub no se salta.

Hueco residual conocido: si Claude commitea configuración saltándose a la vez el deny, el guard y los hooks, el CI no distingue ese commit de uno mío. Lo cubre la revisión de cada commit en el PR.

## Configuración necesaria

1. Hooks de git: el hook `SessionStart` de Claude Code fija `core.hooksPath=.githooks`. Fuera de Claude Code, una vez por clon: `git config core.hooksPath .githooks`.
2. Alias para commits asistidos a mano: `git config alias.ai 'commit --trailer "Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>"'` (se actualiza al cambiar de modelo).
3. En GitHub, `.github/scripts/apply-repo-settings.sh` (idempotente) deja:
   - solo rebase merge, borrado de ramas al fusionar, "Update branch" con rebase;
   - ruleset de `main`: PR obligatorio con 0 aprobaciones y hilos resueltos, checks `pr-gate`, `pr-meta` y `commit-policy` de la app de GitHub Actions con la rama al día, historia lineal, sin force push ni borrado y sin bypass;
   - **no** se exigen commits firmados: GitHub no firma los commits que reescribe al hacer rebase merge y el ruleset bloquearía todos los merges. Firmo en local con SSH, como recomendación.
4. En Claude Code: `attribution.commit` sin fijar (por defecto nombra el modelo en uso), `attribution.pr` vacío (la plantilla de PR ya tiene su sección de IA) y `attribution.sessionUrl` en `false`.

## Instalación inicial (orden de commits)

La propia instalación cumple la política:
1. `chore: add authorship policy and git hooks` (humano, sin trailer): `.gitattributes`, `.githooks/`, `docs/ai/politica-autoria.md`.
2. `chore(ai): add claude code layer` (humano, sin trailer): `AGENTS.md`, `CLAUDE.md`, `.claude/`, resto de `docs/ai/`.
3. `ci: add pr workflows and templates` (humano, sin trailer): `.github/`.
4. `build: ignore local claude settings` (asistido, con trailer): líneas nuevas de `.gitignore`.

Antes de los commits 1 a 3 reviso y adapto cada fichero: la primera versión de esta capa se redactó con ayuda de Claude, y firmarla sin trailer solo es honesto si el contenido final es mío y lo puedo defender línea a línea.

## Consecuencias

- El reparto por zona se calcula del historial (`authorship-policy.sh metrics`) y se publica en cada PR. Describe el proceso, no la calidad.
- Hay más commits pequeños: cambiar una regla de IA y el código que la cumple son dos commits, y normalmente dos PR.
- Si algún día escribo código sin IA, este esquema lo marcaría como asistido. Hoy todo el código pasa por Claude (escritura, revisión o tests); si eso cambia, se revisa esta política con un ADR nuevo, no con excepciones sueltas.
- `git commit -c <sha>` se trata como `--amend` y puede dar un falso "mixto": basta con repetirlo sin `-c`.

## Comprobación rápida

```bash
bash .githooks/authorship-policy.sh classify .github/workflows/pr.yml core/domain/src/commonMain/kotlin/X.kt
# human     .github/workflows/pr.yml
# assisted  core/domain/src/commonMain/kotlin/X.kt
bash .githooks/authorship-policy.sh check-range origin/main HEAD
bash .githooks/tests/authorship-policy.test.sh
```
