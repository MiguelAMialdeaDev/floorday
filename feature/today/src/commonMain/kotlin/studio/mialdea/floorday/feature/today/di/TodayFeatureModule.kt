package studio.mialdea.floorday.feature.today.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import studio.mialdea.floorday.core.domain.usecase.ObserveTodayUseCase
import studio.mialdea.floorday.core.domain.usecase.RestoreCompletionUseCase
import studio.mialdea.floorday.core.domain.usecase.ToggleSlotUseCase
import studio.mialdea.floorday.feature.today.TodayViewModel

val todayFeatureModule = module {
    factoryOf(::ObserveTodayUseCase)
    factoryOf(::ToggleSlotUseCase)
    factoryOf(::RestoreCompletionUseCase)
    viewModelOf(::TodayViewModel)
}
