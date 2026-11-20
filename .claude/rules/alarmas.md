---
paths:
  - "**/reminders/**"
  - "**/*Reminder*.kt"
  - "**/*Alarm*.kt"
  - "**/*Dose*.kt"
  - "**/*Reschedule*.kt"
  - "**/*Watchdog*.kt"
  - "**/*Nudge*.kt"
  - "docs/alarm-reliability.md"
---

# Alarmas de medicación (ADR-0005)

Es la parte de la app donde un fallo hace daño real: una toma que no suena. Usa la skill `/cambio-alarmas`.

- Room (`scheduled_alarm`) es la fuente de verdad. `ReminderSync` es el **único** que llama a `ReminderScheduler`; idempotente y en transacción. Ningún receiver, ViewModel ni worker programa alarmas por su cuenta.
- Primer aviso: `setAlarmClock` si `canScheduleExactAlarms()`. Reintentos, posponer y prueba: `setExactAndAllowWhileIdle` con intervalo mínimo de 10 min. Sin permiso: `setAndAllowWhileIdle` y capacidad INEXACT con banner.
- Canal `dose_alarm`: `IMPORTANCE_HIGH`, `USAGE_ALARM`, `CATEGORY_REMINDER`, `FLAG_INSISTENT`, `setTimeoutAfter(2 h)`, `VISIBILITY_PRIVATE` con versión pública genérica. No se cambia a `CATEGORY_ALARM`.
- Topes: 3 reintentos, 3 posposiciones y corte a +2 h del aviso original. Después, notificación silenciosa en `dose_missed`.
- Reprogramar en `BOOT_COMPLETED`, `MY_PACKAGE_REPLACED`, `TIME_SET`, `TIMEZONE_CHANGED` y `ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED`, al arrancar y en cada `ON_RESUME` (revocar el permiso no emite broadcast).
- `RescheduleReceiver` empieza deshabilitado; se habilita con el primer recordatorio y se deshabilita cuando no queda ninguno.
- Prohibido: `USE_EXACT_ALARM`, full screen intent, foreground service, `setExpedited`, `setForeground`.
- Todo cambio lleva test de `ReminderPlanner` (DST, cambio de zona, regla de las 12 h) y test Robolectric con `ShadowAlarmManager`, y actualiza la matriz manual de `docs/alarm-reliability.md` si cambia el comportamiento.
