---
name: cambio-alarmas
description: Guía obligatoria para cualquier cambio en los recordatorios y alarmas de medicación de Floorday (ReminderPlanner, ReminderSync, AndroidReminderScheduler, receivers, canales, watchdog, posponer, reprogramación). Incluye el diseño que no se puede romper, los tests mínimos y la checklist de reinicio, Doze, zona horaria, permisos y fabricantes.
argument-hint: "<qué cambia> [#issue]"
---

# Cambio en alarmas de medicación

Una alarma que no suena es el peor fallo posible de Floorday. Este flujo es más lento a propósito.

Documentos de referencia: `docs/adr/0005-*.md`, `docs/alarm-reliability.md` y la regla `.claude/rules/alarmas.md`.

## 1. Plan (siempre en modo plan, siempre con visto bueno de Miguel)

Responde por escrito:
1. ¿Qué comportamiento cambia, visto por el usuario?
2. ¿Qué estados de `scheduled_alarm` se crean, cambian o borran? Dibuja la transición.
3. ¿Sigue `ReminderSync` siendo el único que llama a `ReminderScheduler`?
4. ¿Es idempotente? Si se ejecuta dos veces seguidas, ¿queda una sola alarma por toma?
5. ¿Qué pasa si el proceso muere a mitad (receiver con `goAsync()` y timeout de 8 s)?
6. ¿Afecta a permisos o al manifest? Si sí, para: requiere ADR y permiso explícito.

## 2. Invariantes que no se rompen

- Primer aviso con `setAlarmClock`; reintentos y posponer con `setExactAndAllowWhileIdle` (mínimo 10 min); sin permiso, inexacta y capacidad INEXACT visible.
- 3 reintentos, 3 posposiciones, corte a +2 h. Después, `dose_missed` silenciosa.
- Descartar la notificación no confirma la toma.
- Pantalla de bloqueo privada por defecto.
- Sin `USE_EXACT_ALARM`, sin full screen intent, sin foreground service.

## 3. Tests obligatorios

Dominio (`commonTest`, `FakeClock`, `Europe/Madrid`):
- [ ] Próxima ocurrencia normal, en el hueco de DST (28-mar-2027 02:30 no existe) y en la hora repetida (31-oct-2027).
- [ ] Cambio de zona a `America/New_York` y regla de las 12 h.
- [ ] Momento ya hecho o saltado: no se programa.
- [ ] Posponer reinicia la cadena; la tercera posposición quita el botón; corte a +2 h.
- [ ] Idempotencia: `resyncAll()` dos veces deja el mismo conjunto de alarmas.

Android (`androidHostTest`, Robolectric, `ShadowAlarmManager`):
- [ ] `setAlarmClock` en el primer aviso, exacta no exenta en reintentos, inexacta sin permiso.
- [ ] `DoseActionReceiver`: Tomada cancela reintentos; Posponer deja una sola alarma activa.
- [ ] `RescheduleReceiver` reconstruye N alarmas y se habilita/deshabilita según haya recordatorios.

## 4. Checklist manual (para Miguel, en el PR)

Marca lo que aplica al cambio y deja el resto con "no aplica":

| Caso | Cómo | Esperado |
|---|---|---|
| Pantalla apagada 1 h | programar a +60 min y dejar el móvil | suena a su hora |
| Doze forzado | `adb shell dumpsys deviceidle force-idle` | suena (setAlarmClock sale de Doze) |
| Reinicio | programar, reiniciar, desbloquear | suena tras el primer desbloqueo |
| Cambio de hora y zona | ajustes del sistema | se reprograma bien |
| DST | emulador con fecha del cambio | sin duplicados ni huecos |
| Permiso de alarmas exactas revocado | ajustes de la app | banner INEXACT al volver; al conceder, se reprograma |
| App cerrada desde recientes | deslizar | suena |
| Forzar detención | ajustes | no suena (limitación de Android, documentada) |
| No molestar | activar de noche | según ajuste de recordatorios; ítem en Fiabilidad |
| Restauración de copia | reinstalar con backup | reprograma sin falsos "no saltó" |
| Xiaomi/HyperOS | autoarranque y batería sin restricciones | guía visible en Fiabilidad |
| Samsung | "Nunca suspender" | guía visible en Fiabilidad |

## 5. Cierre

- `bash .claude/hooks/verify.sh --full`.
- Actualiza `docs/alarm-reliability.md` si cambia el comportamiento.
- Pide la revisión con `/revision-pr` antes de abrir el PR.
- `/commit` con ámbito `reminders`.
