#!/usr/bin/env bash
# Política de autoría de commits de Floorday. Fuente única de verdad (ADR-0015).
# La usan: .githooks/prepare-commit-msg, .githooks/commit-msg y .githooks/pre-push (local),
# .claude/hooks/guard-commit.sh (Claude Code), .github/workflows/commit-policy.yml (PR, se
# ejecuta la versión de la rama base) y el job commit-policy de .github/workflows/main.yml.
# Documentada en docs/ai/politica-autoria.md.
#
# Zonas:
#   human     configuración de IA y de CI/CD. La firma solo Miguel, SIN rastro de IA.
#             Claude no puede editarla (deny en .claude/settings.json).
#   release   ficheros que escribe release-please. Sin rastro de IA.
#   assisted  todo lo demás (código, tests, recursos, build, docs de producto y ADRs).
#             Trailer obligatorio: "Co-Authored-By: Claude[ <modelo>] <noreply@anthropic.com>".
#   Un commit que toca más de una zona es "mixed" y se rechaza: hay que separarlo.
#   Bots: solo renovate[bot] (rutas de dependencias) y claude[bot] (zona asistida, con trailer).
#   Asunto en Conventional Commits, 72 caracteres como máximo (salvo bots).
#   En local se admiten fixup!/squash!/amend!; en CI no. En un PR no puede haber merges.
#
# Uso:
#   authorship-policy.sh classify <ruta>...                       zona de cada ruta
#   authorship-policy.sh category                                 rutas por stdin: empty|human|release|assisted|mixed
#   authorship-policy.sh check-subject <asunto>                   Conventional Commits (modo local)
#   authorship-policy.sh check-msg <fichero-msg>                  commit en preparación (hook commit-msg)
#   authorship-policy.sh check-range <base|ROOT|remotes=NOMBRE> <head>   cada commit del rango
#   authorship-policy.sh metrics [<rango>]                        reparto de commits por zona
#
# Variables: FLOORDAY_SUBJECT_MODE=local (pre-push) | ci (por defecto en check-range).
#            FLOORDAY_PR_AUTHOR=<login> (CI): un commit "de bot" solo vale en un PR abierto por ese bot.
# Requisitos: bash 3.2+ y git 2.30+. Linux, macOS (bash del sistema) y Git Bash de Windows.
set -euo pipefail

# En [[ == ]] el asterisco también cruza "/". Cambiar estas listas es un cambio de política:
# PR propio de zona humana, ADR nuevo y docs/ai/politica-autoria.md actualizado.
HUMAN_PATTERNS=(
  "AGENTS.md" "*/AGENTS.md"
  "CLAUDE.md" "*/CLAUDE.md"
  ".claude/*"
  ".mcp.json"
  ".githooks/*"
  ".github/*"
  "docs/ai/*"
  "fastlane/*"
  "Gemfile" "Gemfile.lock"
  "release-please-config.json"
  ".gitattributes"
)
RELEASE_PATTERNS=(
  "CHANGELOG.md"
  "version.txt"
  ".release-please-manifest.json"
)
# Lo único que puede tocar Renovate.
BOT_PATH_RE='^(gradle/libs\.versions\.toml|gradle/wrapper/.*|gradle/verification-metadata\.xml|\.github/workflows/.*|\.github/actions/.*|Gemfile\.lock)$'

TRAILER_RE='^co-authored-by:[[:space:]]*claude([[:space:]][^<]*)?[[:space:]]*<noreply@anthropic\.com>[[:space:]]*$'
AI_MARK_RE='(co-authored-by:.*(claude|anthropic))|(^claude-session:)|(generated with \[?claude code)'
CC_RE='^(feat|fix|perf|refactor|test|build|ci|docs|chore|style|revert|a11y|i18n)(\([a-z0-9._/-]+\))?!?: [^[:space:]].*$'
REVERT_RE='^Revert "'
FIXUP_RE='^(fixup|squash|amend)! '
MAX_SUBJECT=72

die() { printf 'authorship-policy: %s\n' "$*" >&2; exit 2; }

