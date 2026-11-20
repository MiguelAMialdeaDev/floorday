---
name: nueva-feature-kmp
description: Crea un módulo de feature KMP nuevo (:feature:x) en Floorday con el plugin de convención, su módulo Koin, su clave de Navigation 3, recursos ES y EN, una pantalla MVI inicial y sus tests. Úsala cuando un issue pida una sección nueva de la app (por ejemplo review, couple, weight), no para añadir una pantalla a un feature existente.
argument-hint: "<nombre-feature> [#issue]"
---

# Nuevo feature KMP: `:feature:$0`

Antes de empezar, confirma con Miguel que esto merece un módulo nuevo (y no una pantalla en un feature existente). Un módulo nuevo es una decisión de arquitectura: si no hay ADR ni design doc que lo justifique, propón uno con `/adr`.

Módulos actuales: !`grep -E 'include\(' settings.gradle.kts`

## 1. Estructura

```
feature/$0/
  build.gradle.kts
  src/commonMain/kotlin/studio/mialdea/floorday/feature/$0/
    di/<Nombre>Module.kt
    navigation/<Nombre>Destination.kt
    ui/                      (pantallas: usa /pantalla-mvi)
  src/commonMain/composeResources/values/strings.xml       (EN)
  src/commonMain/composeResources/values-es/strings.xml    (ES)
  src/commonTest/kotlin/studio/mialdea/floorday/feature/$0/
```

## 2. `build.gradle.kts` (solo plugins y dependencias)

```kotlin
plugins {
    alias(libs.plugins.floorday.kmp.feature)
}

kotlin {
    androidLibrary {
        namespace = "studio.mialdea.floorday.feature.$0"
    }
    sourceSets {
        commonMain.dependencies {
            // core:domain, core:designsystem, Koin, Nav3 y lifecycle ya los aporta floorday.kmp.feature
        }
        commonTest.dependencies {
            implementation(projects.core.testing)
        }
    }
}

compose.resources {
    packageOfResClass = "studio.mialdea.floorday.feature.$0.resources"
}
```

Si falta algo en el plugin de convención, no lo añadas aquí: propón el cambio en `build-logic` (requiere permiso).

## 3. Navegación (Navigation 3)

```kotlin
@Serializable
data object <Nombre>Key : NavKey
```

La entrada del back stack y el `entry<...>` se registran en `:shared` (`FloordayApp`). El feature nunca importa otro feature: navega emitiendo un efecto `Navigate` que resuelve `:shared`.

## 4. Koin

```kotlin
val <nombre>Module = module {
    viewModelOf(::<Nombre>ViewModel)
}
```

Regístralo en `initKoin()` de `:shared`.

## 5. Registro y guardas

1. `include(":feature:$0")` en `settings.gradle.kts` (pide permiso: regla `ask`).
2. Dependencia en `:shared`.
3. `./gradlew checkModuleGraph` confirma que no hay feature dependiendo de feature.

## 6. Primera pantalla y tests

Usa `/pantalla-mvi` para la pantalla inicial. Al menos: test del reducer, test del ViewModel con Turbine y un string de prueba en ES y EN que se lea en runtime (protege contra `MissingResourceException` si `androidResources` no está activo).

## 7. Cierre

- `bash .claude/hooks/verify.sh --full` (cambia el grafo de módulos).
- Commits con `/commit`: `build(<feature>): add module` y `feat(<feature>): <pantalla inicial>` por separado.
- En el resumen: diagrama del grafo actualizado (texto) y lo que queda para Miguel (`settings.gradle.kts` si no se aprobó).
