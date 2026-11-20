package studio.mialdea.floorday

import android.app.Application
import org.koin.android.ext.koin.androidContext
import studio.mialdea.floorday.shared.di.initKoin

class FloordayApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@FloordayApplication)
        }
    }
}