classify_path() {
  local p="${1#./}" pat
  for pat in "${HUMAN_PATTERNS[@]}"; do
    # shellcheck disable=SC2053
    if [[ "$p" == $pat ]]; then echo human; return; fi
  done
  for pat in "${RELEASE_PATTERNS[@]}"; do
    # shellcheck disable=SC2053
    if [[ "$p" == $pat ]]; then echo release; return; fi
  done
  echo assisted
}

# Lee rutas por stdin y devuelve: empty | human | release | assisted | mixed
category_of_paths() {
  local p z h=0 r=0 a=0
  while IFS= read -r p; do
    [[ -z "$p" ]] && continue
    z="$(classify_path "$p")"
    case "$z" in human) h=1 ;; release) r=1 ;; *) a=1 ;; esac
  done
  case "$((h + r + a))" in
    0) echo empty ;;
    1) if ((h)); then echo human; elif ((r)); then echo release; else echo assisted; fi ;;
    *) echo mixed ;;
  esac
}

# subject_reason <asunto> <local|ci|bot>: vacío si es válido; si no, el motivo.
subject_reason() {
  local s="$1" mode="$2"
  [[ "$s" =~ $REVERT_RE ]] && return 0
  if [[ "$s" =~ $FIXUP_RE ]]; then
    [[ "$mode" == local ]] && return 0
    echo "fixup!/squash!/amend! sin aplicar: git rebase -i --autosquash origin/main"; return 0
  fi
  if [[ ! "$s" =~ $CC_RE ]]; then echo "Asunto fuera de Conventional Commits: '$s'"; return 0; fi
  if [[ "$mode" != bot ]] && ((${#s} > MAX_SUBJECT)); then
    echo "Asunto de ${#s} caracteres (máximo $MAX_SUBJECT): '$s'"
  fi
}

trailers_of() { git interpret-trailers --parse --no-divider 2>/dev/null | tr '[:upper:]' '[:lower:]'; }
has_claude_trailer() { trailers_of <<<"$1" | grep -Eq "$TRAILER_RE"; }
has_ai_mark() { tr '[:upper:]' '[:lower:]' <<<"$1" | grep -Eq "$AI_MARK_RE"; }

list_by_zone() {
  local p
  while IFS= read -r p; do [[ -n "$p" ]] && printf '  [%s] %s\n' "$(classify_path "$p")" "$p"; done <<<"$1"
}

# evaluate <categoría> <mensaje> <rutas>: 0 si cumple; si no, imprime el motivo y devuelve 1.
evaluate() {
  local cat="$1" msg="$2" paths="$3"
  case "$cat" in
    empty) return 0 ;;
    mixed)
      printf 'Commit MIXTO: toca más de una zona. Sepáralo (la configuración de IA y CI/CD va en su propio commit).\n'
      list_by_zone "$paths"
      return 1 ;;
    human|release)
      if has_ai_mark "$msg"; then
        printf 'Commit de zona %s con rastro de IA. Quita Co-Authored-By de Claude, Claude-Session y "Generated with Claude Code".\n' "$cat"
        return 1
      fi ;;
    assisted)
      if ! has_claude_trailer "$msg"; then
        printf 'Commit de zona asistida sin el trailer. Añade al final: Co-Authored-By: Claude <modelo> <noreply@anthropic.com> (o usa: git ai).\n'
        return 1
      fi ;;
  esac
  return 0
}

cmd_classify() { local p; for p in "$@"; do printf '%s\t%s\n' "$(classify_path "$p")" "$p"; done; }

