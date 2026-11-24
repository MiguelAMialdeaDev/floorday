package studio.mialdea.floorday.feature.history.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import studio.mialdea.floorday.core.domain.usecase.ObserveHabitWeekUseCase
import studio.mialdea.floorday.feature.history.HistoryViewModel

val historyFeatureModule = module {
    factoryOf(::ObserveHabitWeekUseCase)
    viewModelOf(::HistoryViewModel)
}
