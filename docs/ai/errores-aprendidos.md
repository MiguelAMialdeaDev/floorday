# Errores aprendidos

Registro vivo de errores de la IA (y míos al dirigirla) que podían repetirse, con la barrera que se añadió. La regla: **cada entrada termina en un cambio verificable** (test, hook, regla, permiso, check de CI o paso de una skill). Una entrada sin barrera es solo una anécdota.

Claude propone la entrada cuando yo corrijo algo; yo la reviso y la commiteo (zona humana).

## Formato

```markdown
### AAAA-MM-DD · <título corto>
- **Qué pasó:** hechos, con enlace al PR o commit.
- **Por qué:** causa raíz, no el síntoma.
- **Barrera añadida:** fichero y tipo (hook, regla, test, skill, CI).
- **Cómo sé que funciona:** el test o la prueba que lo demuestra.
```

## Riesgos conocidos de antemano

Antes de escribir código ya sé que un modelo tiende a estos fallos en este tipo de proyecto. Cada uno tiene su barrera desde el primer día:

| Tendencia | Ejemplo en Floorday | Barrera |
|---|---|---|
| Usar la API de plataforma más conocida | `java.time` o `android.util.Log` en `commonMain` | Hook `guard-edits.sh` + detekt `ForbiddenImport` |
| Leer la hora del sistema "porque es más corto" | `Clock.System.now()` en un caso de uso | Hook `guard-edits.sh` + regla `common-main.md` |
| Elegir el permiso que "funciona siempre" | `USE_EXACT_ALARM` o full screen intent | Hook del manifest + `verifyMergedManifest` en CI |
| Inventar versiones o APIs | una versión de Room 3 que no existe | Regla de fuentes oficiales + permisos de `WebFetch` solo a dominios oficiales + catálogo con `ask` |
| Arreglar el síntoma tocando el generado | editar el JSON del esquema de Room | Hook `guard-edits.sh` + `deny` en permisos |
| Hacer pasar el CI relajando la regla | `@Ignore`, `@Suppress` o bajar el umbral de Kover | Regla `tests.md` + revisión de `revisor-kmp` |
| Ampliar el alcance "ya que estaba" | refactor no pedido dentro de un PR de feature | `CLAUDE.md` + revisión humana |
| Decir "hecho" sin ejecutar nada | resumen optimista sin tests | Hook `Stop` con huella de verificación |
| Textos de UI en duro | `Text("Hoy")` | Hook `post-edit.sh` + regla `ui-compose.md` |

## Registro

*(Primera entrada cuando ocurra. Fecha de inicio del registro: 2026-11-20.)*
