#!/usr/bin/env bash
# Tests de la política de autoría sobre un repo temporal.
# Los ejecuta el job commit-policy en cada PR y se pueden lanzar en local.
set -euo pipefail
unset GITHUB_ACTIONS GITHUB_OUTPUT GITHUB_STEP_SUMMARY CLAUDECODE FLOORDAY_PR_AUTHOR FLOORDAY_SUBJECT_MODE
HOOKS="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
POLICY="$HOOKS/authorship-policy.sh"
repo="$(mktemp -d)"; msg="$(mktemp)"; trap 'rm -rf "$repo" "$msg"' EXIT
cd "$repo"
git init -q -b main
git config user.name test; git config user.email test@example.com; git config commit.gpgsign false; git config core.autocrlf false
T='Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>'
echo base >README.md; git add -A; git commit -q --no-verify -m "chore: init" -m "$T"
BASE="$(git rev-parse HEAD)"
fails=0

expect() { # <ok|fail> <descripción> <comando...>
  local want="$1" desc="$2" got; shift 2
  if "$@" >/dev/null 2>&1; then got=ok; else got=fail; fi
  if [[ "$got" == "$want" ]]; then echo "ok    $desc"; else echo "FALLA $desc (esperado $want, obtenido $got)"; fails=1; fi
}
touch_files() { local f; for f in $1; do mkdir -p "$(dirname "$f")"; echo "$RANDOM" >>"$f"; done; git add -A; }
scenario() { # <ok|fail> <descripción> "<ficheros>" <asunto> [trailer]
  git checkout -q -B t "$BASE"; touch_files "$3"
  if [[ -n "${5:-}" ]]; then git commit -q --no-verify -m "$4" -m "$5"; else git commit -q --no-verify -m "$4"; fi
  expect "$1" "$2" bash "$POLICY" check-range "$BASE" HEAD
}

# Clasificación y CI (check-range)
scenario ok   "código con trailer"              "core/domain/A.kt"                         "feat(domain): add habit" "$T"
scenario fail "código sin trailer"              "core/domain/A.kt"                         "feat(domain): add habit"
scenario ok   "build-logic es asistida"         "build-logic/convention/X.kt"              "build(logic): add quality" "$T"
scenario ok   "workflow sin trailer"            ".github/workflows/pr.yml"                 "ci: add pr workflow"
scenario fail "workflow con trailer"            ".github/workflows/pr.yml"                 "ci: add pr workflow" "$T"
scenario fail "workflow con Claude-Session"     ".github/workflows/pr.yml"                 "ci: add pr workflow" "Claude-Session: https://claude.ai/x"
scenario ok   "CLAUDE.md anidado sin trailer"   "core/data/CLAUDE.md"                      "docs(ai): data rules"
scenario ok   ".gitattributes es humana"        ".gitattributes"                           "chore: line endings"
scenario ok   "ADR es asistida"                 "docs/adr/0015-ai-authorship.md"           "docs(adr): authorship policy" "$T"
scenario fail "commit mixto"                    "core/domain/A.kt .claude/settings.json"   "feat: mixed" "$T"
scenario fail "código más CHANGELOG"            "core/domain/A.kt CHANGELOG.md"            "feat: mixed" "$T"
scenario ok   "release-please"                  "CHANGELOG.md version.txt .release-please-manifest.json" "chore(main): release 0.9.0"
scenario fail "asunto no convencional"          "core/domain/A.kt"                         "Added stuff" "$T"
scenario fail "asunto de más de 72"             "core/domain/A.kt"                         "feat(domain): $(printf 'x%.0s' $(seq 1 70))" "$T"
scenario fail "fixup sin aplicar en CI"         "core/domain/A.kt"                         "fixup! feat(domain): add habit" "$T"
scenario ok   "revert de git"                   "core/domain/A.kt"                         'Revert "feat(domain): add habit"' "$T"

git checkout -q -B t "$BASE"; touch_files "gradle/libs.versions.toml"
GIT_AUTHOR_NAME='renovate[bot]' git commit -q --no-verify -m "build(deps): update dependency org.jetbrains.kotlin:kotlin-gradle-plugin to v9.9.9 (major)"
expect ok   "Renovate: rutas suyas y asunto largo" bash "$POLICY" check-range "$BASE" HEAD
expect fail "Renovate falsificado en PR de otro"   env FLOORDAY_PR_AUTHOR=MiguelAMialdeaDev bash "$POLICY" check-range "$BASE" HEAD

