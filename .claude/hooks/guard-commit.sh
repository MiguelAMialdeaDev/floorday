#!/usr/bin/env bash
# PreToolUse (matcher Bash, sin "if": se filtra aquí dentro para no depender de cómo se
# emparejan los comandos compuestos). Primera barrera de la política de autoría; la segunda
# es .githooks/commit-msg y la definitiva el check commit-policy del CI. exit 2 bloquea.
set -uo pipefail
source "$(dirname "${BASH_SOURCE[0]}")/lib.sh"

# Filtro barato sobre la entrada cruda: si no menciona git ni variables de git, no hay nada que mirar.
case "$HOOK_INPUT" in *git*|*GIT_*) ;; *) exit 0 ;; esac
require_json_or_block

cmd="$(json_get tool_input.command)"

# 1. Vías para saltarse los hooks de git, en cualquier comando.
[[ "$cmd" == *core.hooksPath* ]] && block "BLOQUEADO: core.hooksPath lo gestiona el hook SessionStart."
grep -Eq 'GIT_CONFIG_(PARAMETERS|COUNT|KEY_|VALUE_)' <<<"$cmd" && block "BLOQUEADO: configuración de git por variables de entorno."
grep -Eq '(^|[;&|[:space:]])git([[:space:]]+-[^[:space:]]+)*[[:space:]]+(commit-tree|update-ref|replace)([[:space:]]|$)' <<<"$cmd" \
  && block "BLOQUEADO: fontanería de git que no pasa por los hooks (commit-tree, update-ref, replace)."
[[ "$cmd" == *--no-verify* ]] && block "BLOQUEADO: --no-verify. Los hooks de git no se saltan nunca."

[[ "$cmd" == *"git commit"* ]] || exit 0

cwd="$(json_get cwd)"
[[ -n "$cwd" ]] && cd "${cwd//\\//}" 2>/dev/null
policy="$ROOT/.githooks/authorship-policy.sh"
[[ -f "$policy" ]] || block "No encuentro $policy. La política de autoría es obligatoria."

# Opciones del commit, sin el texto del mensaje (evita falsos positivos con "-n" o "-a" dentro de -m).
commit_part="${cmd#*git commit}"
opts="${commit_part%%-m *}"; opts="${opts%%-F *}"; opts="${opts%%<<*}"
if grep -Eq '(^|[[:space:]])-[a-zA-Z]*n[a-zA-Z]*([[:space:]]|$)' <<<"$opts"; then
  block "BLOQUEADO: git commit -n salta los hooks. Si era otra opción, ejecútala aparte."
fi

base="$(git rev-parse -q --verify HEAD 2>/dev/null || git hash-object -t tree /dev/null)"
[[ "$opts" == *--amend* ]] && base="$(git rev-parse -q --verify 'HEAD^' 2>/dev/null || git hash-object -t tree /dev/null)"
paths="$(git diff --cached --name-only --no-renames "$base" 2>/dev/null)"
if grep -Eq '(^|[[:space:]])(--all|-[a-zA-Z]*a[a-zA-Z]*)([[:space:]]|$)' <<<"$opts"; then
  paths="$paths"$'\n'"$(git diff --name-only --no-renames 2>/dev/null)"
fi
category="$(bash "$policy" category <<<"$paths")"

case "$category" in
  empty) exit 0 ;;
  mixed)
    listing="$(grep -v '^$' <<<"$paths" | sort -u | while IFS= read -r p; do bash "$policy" classify "$p"; done)"
    block "BLOQUEADO: commit mixto." "$listing" \
          "Usa la skill /commit: deja en el índice solo la zona asistida y commitea eso;" \
          "la zona humana (IA y CI/CD) y la de release no las commitea Claude." ;;
  human|release)
    block "BLOQUEADO: cambios de zona $category." \
          "Según docs/ai/politica-autoria.md los commitea Miguel (o release-please), sin coautoría." \
          "Déjalos en el índice y propón a Miguel el mensaje de commit en el chat." ;;
  assisted)
    msg="$cmd"
    if [[ "$commit_part" =~ (-F|--file)[=[:space:]]+([^[:space:]]+) ]]; then
      f="${BASH_REMATCH[2]}"; f="${f//\"/}"; f="${f//\'/}"
      [[ -f "$f" ]] && msg="$(cat "$f")"
    fi
    if ! grep -Eqi 'co-authored-by:[[:space:]]*claude[^<]*<noreply@anthropic\.com>' <<<"$msg"; then
      block "BLOQUEADO: commit de código sin trailer de coautoría." \
            "Termina el mensaje con una línea en blanco y: Co-Authored-By: Claude <modelo> <noreply@anthropic.com>"
    fi ;;
esac
exit 0