cmd_check_msg() {
  local msg_file="${1:?falta el fichero del mensaje}" git_dir empty_tree base msg paths cat reason subject
  git_dir="$(git rev-parse --git-dir)"
  [[ -f "$git_dir/MERGE_HEAD" ]] && exit 0   # main solo admite rebase; el CI rechaza merges en el PR
  empty_tree="$(git hash-object -t tree /dev/null)"
  base="$(git rev-parse -q --verify HEAD || echo "$empty_tree")"
  if [[ -f "$git_dir/floorday-amend" ]]; then   # marca de prepare-commit-msg: el commit real es HEAD^..índice
    rm -f "$git_dir/floorday-amend"
    base="$(git rev-parse -q --verify 'HEAD^' || echo "$empty_tree")"
  fi
  # Corta el diff de "commit -v" (tijera) y quita comentarios antes de validar.
  msg="$(sed '/^# -\{24\} >8 -\{24\}$/,$d' "$msg_file" | git stripspace --strip-comments)"
  subject="$(sed -n '/[^[:space:]]/{p;q;}' <<<"$msg")"
  reason="$(subject_reason "$subject" local)"
  if [[ -z "$reason" ]]; then
    paths="$(git diff --cached --name-only --no-renames "$base")"
    cat="$(category_of_paths <<<"$paths")"
    if [[ "$cat" == human && "${CLAUDECODE:-}" == 1 ]]; then
      reason="La zona humana (configuración de IA y CI/CD) la commitea Miguel. Claude la deja preparada y avisa."
    else
      reason="$(evaluate "$cat" "$msg" "$paths")" || true
    fi
  fi
  if [[ -n "$reason" ]]; then
    printf '\n[commit-msg] Política de autoría (docs/ai/politica-autoria.md):\n%s\n\n' "$reason" >&2
    exit 1
  fi
}

# check_one <sha> <modo de asunto>: imprime "zona<TAB>motivo" (motivo vacío si cumple).
check_one() {
  local sha="$1" smode="$2" author msg paths cat reason="" outside
  author="$(git log -1 --format=%an "$sha")"
  msg="$(git log -1 --format=%B "$sha")"
  paths="$(git diff-tree --root --no-commit-id --name-only -r --no-renames "$sha")"
  case "$author" in
    'renovate[bot]')
      cat=bot
      outside="$(grep -v '^$' <<<"$paths" | grep -Ev "$BOT_PATH_RE" || true)"
      if [[ -n "${FLOORDAY_PR_AUTHOR:-}" && "$FLOORDAY_PR_AUTHOR" != 'renovate[bot]' ]]; then
        reason="Commit con autor renovate[bot] en un PR abierto por $FLOORDAY_PR_AUTHOR."
      elif [[ -n "$outside" ]]; then
        reason="renovate[bot] fuera de sus rutas: $(tr '\n' ' ' <<<"$outside")"
      elif has_ai_mark "$msg"; then
        reason="Commit de bot con rastro de Claude."
      fi
      [[ -z "$reason" ]] && reason="$(subject_reason "$(head -n1 <<<"$msg")" bot)" ;;
    'claude[bot]')
      cat="$(category_of_paths <<<"$paths")"
      if [[ "$cat" != assisted ]]; then reason="claude[bot] solo puede tocar la zona asistida (zona: $cat)."
      else reason="$(evaluate "$cat" "$msg" "$paths")" || true; fi
      [[ -z "$reason" ]] && reason="$(subject_reason "$(head -n1 <<<"$msg")" "$smode")" ;;
    *'[bot]')
      cat=bot; reason="Bot no autorizado: $author." ;;
    *)
      cat="$(category_of_paths <<<"$paths")"
      reason="$(subject_reason "$(head -n1 <<<"$msg")" "$smode")"
      if [[ -z "$reason" ]]; then reason="$(evaluate "$cat" "$msg" "$paths")" || true; fi ;;
  esac
  printf '%s\t%s\n' "$cat" "$(tr '\n' ' ' <<<"$reason" | sed 's/ *$//')"
}

