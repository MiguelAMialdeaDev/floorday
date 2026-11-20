package studio.mialdea.floorday.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * floorday.room: stub en S0. Solo aplica floorday.kmp.library.
 * El PR de Room 3 (issue #10) añade KSP por target, room3, sqlite-bundled, el esquema exportado
 * y los plugins `ksp` y `room3` con apply false en la raíz.
 */
class RoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.pluginManager.apply(target.libs.pluginId("floorday-kmp-library"))
    }
}
