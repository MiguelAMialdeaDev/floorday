---
paths:
  - ".github/**"
  - ".githooks/**"
  - "fastlane/**"
  - "Gemfile"
  - "Gemfile.lock"
  - "release-please-config.json"
  - ".gitattributes"
  - ".mcp.json"
---

# CI/CD (zona humana)

Esta zona la escribe y la commitea Miguel sin coautoría de IA (`docs/ai/politica-autoria.md`). No puedes editarla (deny en `.claude/settings.json`): propón el diff en el chat con su motivo y él lo aplica.

- Acciones de terceros fijadas por SHA completo con el tag en comentario (`uses: actions/checkout@<sha> # v7.0.1`). Renovate las mantiene al día.
- `permissions:` mínimos por workflow (por defecto `contents: read`). Nunca `pull_request_target` con checkout del código del PR.
- Secretos solo en `main.yml`, `rollout.yml` e `ios.yml`, en environments con revisor, y solo en el paso que los usa. Nunca se imprimen ni se escriben en artefactos.
- `concurrency` con `cancel-in-progress` en PRs. Caché de Gradle con `gradle/actions/setup-gradle`.
- Checks obligatorios en la protección de `main`: `pr-gate`, `pr-meta` y `commit-policy`. Merge solo por rebase.
- fastlane `supply`: metadatos en `fastlane/metadata/android/{es-ES,en-US}`. La primera subida del AAB es manual.
- Los scripts de `.githooks/` deben funcionar en bash 3.2 (macOS), bash 5 (Linux) y Git Bash (Windows), con finales de línea LF.
