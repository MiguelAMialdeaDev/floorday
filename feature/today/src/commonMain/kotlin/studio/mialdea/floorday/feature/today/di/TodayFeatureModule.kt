package studio.mialdea.floorday.feature.today.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import studio.mialdea.floorday.core.domain.model.IdGenerator
import studio.mialdea.floorday.core.domain.usecase.DeleteHabitUseCase
import studio.mialdea.floorday.core.domain.usecase.ObserveTodayUseCase
import studio.mialdea.floorday.core.domain.usecase.RestoreCompletionUseCase
import studio.mialdea.floorday.core.domain.usecase.SaveHabitUseCase
import studio.mialdea.floorday.core.domain.usecase.ToggleSlotUseCase
import studio.mialdea.floorday.feature.today.TodayViewModel
import studio.mialdea.floorday.feature.today.editor.HabitEditorViewModel

val todayFeatureModule = module {
    factoryOf(::ObserveTodayUseCase)
    factoryOf(::ToggleSlotUseCase)
    factoryOf(::RestoreCompletionUseCase)
    viewModelOf(::TodayViewModel)

    factory { SaveHabitUseCase(get(), get(), get(), IdGenerator.Random) }
    factoryOf(::DeleteHabitUseCase)
    // The habit id comes from the navigation key: koinViewModel { parametersOf(habitId) }, null = new.
    viewModel { params -> HabitEditorViewModel(params.getOrNull(), get(), get(), get(), get()) }
}
