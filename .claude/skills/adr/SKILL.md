---
name: adr
description: Redacta un Architecture Decision Record de Floorday en docs/adr con el formato del proyecto (contexto, decisión, alternativas, consecuencias, verificación). Úsala cuando se toma o se cambia una decisión de arquitectura, de dependencias, de política de Play o de proceso, o cuando un plan introduce un expect/actual, un módulo o una versión alpha.
argument-hint: "<título de la decisión>"
---

# ADR: $ARGUMENTS

ADRs existentes:
!`ls docs/adr/`

## Reglas

- La decisión la toma Miguel. Tú redactas, presentas alternativas con honestidad y señalas lo que no sabes.
- Un ADR aceptado no se edita: se crea uno nuevo con `Sustituye a ADR-XXXX` y se marca el antiguo como `Sustituido por ADR-YYYY` (único cambio permitido).
- Unas 10 a 30 líneas. Si necesita más, primero un `/design-doc`.
- Toda afirmación sobre una API, versión o política lleva enlace a la fuente oficial consultada.

## Plantilla (`docs/adr/NNNN-titulo-en-kebab-case.md`, siguiente número libre)

```markdown
# NNNN. <Título en forma de decisión>

- Estado: Propuesto | Aceptado | Sustituido por ADR-YYYY
- Fecha: AAAA-MM-DD
- Decisor: Miguel
- Redacción asistida por: Claude (<modelo>)

## Contexto
Qué problema hay y qué fuerzas empujan (plazo, Play, iOS en marzo, horas disponibles).

## Decisión
Qué se hace, en una o dos frases en presente.

## Alternativas consideradas
| Opción | A favor | En contra | Por qué no |
|---|---|---|---|

## Consecuencias
Lo que se gana, lo que se pierde y lo que ahora hay que vigilar.

## Verificación
Cómo se sabrá que fue buena decisión y cuándo se revisa (fecha o hito).

## Fuentes
- <enlace oficial>
```

## Pasos

1. Pregunta a Miguel lo que falte para el contexto (no lo inventes).
2. Escribe el ADR con estado `Propuesto`.
3. Añade la línea al índice `docs/adr/README.md`.
4. Miguel lo pasa a `Aceptado`. Commit con `/commit`: `docs(adr): NNNN <título>`.
