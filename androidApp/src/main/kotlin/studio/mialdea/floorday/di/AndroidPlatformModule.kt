package studio.mialdea.floorday.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import studio.mialdea.floorday.core.domain.platform.TimeOfDayFormatter
import studio.mialdea.floorday.platform.AndroidTimeOfDayFormatter

/** Android implementations of the :core:domain/platform interfaces. */
val androidPlatformModule = module {
    single<TimeOfDayFormatter> { AndroidTimeOfDayFormatter(androidContext()) }
}
