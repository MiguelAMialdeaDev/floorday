---
name: pantalla-mvi
description: Implementa una pantalla de Compose Multiplatform en Floorday con el patrón MVI del proyecto (State, Intent, Effect, Reducer puro, ViewModel sobre MviViewModel, Route y Screen sin estado), recursos ES y EN, accesibilidad y tests de reducer y ViewModel. Úsala para cualquier pantalla o diálogo nuevo dentro de un feature.
argument-hint: "<feature> <NombrePantalla> [#issue]"
---

# Pantalla MVI: `$1` en `:feature:$0`

## 0. Diseño antes de código

Escribe en el chat, y espera el visto bueno si la pantalla es nueva:
- **State**: campos y su valor inicial. Todo lo que pinta la pantalla sale de aquí.
- **Intents**: acciones del usuario y del sistema (`Load`, `ToggleSlot(id)`, `Undo`...).
- **Effects**: lo que ocurre una sola vez (`ShowUndoSnackbar`, `Navigate(key)`, `RequestNotificationPermission`).
- **Fuentes**: qué casos de uso o repositorios observa y con qué `Clock`/`TimeZone`.

## 1. Contrato (`ui/<pantalla>/<Pantalla>Contract.kt`)

```kotlin
@Immutable
data class $1State(
    val isLoading: Boolean = true,
    // ...
)

sealed interface $1Intent {
    data object Load : $1Intent
}

sealed interface $1Effect {
    data class Navigate(val key: NavKey) : $1Effect
}
```

## 2. Reducer puro (`<Pantalla>Reducer.kt`)

```kotlin
internal object $1Reducer {
    fun reduce(state: $1State, event: $1Event): $1State = when (event) {
        // sin IO, sin tiempo del sistema, sin efectos
    }
}
```

`$1Event` son los resultados internos (datos cargados, error). Los intents del usuario se traducen a eventos en el ViewModel.

## 3. ViewModel

```kotlin
internal class $1ViewModel(
    private val observeX: ObserveXUseCase,
    private val clock: Clock,
    private val timeZone: TimeZone,
) : MviViewModel<$1State, $1Intent, $1Effect>($1State()) {

    override suspend fun handle(intent: $1Intent) {
        when (intent) {
            $1Intent.Load -> observeX().collect { reduce { $1Reducer.reduce(this, $1Event.Loaded(it)) } }
        }
    }
}
```

Flujos largos con `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ...)` cuando se combinan fuentes.

## 4. UI

```kotlin
@Composable
fun $1Route(onNavigate: (NavKey) -> Unit, viewModel: $1ViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) { is $1Effect.Navigate -> onNavigate(effect.key) }
        }
    }
    $1Screen(state = state, onIntent = viewModel::onIntent)
}

@Composable
internal fun $1Screen(state: $1State, onIntent: ($1Intent) -> Unit, modifier: Modifier = Modifier) { /* sin estado */ }
```

## 5. Checklist de UI

- [ ] Todos los textos con `stringResource`, claves en `values` y `values-es` (y plurales donde haya números).
- [ ] Componentes y tokens del design system.
- [ ] 48 dp, semántica fusionada, `stateDescription`, nada que dependa solo del color.
- [ ] Previews en `preview/` con estados vacío, cargando, normal y error.
- [ ] Fuente al 200 % sin cortes (revisa en la preview con `fontScale = 2f`).

## 6. Tests (`commonTest`)

- `$1ReducerTest`: una prueba por evento.
- `$1ViewModelTest` con Turbine y fakes de `:core:testing`: estado inicial, intent principal, efecto de navegación.
- Si la pantalla tiene un flujo crítico (onboarding, Hoy), un `runComposeUiTest`.

## 7. Cierre

`bash .claude/hooks/verify.sh` y `/commit` (`feat(<feature>): add <pantalla> screen`).
