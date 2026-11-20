#!/usr/bin/env bash
# PreToolUse del subagente escritor-tests: solo puede escribir tests y :core:testing.
set -uo pipefail
source "$(dirname "${BASH_SOURCE[0]}")/lib.sh"
require_json_or_block

file="$(json_get tool_input.file_path)"
rel="$(rel_path "$file")"
case "$rel" in
  */src/commonTest/*|*/src/androidHostTest/*|*/src/androidDeviceTest/*|*/src/test/*|*/src/androidTest/*|core/testing/*)
    exit 0 ;;
esac
block "escritor-tests solo edita tests y core/testing. '$rel' es código de producción:" \
      "describe el cambio necesario en tu informe y lo aplica la sesión principal."
