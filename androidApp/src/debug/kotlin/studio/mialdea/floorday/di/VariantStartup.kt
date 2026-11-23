package studio.mialdea.floorday.di

import android.content.Context
import android.util.Log
import java.io.FileNotFoundException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.core.Koin
import org.koin.core.module.Module
import org.koin.dsl.module
import studio.mialdea.floorday.core.domain.time.LogicalDayResolver
import studio.mialdea.floorday.debug.DebugSeeder

private const val TAG = "FloordaySeed"

/** Seed file in the debug assets. It holds personal data, so it is gitignored. */
private const val SEED_ASSET = "seed.json"

/** Debug variant of the startup hooks: the seeder. The release variant does nothing. */
internal object VariantStartup {

    val modules: List<Module> = listOf(
        module {
            factory {
                val context = androidContext()
                DebugSeeder(get(), get(), LogicalDayResolver(get(), get())) { readSeed(context) }
            }
        },
    )

    fun launch(koin: Koin, scope: CoroutineScope) {
        scope.launch {
            // A bad seed must not crash the app: it is a development aid, the log says why.
            runCatching { koin.get<DebugSeeder>().seedIfEmpty() }
                .onSuccess { count -> if (count > 0) Log.i(TAG, "Seeded $count habits from $SEED_ASSET") }
                .onFailure { Log.w(TAG, "Could not seed from $SEED_ASSET", it) }
        }
    }

    private fun readSeed(context: Context): String? = try {
        context.assets.open(SEED_ASSET).bufferedReader().use { it.readText() }
    } catch (_: FileNotFoundException) {
        null
    }
}
