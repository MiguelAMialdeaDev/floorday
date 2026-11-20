#!/usr/bin/env bash
# Verificación local antes de dar una tarea por terminada.
#   bash .claude/hooks/verify.sh          rápida: detekt + tests de los módulos tocados
#   bash .claude/hooks/verify.sh --full   lo mismo que el CI de PR (pr.yml)
# Si pasa, guarda la huella del árbol de trabajo en .claude/.state/verified para que el hook
# Stop sepa que estos cambios exactos ya se verificaron. Lo usan Claude y las personas.
set -euo pipefail
ROOT="$(git rev-parse --show-toplevel)"
cd "$ROOT"
STATE_DIR="$ROOT/.claude/.state"
mkdir -p "$STATE_DIR"

fingerprint() {
  { git diff HEAD --no-ext-diff --binary
    git ls-files --others --exclude-standard -z | xargs -0 -r cat 2>/dev/null
  } | git hash-object --stdin
}

FULL_TASKS=(detekt lint allTests testAndroidHostTest koverVerify checkModuleGraph verifyMergedManifest :androidApp:assembleRelease)

base="$(git merge-base HEAD origin/main 2>/dev/null || git rev-parse HEAD)"
changed="$( { git diff --name-only "$base"; git ls-files --others --exclude-standard; } | sort -u)"

mode="${1:-quick}"
# Cambios de build o de versiones afectan a todo: verificación completa.
if grep -Eq '^(build-logic/|gradle/|settings\.gradle\.kts|build\.gradle\.kts|gradle\.properties)' <<<"$changed"; then
  mode="--full"
fi

tasks=()
if [[ "$mode" == "--full" ]]; then
  tasks=("${FULL_TASKS[@]}")
else
  seen=" "
  while IFS= read -r f; do
    [[ "$f" == *.kt || "$f" == *.kts || "$f" == *.xml || "$f" == *.json ]] || continue
    d="$(dirname "$f")"
    while [[ "$d" != "." && ! -f "$d/build.gradle.kts" ]]; do d="$(dirname "$d")"; done
    [[ "$d" == "." ]] && continue
    module=":${d//\//:}"
    [[ "$seen" == *" $module "* ]] && continue
    seen="$seen$module "
    if [[ "$module" == ":androidApp" ]]; then
      tasks+=("$module:detekt" "$module:testDebugUnitTest")
    else
      tasks+=("$module:detekt" "$module:testAndroidHostTest")
    fi
  done <<<"$changed"
fi

if ((${#tasks[@]} == 0)); then
  echo "verify: no hay cambios de Kotlin ni recursos que verificar."
  fingerprint >"$STATE_DIR/verified"
  exit 0
fi

echo "verify ($mode): ./gradlew ${tasks[*]} --continue"
./gradlew "${tasks[@]}" --continue
fingerprint >"$STATE_DIR/verified"
echo "verify: OK. Huella guardada."
