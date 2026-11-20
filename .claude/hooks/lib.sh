#!/usr/bin/env bash
# Utilidades compartidas por los hooks de Claude Code de Floorday.
# Portables: Linux, macOS y Git Bash de Windows. Lectura de JSON con jq; si no hay jq,
# con node o python3 (comprobando que el intérprete funciona de verdad: en Windows
# "python3" puede ser el alias vacío de la Microsoft Store).

HOOK_INPUT="$(cat)"

ROOT="${CLAUDE_PROJECT_DIR:-$(git rev-parse --show-toplevel 2>/dev/null || pwd)}"
ROOT="${ROOT//\\//}"

_json_backend=""
_pick_backend() {
  [[ -n "$_json_backend" ]] && return 0
  if command -v jq >/dev/null 2>&1; then _json_backend=jq
  elif command -v node >/dev/null 2>&1 && node -e "0" >/dev/null 2>&1; then _json_backend=node
  elif command -v python3 >/dev/null 2>&1 && python3 -c "0" >/dev/null 2>&1; then _json_backend=python3
  else _json_backend=none; fi
}

# json_get <ruta con puntos>   ej.: json_get tool_input.file_path
# Imprime el valor (cadena o JSON compacto) o nada si no existe.
json_get() {
  _pick_backend
  local path="$1"
  case "$_json_backend" in
    jq) jq -r --arg p "$path" 'getpath($p | split(".")) // empty | if type == "string" then . else tojson end' <<<"$HOOK_INPUT" ;;
    node) node -e '
      let d = JSON.parse(require("fs").readFileSync(0, "utf8") || "{}");
      for (const k of process.argv[1].split(".")) { d = d == null ? undefined : d[k]; }
      if (d !== undefined && d !== null) process.stdout.write(typeof d === "string" ? d : JSON.stringify(d));
    ' "$path" <<<"$HOOK_INPUT" ;;
    python3) python3 -c '
import json, sys
d = json.loads(sys.stdin.read() or "{}")
for k in sys.argv[1].split("."):
    d = d.get(k) if isinstance(d, dict) else None
if d is not None:
    sys.stdout.write(d if isinstance(d, str) else json.dumps(d))
' "$path" <<<"$HOOK_INPUT" ;;
    none) return 3 ;;
  esac
}

# Texto nuevo que una herramienta de edición quiere escribir (Write, Edit o edits[] múltiples).
new_text() {
  _pick_backend
  case "$_json_backend" in
    jq) jq -r '.tool_input | [.content, .new_string, (.edits // [] | .[].new_string)] | map(select(. != null)) | join("\n")' <<<"$HOOK_INPUT" ;;
    node) node -e '
      const t = (JSON.parse(require("fs").readFileSync(0, "utf8") || "{}").tool_input) || {};
      const parts = [t.content, t.new_string, ...((t.edits || []).map(e => e.new_string))].filter(x => x != null);
      process.stdout.write(parts.join("\n"));
    ' <<<"$HOOK_INPUT" ;;
    python3) python3 -c '
import json, sys
t = json.loads(sys.stdin.read() or "{}").get("tool_input") or {}
parts = [t.get("content"), t.get("new_string")] + [e.get("new_string") for e in t.get("edits") or []]
sys.stdout.write("\n".join(p for p in parts if p is not None))
' <<<"$HOOK_INPUT" ;;
    none) return 3 ;;
  esac
}

# Ruta relativa a la raíz del repo, con "/" y sin distinguir mayúsculas en la letra de unidad.
rel_path() {
  local p="${1//\\//}"
  local lp lr; lp="$(tr "[:upper:]" "[:lower:]" <<<"$p")"; lr="$(tr "[:upper:]" "[:lower:]" <<<"$ROOT")"
  if [[ "$lp" == "$lr/"* ]]; then p="${p:${#ROOT}+1}"; fi
  printf '%s' "${p#./}"
}

# Bloquea la acción (PreToolUse) o informa a Claude (PostToolUse): stderr + exit 2.
block() { printf '%s\n' "$@" >&2; exit 2; }

# Sin lector de JSON no podemos validar: los guardas fallan cerrados.
require_json_or_block() {
  _pick_backend
  if [[ "$_json_backend" == none ]]; then
    block "Hook de Floorday: no hay jq, node ni python3 para leer la entrada del hook." \
          "Instala jq (winget install jqlang.jq | brew install jq | apt install jq) y repite."
  fi
}

# Salida JSON mínima para añadir contexto sin error (Stop, SubagentStop, SessionStart).
emit_context() {
  local event="$1" text="$2"
  text="${text//\\/\\\\}"; text="${text//\"/\\\"}"; text="${text//$'\n'/\\n}"
  printf '{"hookSpecificOutput":{"hookEventName":"%s","additionalContext":"%s"}}\n' "$event" "$text"
}
