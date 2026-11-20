#!/usr/bin/env bash
# SessionStart: comprueba que el entorno aplica las reglas y da a Claude el contexto vivo
# (rama, issue y estado). Lo que imprime por stdout entra en el contexto de Claude.
set -uo pipefail
source "$(dirname "${BASH_SOURCE[0]}")/lib.sh"
cd "$ROOT" 2>/dev/null || exit 0

notes=()
hooks_path="$(git config --get core.hooksPath 2>/dev/null || true)"
if [[ "$hooks_path" != ".githooks" ]]; then
  # El hook corre fuera de los permisos de Claude: puede activar los hooks de git del repo.
  if git config core.hooksPath .githooks 2>/dev/null; then
    notes+=("Hooks de git activados (core.hooksPath=.githooks; antes '$hooks_path').")
  else
    notes+=("AVISO: no se pudieron activar los hooks de git. Pide a Miguel: git config core.hooksPath .githooks")
  fi
fi
_pick_backend
if [[ "$_json_backend" == none ]]; then
  notes+=("AVISO: no hay jq, node ni python3. Los guardas de edición y commit bloquearán todo hasta instalar jq.")
fi

branch="$(git branch --show-current 2>/dev/null || true)"
issue=""
[[ "$branch" =~ ^[a-z]+/([0-9]+)- ]] && issue="${BASH_REMATCH[1]}"

echo "Contexto de sesión de Floorday:"
echo "- Rama: ${branch:-desconocida}${issue:+ (issue #$issue: léela con gh issue view $issue antes de planificar)}"
echo "- Cambios sin commitear: $(git status --porcelain 2>/dev/null | wc -l | tr -d ' ')"
echo "- Últimos commits:"
git log -5 --format='  %h %s' 2>/dev/null
for n in ${notes[@]+"${notes[@]}"}; do echo "- $n"; done
exit 0
