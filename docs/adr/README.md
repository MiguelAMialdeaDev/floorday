# Architecture Decision Records

Each ADR records one decision that is expensive to undo. It is written on the day the decision is taken and is never rewritten: if the decision changes, a new ADR supersedes it and the old one is marked "Superseded by ADR-XXXX".

| No. | Title | Status |
|---|---|---|
| 0000 | Product name and application id | In progress (closes with the 5-person name test) |
| [0001](0001-architecture.md) | Architecture: KMP, Compose Multiplatform and MVI | Accepted |
| 0002 | Room 3 over SQLDelight (detail and plan B) | Planned |
| 0003 | MVI with StateFlow | Merged into 0001 (number not reused) |
| 0004 | Koin now, Metro later | Planned |
| 0005 | Alarms: SCHEDULE_EXACT_ALARM and setAlarmClock, no FGS or FSI | Planned |
| 0006 | Forgiving streaks, edit window and closing at D+3 | Planned |
| 0007 | Not a medical device | Planned |
| 0008 | Time model | Planned |
| 0009 | Navigation 3 and its plan B | Planned |
| 0010 | Widget data and database in the App Group | Planned |
| 0011 | Offline first, no INTERNET permission | Planned |
| 0012 | License | Planned |
| 0013 | minSdk 28 | Planned |
| 0014 | Backup and restore | Planned |
| 0015 | Visual identity | Planned (S1) |

## Template

```markdown
# ADR-XXXX: Title

Date: YYYY-MM-DD · Status: Proposed | Accepted | Superseded by ADR-YYYY

## Context
## Decision
## Alternatives considered
## Consequences
## Sources
```
