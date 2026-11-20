#!/usr/bin/env bash
# PreToolUse (Edit|Write): reglas duras que se comprueban ANTES de escribir.
# Exit 2 = la edición no se hace y Claude recibe el motivo por stderr.
set -uo pipefail
source "$(dirname "${BASH_SOURCE[0]}")/lib.sh"
require_json_or_block

file="$(json_get tool_input.file_path)"
[[ -z "$file" ]] && exit 0
rel="$(rel_path "$file")"
text="$(new_text)"

# 1. Ficheros generados, binarios o secretos: nunca a mano.
case "$rel" in
  */schemas/*.json)
    block "BLOQUEADO: $rel es un esquema de Room exportado por KSP." \
          "Cambia la entidad, sube la versión de la base de datos, añade la migración y compila." \
          "Usa la skill /migracion-room." ;;
  build/*|*/build/*|*/generated/*|.gradle/*|*/.gradle/*)
    block "BLOQUEADO: $rel es salida de compilación. Cambia la fuente, no el resultado." ;;
  *.jks|*.keystore|keystore/*|*/keystore/*|local.properties|*.p12|*service-account*.json)
    block "BLOQUEADO: $rel contiene secretos o configuración local. Solo Miguel lo toca, fuera del repo." ;;
  gradle/wrapper/gradle-wrapper.jar|gradlew|gradlew.bat)
    block "BLOQUEADO: el wrapper se actualiza con ./gradlew wrapper --gradle-version X en un PR build: propio." ;;
esac

# 2. Manifest: permisos y componentes prohibidos (ADR-0005 y ADR-0011).
if [[ "$rel" == *AndroidManifest.xml ]]; then
  # Un elemento XML por línea para no depender de cómo esté partido el texto.
  flat="$(tr '\n\r' '  ' <<<"$text")"
  elements="${flat//</$'\n'<}"
  forbidden='android\.permission\.(USE_EXACT_ALARM|USE_FULL_SCREEN_INTENT|FOREGROUND_SERVICE[A-Z_]*|REQUEST_IGNORE_BATTERY_OPTIMIZATIONS|INTERNET|SYSTEM_ALERT_WINDOW)"'
  bad="$(grep -E '^<uses-permission' <<<"$elements" | grep -E "$forbidden" | grep -v 'tools:node="remove"' || true)"
  if [[ -n "$bad" ]]; then
    block "BLOQUEADO: permiso prohibido en el manifest:" "$bad" \
          "Floorday no declara USE_EXACT_ALARM, USE_FULL_SCREEN_INTENT, FOREGROUND_SERVICE*, REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, INTERNET ni SYSTEM_ALERT_WINDOW." \
          "Solo se admiten con tools:node=\"remove\". Cambiar esto exige ADR nuevo y permiso explícito de Miguel."
  fi
  fgs="$(grep -E '^<service' <<<"$elements" | grep -E 'foregroundServiceType' | grep -v 'tools:node="remove"' || true)"
  if [[ -n "$fgs" ]]; then
    block "BLOQUEADO: Floorday no usa foreground services (ADR-0005)." "$fgs"
  fi
fi

# 3. Kotlin.
if [[ "$rel" == *.kt ]]; then
  if [[ "$rel" == */src/commonMain/* || "$rel" == */src/commonTest/* ]]; then
    jvm="$(grep -nE '^[[:space:]]*import[[:space:]]+(android|androidx\.core|java|javax)\.' <<<"$text" || true)"
    [[ -n "$jvm" ]] && block "BLOQUEADO: import de plataforma en $rel (commonMain/commonTest debe compilar para iOS):" "$jvm" \
      "Define una interfaz en :core:domain/platform e implementa en :androidApp, o usa expect/actual solo si ya está aprobado (databaseBuilder, dataStorePath, DateFormatter, is24HourFormat)."
    if [[ "$rel" != */di/* ]]; then
      clk="$(grep -nE 'Clock\.System|TimeZone\.currentSystemDefault\(' <<<"$text" || true)"
      [[ -n "$clk" ]] && block "BLOQUEADO: reloj o zona del sistema en $rel:" "$clk" \
        "Clock y TimeZone se inyectan por constructor (Koin los provee en di/). En tests usa FakeClock de :core:testing."
    fi
  fi
  fg="$(grep -nE '\.(setExpedited|setForeground|setForegroundAsync)\(' <<<"$text" || true)"
  [[ -n "$fg" ]] && block "BLOQUEADO: WorkManager expedited/foreground en $rel:" "$fg" \
    "Se eliminó SystemForegroundService del manifest. Usa trabajo normal o AlarmManager (skill /cambio-alarmas)."
fi

exit 0
