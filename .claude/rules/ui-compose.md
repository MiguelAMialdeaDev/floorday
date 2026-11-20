---
paths:
  - "feature/**/*.kt"
  - "core/designsystem/**/*.kt"
  - "**/composeResources/**"
---

# UI (Compose Multiplatform)

- Patrón: `XRoute` (Koin, `collectAsStateWithLifecycle()` y efectos) y `XScreen(state, onIntent)` sin estado. Usa `/pantalla-mvi`.
- Textos: `stringResource(Res.string.x)` y `pluralStringResource`. Claves en `values` (EN) y `values-es` (ES) a la vez; el hook avisa si falta una.
- Componentes del design system (`CheckRow`, `ProgressRing`, `StreakBadge`, `MonthCalendar`, `WeightChart`) antes que crear otros. Colores, tipografía y espaciado solo desde los tokens del tema.
- Accesibilidad obligatoria: área táctil de 48 dp (`minimumInteractiveComponentSize()`), `toggleable(role = Role.Checkbox)` con `stateDescription` en los checks, semántica fusionada en filas, `progressBarRangeInfo` en anillos, un nodo semántico por día en el calendario, estado nunca solo por color y respeto a la reducción de animaciones.
- Fuente al 200 % y pantalla grande no rompen el layout: nada de alturas fijas para texto.
- Previews con `@Preview` en `preview/` y datos de `:core:testing`. Sin lógica en los composables.
- Vocabulario: "recordatorio", "toma", "cantidad". Nunca "tratamiento", "terapia" ni "dosis recomendada".
