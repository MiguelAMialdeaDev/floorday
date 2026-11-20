package studio.mialdea.floorday.shared

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import studio.mialdea.floorday.core.designsystem.theme.FloordayTheme
import studio.mialdea.floorday.feature.history.HistoryRoute
import studio.mialdea.floorday.feature.settings.SettingsRoute
import studio.mialdea.floorday.feature.today.TodayRoute
import studio.mialdea.floorday.shared.navigation.HistoryKey
import studio.mialdea.floorday.shared.navigation.SettingsKey
import studio.mialdea.floorday.shared.navigation.TodayKey
import studio.mialdea.floorday.shared.navigation.floordaySavedStateConfiguration

@Composable
fun FloordayApp() {
    FloordayTheme {
        val backStack = rememberNavBackStack(floordaySavedStateConfiguration, TodayKey)
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            // El decorador de ViewModel es obligatorio: liga cada ViewModel a su entrada del back stack.
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = entryProvider {
                entry<TodayKey> {
                    TodayRoute(
                        onOpenHistory = { backStack.add(HistoryKey) },
                        onOpenSettings = { backStack.add(SettingsKey) },
                    )
                }
                entry<HistoryKey> { HistoryRoute() }
                entry<SettingsKey> { SettingsRoute() }
            },
        )
    }
}
