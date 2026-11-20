---
paths:
  - "androidApp/**"
  - "**/src/androidMain/**"
---

# Código Android (:androidApp y androidMain)

- Aquí vive lo que necesita `Context`: receivers, `AlarmManager`, `NotificationManager`, canales, WorkManager, Glance, Health Connect, `Build.MANUFACTURER`.
- Cada implementación de plataforma implementa una interfaz de `:core:domain/platform` y se registra en `androidPlatformModule` (Koin). La UI común nunca la ve directamente.
- Receivers: `goAsync()` y corrutina con timeout de 8 s; el trabajo va dentro de `ReminderSync`, no como lógica suelta en `onReceive`.
- `PendingIntent` siempre con `FLAG_IMMUTABLE`. Componentes con `android:exported="false"` salvo los de la lista blanca del CI.
- Strings de notificación y widget en `androidApp/src/main/res/values` (EN) y `values-es` (ES).
- Manifest: cualquier cambio pide permiso (regla `ask`) y pasa por `verifyMergedManifest`. Si tocas recordatorios, aplica también `.claude/rules/alarmas.md`.
- Intents de fabricante (Xiaomi, Samsung y demás) siempre en `try/catch ActivityNotFoundException`, con caída a los ajustes de la app.
