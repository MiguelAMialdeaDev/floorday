---
name: design-doc
description: Escribe un documento de diseño corto al estilo de las grandes empresas de producto (objetivos, no objetivos, diseño, alternativas, riesgos, plan de pruebas y de despliegue) antes de implementar una feature grande de Floorday, como widget, Health Connect, iOS, revisión semanal o pareja. Úsala cuando un issue supere 5 puntos o toque más de dos módulos.
argument-hint: "<feature> [número de issue]"
---

# Design doc: $ARGUMENTS

Se guarda en `docs/design/AAAA-MM-DD-<feature>.md`. Máximo unas 2 páginas: si sale más largo, el alcance es demasiado grande y se parte.

Contexto disponible:
- Issue: !`gh issue view $1 --json title,body --jq '.title + "\n\n" + .body' 2>/dev/null || echo "sin issue"`
- ADRs: !`ls docs/adr/`

## Plantilla

```markdown
# <Feature>: documento de diseño

- Autor: Miguel · Redacción asistida por Claude (<modelo>)
- Estado: Borrador | En revisión | Aprobado | Implementado
- Issue: #NN · Sprint: SN

## Resumen
Tres líneas: qué, para quién y por qué ahora.

## Objetivos
- Medibles. "El widget marca una toma en menos de 1 s sin abrir la app."

## No objetivos
- Lo que explícitamente no se hace en esta iteración.

## Contexto
Estado actual, restricciones (Play, offline, iOS en marzo, 10-15 h/semana).

## Diseño
- Módulos afectados y grafo de dependencias resultante.
- Modelo de datos (y migración si la hay).
- Interfaces nuevas en `:core:domain/platform`, y su implementación Android e iOS prevista.
- Flujo principal (secuencia en texto o mermaid).
- Estados de error y vacíos.

## Alternativas
| Opción | Coste | Riesgo | Por qué no |

## Privacidad, Play y accesibilidad
Permisos, Seguridad de los datos, textos, TalkBack.

## Plan de pruebas
Unitarias, Robolectric, screenshots, manuales (dispositivos concretos).

## Despliegue
Pista (internal, alpha, producción), fases y cómo se revierte.

## Riesgos y preguntas abiertas
| Riesgo | Probabilidad | Impacto | Mitigación |

## Estimación
Por PR, en horas, y en qué sprint cae cada uno.
```

## Pasos

1. Lee el issue, los ADRs relacionados y el código afectado antes de escribir.
2. Rellena la plantilla; marca con **[DECIDE MIGUEL]** cada punto que necesite decisión.
3. Si una decisión es de arquitectura duradera, propón también el ADR (`/adr`).
4. Commit con `/commit`: `docs(design): <feature>`.
