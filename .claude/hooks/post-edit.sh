#!/usr/bin/env bash
# PostToolUse (Edit|Write): formatea y revisa el fichero recién editado.
# La edición ya está hecha; exit 2 solo sirve para que Claude lea los avisos y los corrija.
#
# Herramientas opcionales (rápidas, sin arrancar Gradle):
#   ktlint      -> en PATH o en FLOORDAY_KTLINT   (formatea .kt y .kts)
#   detekt-cli  -> en PATH como "detekt" o en FLOORDAY_DETEKT (análisis del fichero)
# Si no están, se aplican solo las comprobaciones de texto. El CI ejecuta siempre detekt completo.
set -uo pipefail
source "$(dirname "${BASH_SOURCE[0]}")/lib.sh"
_pick_backend
[[ "$_json_backend" == none ]] && exit 0

file="$(json_get tool_input.file_path)"
[[ -z "$file" ]] && exit 0
file="${file//\\//}"
rel="$(rel_path "$file")"
[[ -f "$file" ]] || exit 0
warnings=()

keys() { grep -oE '<(string|plurals) name="[^"]+"' "$1" 2>/dev/null | sed -E 's/.*name="([^"]+)"/\1/' | sort -u; }

case "$rel" in
  *.kt|*.kts)
    ktlint_bin="${FLOORDAY_KTLINT:-$(command -v ktlint 2>/dev/null || true)}"
    if [[ -n "$ktlint_bin" ]]; then
      out="$("$ktlint_bin" -F --relative "$file" 2>&1)" || warnings+=("ktlint no pudo corregirlo todo:" "$out")
    fi
    detekt_bin="${FLOORDAY_DETEKT:-$(command -v detekt 2>/dev/null || true)}"
    if [[ -n "$detekt_bin" && "$rel" == *.kt && -f "$ROOT/config/detekt/detekt.yml" ]]; then
      out="$("$detekt_bin" --input "$file" --config "$ROOT/config/detekt/detekt.yml" --build-upon-default-config 2>&1)" \
        || warnings+=("detekt encontró problemas:" "$out")
    fi
    # Textos de UI escritos a mano: deben salir de Compose resources (ES + EN).
    if [[ "$rel" == */src/commonMain/* && "$rel" != */preview/* ]]; then
      lit="$(grep -nE '(Text\([[:space:]]*"[^"]+"|contentDescription[[:space:]]*=[[:space:]]*"[^"]+")' "$file" || true)"
      [[ -n "$lit" ]] && warnings+=("Textos literales en UI ($rel). Muévelos a composeResources/values y values-es:" "$lit")
    fi
    ;;
  */composeResources/values/strings.xml|*/composeResources/values-es/strings.xml|*/res/values/strings.xml|*/res/values-es/strings.xml)
    base="${file%/values*/strings.xml}"
    if [[ -f "$base/values/strings.xml" && -f "$base/values-es/strings.xml" ]]; then
      missing_es="$(comm -23 <(keys "$base/values/strings.xml") <(keys "$base/values-es/strings.xml"))"
      missing_en="$(comm -13 <(keys "$base/values/strings.xml") <(keys "$base/values-es/strings.xml"))"
      [[ -n "$missing_es" ]] && warnings+=("Faltan en values-es (español):" "$missing_es")
      [[ -n "$missing_en" ]] && warnings+=("Faltan en values (inglés):" "$missing_en")
    fi
    ;;
esac

if ((${#warnings[@]})); then
  printf '%s\n' "${warnings[@]}" >&2
  exit 2
fi
exit 0
