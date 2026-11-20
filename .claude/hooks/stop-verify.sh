#!/usr/bin/env bash
# Stop y SubagentStop: si hay cambios de Kotlin, recursos o build que no pasaron por
# .claude/hooks/verify.sh, pide a Claude que verifique antes de dar el trabajo por hecho.
# No bloquea en bucle: respeta stop_hook_active y el tope de continuaciones de Claude Code.
set -uo pipefail
source "$(dirname "${BASH_SOURCE[0]}")/lib.sh"
_pick_backend
[[ "$_json_backend" == none ]] && exit 0

event="$(json_get hook_event_name)"; event="${event:-Stop}"
[[ "$(json_get stop_hook_active)" == "true" ]] && exit 0

cd "$ROOT" 2>/dev/null || exit 0
git rev-parse --git-dir >/dev/null 2>&1 || exit 0

changed="$( { git diff --name-only HEAD; git ls-files --others --exclude-standard; } 2>/dev/null \
  | grep -E '\.(kt|kts|xml|toml|json)$' | grep -vE '(^|/)(build|\.claude)/' || true)"
[[ -z "$changed" ]] && exit 0

current="$( { git diff HEAD --no-ext-diff --binary
  git ls-files --others --exclude-standard -z | xargs -0 -r cat 2>/dev/null
} | git hash-object --stdin)"
verified="$(cat "$ROOT/.claude/.state/verified" 2>/dev/null || true)"
[[ "$current" == "$verified" ]] && exit 0

n="$(wc -l <<<"$changed" | tr -d ' ')"
emit_context "$event" "Hay $n ficheros de código, recursos o build sin verificar desde el último verify. Antes de terminar ejecuta: bash .claude/hooks/verify.sh (o --full si tocaste build, manifest o alarmas). Si no se puede ejecutar aquí, dilo explícitamente en el resumen final y explica por qué."
exit 0
