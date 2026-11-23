package studio.mialdea.floorday.di

import kotlinx.coroutines.CoroutineScope
import org.koin.core.Koin
import org.koin.core.module.Module

/** Release variant of the startup hooks: nothing. The debug one seeds the database. */
internal object VariantStartup {

    val modules: List<Module> = emptyList()

    @Suppress("UnusedParameter")
    fun launch(koin: Koin, scope: CoroutineScope) = Unit
}