git checkout -q -B t "$BASE"; touch_files "core/domain/A.kt"
GIT_AUTHOR_NAME='renovate[bot]' git commit -q --no-verify -m "build(deps): sneaky"
expect fail "Renovate fuera de sus rutas" bash "$POLICY" check-range "$BASE" HEAD

git checkout -q -B t "$BASE"; touch_files ".claude/rules/a.md"
GIT_AUTHOR_NAME='claude[bot]' git commit -q --no-verify -m "docs(ai): rule" -m "$T"
expect fail "claude[bot] en zona humana" bash "$POLICY" check-range "$BASE" HEAD

git checkout -q -B t "$BASE"; touch_files "core/A.kt"
GIT_AUTHOR_NAME='evil[bot]' git commit -q --no-verify -m "feat: x"
expect fail "bot no autorizado" bash "$POLICY" check-range "$BASE" HEAD

git checkout -q -B side "$BASE"; touch_files "core/B.kt"; git commit -q --no-verify -m "feat: b" -m "$T"
git checkout -q -B t "$BASE"; touch_files "core/C.kt"; git commit -q --no-verify -m "feat: c" -m "$T"
git merge -q --no-ff --no-edit side -m "chore: merge" >/dev/null
expect fail "commit de merge en el rango" bash "$POLICY" check-range "$BASE" HEAD

expect ok "commit raíz (ROOT)" bash "$POLICY" check-range ROOT "$BASE"

# Hook commit-msg (check-msg): rechaza, no reescribe
git checkout -q -B t "$BASE"; touch_files "app.kt"
printf 'feat(app): add entry point\n' >"$msg"
expect fail "commit-msg: código sin trailer" bash "$POLICY" check-msg "$msg"
printf 'feat(app): add entry point\n\n%s\n' "$T" >"$msg"
expect ok   "commit-msg: código con trailer" bash "$POLICY" check-msg "$msg"
printf 'feat(app): add entry point\n\n%s\n# ------------------------ >8 ------------------------\ndiff --git a/x b/x\n' "$T" >"$msg"
expect ok   "commit-msg: commit -v (tijera)" bash "$POLICY" check-msg "$msg"

git reset -q --hard "$BASE"; touch_files ".github/CODEOWNERS"
printf 'ci: codeowners\n\n%s\n' "$T" >"$msg"
expect fail "commit-msg: config con trailer" bash "$POLICY" check-msg "$msg"
printf 'ci: codeowners\n' >"$msg"
expect ok   "commit-msg: config sin trailer" bash "$POLICY" check-msg "$msg"
expect fail "commit-msg: Claude no commitea config" env CLAUDECODE=1 bash "$POLICY" check-msg "$msg"

git reset -q --hard "$BASE"; touch_files "core/B.kt .claude/x.md"
printf 'feat: mixed\n\n%s\n' "$T" >"$msg"
expect fail "commit-msg: mixto" bash "$POLICY" check-msg "$msg"

git reset -q --hard "$BASE"; touch_files "core/B.kt"
printf 'fixup! feat: b\n\n%s\n' "$T" >"$msg"
expect ok "commit-msg: fixup! en local" bash "$POLICY" check-msg "$msg"

# --amend: un commit asistido al que se añade config pasa a ser mixto
git reset -q --hard "$BASE"; touch_files "core/D.kt"; git commit -q --no-verify -m "feat: d" -m "$T"
touch_files ".github/x.yml"; printf 'feat: d\n\n%s\n' "$T" >"$msg"
bash "$HOOKS/prepare-commit-msg" "$msg" commit HEAD
expect fail "commit-msg: amend que mezcla zonas" bash "$POLICY" check-msg "$msg"

# Primer commit del repo (sin HEAD)
empty="$(mktemp -d)"
( cd "$empty" && git init -q -b main && mkdir -p .github && echo x >.github/CODEOWNERS && git add -A \
  && printf 'ci: codeowners\n' >"$msg" && bash "$POLICY" check-msg "$msg" ) >/dev/null 2>&1 \
  && echo "ok    commit-msg: primer commit del repo" || { echo "FALLA commit-msg: primer commit del repo"; fails=1; }
rm -rf "$empty"

exit "$fails"
