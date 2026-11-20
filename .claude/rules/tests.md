---
paths:
  - "**/src/commonTest/**"
  - "**/src/androidHostTest/**"
  - "**/src/androidDeviceTest/**"
  - "**/src/test/**"
  - "**/src/androidTest/**"
  - "core/testing/**"
---

# Tests

- `kotlin.test`, `kotlinx-coroutines-test` (`runTest`) y Turbine para Flows. Nombres con backticks que describen el comportamiento, por ejemplo `` `posponer reinicia la cadena de reintentos` ``.
- Tiempo controlado: `FakeClock` y `TimeZone.of("Europe/Madrid")` explícita. Casos obligatorios donde aplique: 23:59 y 00:01, inicio del día lógico, DST (28-mar-2027 y 31-oct-2027) y viaje a `America/New_York`.
- Fakes de `:core:testing` antes que mocks. Nunca mocks en `commonTest`.
- Un test, un comportamiento. Estructura dado, cuando, entonces separada por líneas en blanco.
- Room: DAOs y migraciones en `androidHostTest` con BD en memoria. Receivers y `AlarmManager` con Robolectric (`ShadowAlarmManager`).
- Screenshots con Roborazzi: las referencias se regeneran con `recordRoborazzi` y se revisan en el PR; nunca se sobrescriben a mano.
- Un test no se borra ni se marca `@Ignore` para que el CI pase. Si un test está mal, se explica en el PR.
- Cobertura: `:core:domain` por encima del 80 % (`koverVerify` falla por debajo).
