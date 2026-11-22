# ADR-0015: Visual identity "Suelo Quieto"

Date: 2026-11-22 · Status: Accepted

## Context

Floorday promises "your floor, not your ceiling". Habit apps tend to use loud greens, flames and a red cross for a missed day, and default Material 3 (purple, tonal shadows, ripples) reads as a template. The app is used every day, often half awake, partly by people with low vision, and much of it is about medication, so it has to be calm, legible and certain. Before implementing #12 we compared the spec's identity against Streaks, Gentler Streak, Finch, Habitify, Medisafe, Apple's rings, Things 3 and Apple Journal, and three competing directions were judged on beauty, fit, accessibility, feasibility in CMP 1.12 and light/dark coherence.

## Decision

1. **Warm paper, one green ink, one signature object.** Surfaces are Hueso #F7F3EC (light) and Noche #14130F (dark). Pino #1F4A3D is the only ink for done and the minimum. Barro #8E4A2B is the streak and Pizarra #2F5470 the freeze (both from S4).
2. **Strict meaning of colour.** Pino is done or the minimum, Barro the streak, Pizarra a freeze, a neutral outline is pending or missed. Red is only for form validation: never for a day or a dose. No colour per habit.
3. **The ring rises from the floor.** Two arms grow from 6 o'clock like a level, with a tick per required slot still to do and a floor line underneath. When they meet, the interior fills, a hand-drawn scalloped seal sets at 12 o'clock and the caption says "Minimum met". That is the only bounce in the app.
4. **Two OFL typefaces with a strict boundary.** Atkinson Hyperlegible Next (4 static weights) for everything below 28sp, including medication names and times. Fraunces 72pt Soft SemiBold only for display, headline and the ring numeral, shown as "2 / of 4" (no slash next to a slashed zero). Fraunces has no tabular figures, so changing numbers sit in fixed slots.
5. **No shadows, no dividers, no ripple.** Depth comes from the tonal staircase. Rows are 16 dp tiles that scale to 0.98 with their own press layer. Radii 4 to 24 dp; pills only for chips.
6. **State by shape and words, never colour alone.** Pending is an empty squircle, done a circle with a check that draws on, skipped a dash, plus `stateDescription`. A floor glyph marks slots that are part of the minimum. Done keeps its ink (no strikethrough) and says "Done 08:42".
7. **CheckRow uses `combinedClickable(role = Role.Checkbox)`**, not `toggleable`, because Today needs the long press to skip a slot. It adds `toggleableState`, `stateDescription`, click labels and custom actions.
8. **Calm motion and haptics.** `MotionScheme.standard()` so stock components do not bounce; springs without overshoot except the seal. Haptics only on commit (toggle on and off, long press, and a confirm once per day when the minimum is met), never the reject pattern. With reduced motion everything snaps to a complete static state.
9. **No dynamic colour.** Hand-made `ImageVector` icons; no material-icons.
10. **Contrast is a test.** `ContrastTest` checks every pair the components draw: 4.5:1 for text and 3:1 for non-text, in light and dark.

## Alternatives considered

- **Keep the spec palette as is.** Pino600 and Pino700 were 1.13:1 apart and the arc read as the same dark mass as the numeral; the ramps were rebuilt keeping the brand tones.
- **Material 3 Expressive** (expressive motion, lime green, shape morphing API). Lovely glyph ideas, but stock components bounce, the lime goes muddy in dark mode and its missed-day dot failed contrast. We kept only the squircle to circle morph and the scalloped seal, drawn by hand.
- **Dynamic colour.** It erases the identity; maybe optional in v1.x.
- **One typeface only.** Cheaper, but the hero numeral is the emotional centre of the app; Fraunces is the emergency cut if it ever causes trouble.
- **Confetti, sounds or a strikethrough on done.** They celebrate the ceiling, not the floor.

## Consequences

- Changing a token means passing `ContrastTest`. Decorative pairs (ring track, row tile, met fill, outlineVariant) are excluded on purpose and listed in the test.
- About 530 KB of fonts in the app (Atkinson 4 x 66 KB, Fraunces 263 KB).
- Today (#13) passes `stateText` to the ring and fires the once-per-day confirm haptic from `onMinimumMet`.
- Settings will force light or dark through the `darkTheme` parameter of `FloordayTheme`.
- Reduced motion is read once per composition; a live change of the setting applies on the next launch (S8 may add a reactive reader).

## Sources

- https://github.com/googlefonts/atkinson-hyperlegible-next (commit 7925f50f)
- https://github.com/undercasetype/Fraunces (commit 7ccdec31)
- https://www.w3.org/WAI/WCAG22/Understanding/contrast-minimum.html
- https://www.w3.org/WAI/WCAG22/Understanding/non-text-contrast.html
- https://kotlinlang.org/docs/multiplatform/compose-multiplatform-resources-usage.html
- https://kotlinlang.org/docs/multiplatform/compose-previews.html
