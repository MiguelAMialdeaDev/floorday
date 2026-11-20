---
name: cumplimiento-play
description: Audita un cambio o una versión de Floorday contra la política de Google Play y las decisiones de cumplimiento del proyecto (permisos, alarmas exactas, notificaciones, Health Connect, Seguridad de los datos, afirmaciones médicas, ficha y capturas). Úsala antes de abrir un PR que añada permisos, dependencias, datos tratados o textos de ficha, y antes de cada release.
argument-hint: "[rango git o PR, por defecto main...HEAD]"
context: fork
agent: auditor-play
---

# Auditoría de cumplimiento en Play

Rango a auditar: `$ARGUMENTS` (si está vacío, `main...HEAD`; si es un número, el PR con `gh pr diff <n>`).

Ficheros cambiados:
!`git diff --name-only main...HEAD`

Cambios en manifest, dependencias y textos:
!`git diff main...HEAD -- '*AndroidManifest.xml' gradle/libs.versions.toml '*strings.xml' fastlane/metadata`

## Qué revisar, en este orden

1. **Permisos** (manifest fuente y, si puedes, el fusionado de `build/intermediates/merged_manifests`):
   - Lista permitida: `POST_NOTIFICATIONS`, `SCHEDULE_EXACT_ALARM`, `RECEIVE_BOOT_COMPLETED`, `VIBRATE`; fusionados `WAKE_LOCK`, `ACCESS_NETWORK_STATE`, `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`; desde v1.3, `READ_STEPS`, `READ_WEIGHT`, `WRITE_WEIGHT` de Health Connect.
   - Prohibidos: `USE_EXACT_ALARM`, `USE_FULL_SCREEN_INTENT`, `FOREGROUND_SERVICE*`, `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`, `INTERNET`, `SYSTEM_ALERT_WINDOW`.
2. **Dependencias nuevas**: ¿envían datos, abren red, añaden permisos o receivers exportados? Cualquier "sí" cambia Seguridad de los datos.
3. **Seguridad de los datos**: la declaración v1 es "no se recogen ni comparten datos". ¿Sigue siendo cierto? Export por SAF `CreateDocument`, nunca `ACTION_SEND`.
4. **No es dispositivo médico** (ADR-0007): sin cálculo de dosis, recomendaciones, alertas clínicas ni base de fármacos.
5. **Vocabulario** en strings y ficha: busca "tratamiento", "terapia", "dosis recomendada", "treatment", "therapy", "recommended dose".
6. **Disclaimer** presente en onboarding, editor de medicación, Ajustes y descripción de Play, con el texto exacto de `docs/adr/0007-*`.
7. **Notificaciones no abusivas**: topes de reintentos y posposiciones, corte a +2 h, un aviso de hábitos al día.
8. **Health Connect** (si aplica): solo los 3 permisos, `PermissionsRationaleActivity`, alias `VIEW_PERMISSION_USAGE`, `getSdkStatus`, declaración enviada antes de subir.
9. **Ficha y capturas** (si cambian): sin nombres de fármacos reales, enfermedades, marcas de terceros ni logos de Google como propios. Datos ficticios.
10. **targetSdk** y páginas de 16 KB: sin cambios no revisados.

## Formato del informe

```
## Veredicto: APTO | APTO CON CAMBIOS | NO APTO

| # | Área | Hallazgo | Severidad (bloqueante/alta/media/baja) | Evidencia (fichero:línea) | Acción |
|---|------|----------|-----------|-----------|--------|

### Formularios de Play Console que hay que actualizar
- (ninguno) o lista concreta

### Lo que NO pude verificar y por qué
```

No inventes políticas: si una conclusión depende de la redacción actual de una política, cita la página del Help Center consultada o márcala como "a verificar por Miguel".