cmd_check_range() {
  local base="${1:?falta base (sha, ROOT o remotes=NOMBRE)}" head="${2:?falta head}"
  local smode="${FLOORDAY_SUBJECT_MODE:-ci}" summary="${GITHUB_STEP_SUMMARY:-/dev/null}"
  local sha fail=0 checked=0 zones="" line cat reason subject merges
  local -a revs
  case "$base" in
    ROOT) revs=("$head") ;;
    remotes=*) revs=("$head" --not "--$base") ;;
    *) revs=("$base..$head") ;;
  esac
  printf '### Política de autoría\n\n| Commit | Zona | Resultado |\n|---|---|---|\n' >>"$summary"
  merges="$(git rev-list --merges "${revs[@]}")"
  if [[ -n "$merges" ]]; then
    fail=1
    reason="Hay commits de merge ($(tr '\n' ' ' <<<"$merges" | cut -c1-80)). La historia es lineal: git fetch && git rebase origin/main"
    if [[ -n "${GITHUB_ACTIONS:-}" ]]; then printf '::error title=Política de autoría::%s\n' "$reason"; else printf 'FALLA %s\n\n' "$reason" >&2; fi
  fi
  while IFS= read -r sha; do
    [[ -z "$sha" ]] && continue
    line="$(check_one "$sha" "$smode")"
    cat="${line%%$'\t'*}"; reason="${line#*$'\t'}"
    subject="$(git log -1 --format=%s "$sha")"
    checked=$((checked + 1))
    [[ ",$zones," == *",$cat,"* ]] || zones="${zones:+$zones,}$cat"
    if [[ -z "$reason" ]]; then
      printf '| `%s` %s | %s | ok |\n' "${sha:0:8}" "${subject//|/\\|}" "$cat" >>"$summary"
    else
      fail=1
      printf '| `%s` %s | %s | **falla** |\n' "${sha:0:8}" "${subject//|/\\|}" "$cat" >>"$summary"
      if [[ -n "${GITHUB_ACTIONS:-}" ]]; then
        printf '::error title=Política de autoría (%s)::%s\n' "${sha:0:8}" "$reason"
      else
        printf 'FALLA %s %s\n%s\n\n' "${sha:0:8}" "$subject" "$reason" >&2
      fi
    fi
  done < <(git rev-list --no-merges --reverse "${revs[@]}")
  [[ -n "${GITHUB_OUTPUT:-}" ]] && echo "zones=$zones" >>"$GITHUB_OUTPUT"
  printf 'Commits revisados: %d\n' "$checked"
  return "$fail"
}

cmd_metrics() {
  local range="${1:-HEAD}" sha total=0 assisted=0 human=0 release=0 bots=0 a_lines=0 h_lines=0 lines paths cat author
  while IFS= read -r sha; do
    [[ -z "$sha" ]] && continue
    author="$(git log -1 --format=%an "$sha")"
    if [[ "$author" == *'[bot]' && "$author" != 'claude[bot]' ]]; then bots=$((bots + 1)); continue; fi
    paths="$(git diff-tree --root --no-commit-id --name-only -r --no-renames "$sha")"
    cat="$(category_of_paths <<<"$paths")"
    lines="$(git show --numstat --format= "$sha" | awk '{ if ($1 != "-") s += $1 + $2 } END { print s + 0 }')"
    total=$((total + 1))
    case "$cat" in
      assisted) assisted=$((assisted + 1)); a_lines=$((a_lines + lines)) ;;
      human) human=$((human + 1)); h_lines=$((h_lines + lines)) ;;
      release) release=$((release + 1)) ;;
    esac
  done < <(git rev-list --no-merges "$range")
  cat <<EOF
| Métrica | Valor |
|---|---|
| Commits (sin bots) | $total |
| Zona asistida (código con Claude, con trailer) | $assisted commits, $a_lines líneas |
| Zona humana (reglas de IA y CI/CD, sin trailer) | $human commits, $h_lines líneas |
| Releases (release-please) | $release |
| Commits de bots | $bots |

El reparto describe el proceso, no la calidad: por diseño todo el código lleva trailer.
EOF
}

main() {
  local sub="${1:-}"; shift || true
  case "$sub" in
    classify) cmd_classify "$@" ;;
    category) category_of_paths ;;
    check-subject) [[ -z "$(subject_reason "${1:-}" local)" ]] ;;
    check-msg) cmd_check_msg "$@" ;;
    check-range) cmd_check_range "$@" ;;
    metrics) cmd_metrics "$@" ;;
    *) die "subcomando desconocido: '$sub' (classify | category | check-subject | check-msg | check-range | metrics)" ;;
  esac
}

if [[ "${BASH_SOURCE[0]}" == "$0" ]]; then main "$@"; fi
