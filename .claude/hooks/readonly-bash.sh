#!/usr/bin/env bash
# PreToolUse de los subagentes revisores (revisor-kmp, auditor-play): solo dejan pasar
# comandos de lectura. Cualquier otro Bash se bloquea con el motivo.
set -uo pipefail
source "$(dirname "${BASH_SOURCE[0]}")/lib.sh"
require_json_or_block

cmd="$(json_get tool_input.command)"
# Sin encadenados, redirecciones ni sustituciones: un solo comando legible.
if grep -Eq '[;&|><`]|\$\(' <<<"$cmd"; then
  block "Subagente de solo lectura: usa un comando simple, sin ; && | > ni \$()."
fi
allowed='^(git (diff|log|show|status|blame|ls-files|rev-parse|merge-base)( |$)|gh (pr|issue) (view|diff|checks)( |$)|bash \.githooks/authorship-policy\.sh (classify|metrics)( |$)|ls( |$)|wc( |$))'
if ! grep -Eq "$allowed" <<<"$cmd"; then
  block "Subagente de solo lectura: '$cmd' no está en la lista (git diff/log/show/status/blame, gh pr|issue view/diff/checks, ls, wc)."
fi
exit 0
