package studio.mialdea.floorday.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * floorday.room: stub in S0. It only applies floorday.kmp.library.
 * The Room 3 PR (issue #10) adds per-target KSP, room3, sqlite-bundled, the exported schema
 * and the `ksp` and `room3` plugins with apply false in the root.
 */
class RoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.pluginManager.apply(target.libs.pluginId("floorday-kmp-library"))
    }
}
