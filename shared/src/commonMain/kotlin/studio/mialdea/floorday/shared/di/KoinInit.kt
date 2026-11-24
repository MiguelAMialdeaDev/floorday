package studio.mialdea.floorday.shared.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import studio.mialdea.floorday.feature.history.di.historyFeatureModule
import studio.mialdea.floorday.feature.today.di.todayFeatureModule

/** Common modules. dataModule(platform) arrives through platformModules because it needs the platform. */
internal val sharedModules: List<Module> = listOf(timeModule, todayFeatureModule, historyFeatureModule)

/**
 * Called once: from FloordayApplication on Android and from the iosApp in v2.
 * [platformModules] are each platform's modules (for example, androidPlatformModule).
 */
fun initKoin(
    platformModules: List<Module> = emptyList(),
    appDeclaration: KoinAppDeclaration = {},
): KoinApplication = startKoin {
    appDeclaration()
    modules(platformModules + sharedModules)
}
