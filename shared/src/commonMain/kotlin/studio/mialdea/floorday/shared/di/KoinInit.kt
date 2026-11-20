package studio.mialdea.floorday.shared.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration

/** Módulos comunes. En S0 está vacío; los PR de datos y de Hoy añaden los suyos. */
internal val sharedModules: List<Module> = emptyList()

/**
 * Se llama una vez: desde FloordayApplication en Android y desde el iosApp en v2.
 * [platformModules] son los módulos de cada plataforma (por ejemplo, androidPlatformModule).
 */
fun initKoin(
    platformModules: List<Module> = emptyList(),
    appDeclaration: KoinAppDeclaration = {},
): KoinApplication = startKoin {
    appDeclaration()
    modules(platformModules + sharedModules)
}
