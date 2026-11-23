package studio.mialdea.floorday

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import studio.mialdea.floorday.core.data.platform.AndroidDataPlatform
import studio.mialdea.floorday.di.androidPlatformModule
import studio.mialdea.floorday.shared.di.dataModule
import studio.mialdea.floorday.shared.di.initKoin
import studio.mialdea.floorday.shared.di.launchDataStartup

class FloordayApplication : Application() {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        val koin = initKoin(platformModules = listOf(dataModule(AndroidDataPlatform(this)), androidPlatformModule)) {
            androidContext(this@FloordayApplication)
        }.koin
        koin.launchDataStartup(appScope)
    }
}
