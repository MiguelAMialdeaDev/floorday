## Qué cambia y por qué

<!-- De 2 a 4 frases. Un solo cambio lógico. Enlaza issue, ADR o design doc. -->

Closes #

## Uso de IA en este PR

<!-- pr-meta exige los cinco campos rellenos. -->

- **Autoría:** <!-- asistida (commits con Co-Authored-By de Claude) / humana (configuración de IA o CI/CD, sin coautoría) / ambas en commits separados -->
- **Qué hizo Claude:** <!-- p. ej. primer borrador de ReminderPlanner y sus tests de DST -->
- **Qué decidí y revisé yo:** <!-- diseño, casos límite, líneas reescritas, tests añadidos a mano -->
- **Reglas y contexto aplicados:** <!-- CLAUDE.md, .claude/rules/..., skill o subagente usado -->
- **Verificación independiente:** <!-- cómo comprobé que es correcto más allá de que compile -->

## Cómo lo he probado

- [ ] Tests nuevos o actualizados (commonTest, Robolectric, Roborazzi o propiedades)
- [ ] Probado en dispositivo real (modelo y versión de Android):
- [ ] Si toca alarmas: reinicio, Doze forzado y cambio de zona; runbook y SLO revisados
- [ ] Capturas o vídeo si cambia la UI (claro y oscuro)

## Checklist del autor

- [ ] Menos de 400 líneas sin generados (ideal menos de 200); el título sigue Conventional Commits
- [ ] Sin `android.*` ni `java.*` en commonMain; `Clock` y `TimeZone` inyectados
- [ ] Accesibilidad: semántica, 48 dp, contraste, el estado no depende solo del color
- [ ] Strings en EN y ES; sin texto fijo en el código
- [ ] Dependencia nueva: licencia y "Seguridad de los datos" de Play revisadas
- [ ] API pública de `:core:*` cambiada: `updateKotlinAbi` ejecutado y justificado (desde S3)
- [ ] Esquema de Room cambiado: migración, test de migración y esquema exportado
- [ ] Manifest o permisos cambiados: lista blanca actualizada en un PR humano aparte
- [ ] Decisión difícil de revertir: ADR nuevo o actualizado

## Riesgos y plan de vuelta atrás

<!-- Qué puede romper y cómo se revierte. "Revert del PR" vale si es verdad. Los reverts se hacen en local con git revert. -->
