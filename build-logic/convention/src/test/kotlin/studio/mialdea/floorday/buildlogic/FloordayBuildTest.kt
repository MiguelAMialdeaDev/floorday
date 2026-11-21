package studio.mialdea.floorday.buildlogic

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FloordayBuildTest {
    @Test fun namespaceOfNestedModule() =
        assertEquals("studio.mialdea.floorday.core.domain", FloordayBuild.namespaceFor(":core:domain"))

    @Test fun namespaceOfTopLevelModule() =
        assertEquals("studio.mialdea.floorday.shared", FloordayBuild.namespaceFor(":shared"))

    @Test fun hyphensBecomeUnderscores() =
        assertEquals("studio.mialdea.floorday.feature.habit_editor", FloordayBuild.namespaceFor(":feature:habit-editor"))

    @Test fun rootProjectHasNoNamespace() {
        assertFailsWith<IllegalArgumentException> { FloordayBuild.namespaceFor(":") }
    }

    @Test fun versionCodeFromGitOutputWithNewline() = assertEquals(142, FloordayBuild.versionCodeFrom("142\n"))

    @Test fun versionCodeWithoutGitIsOne() = assertEquals(1, FloordayBuild.versionCodeFrom(null))

    @Test fun versionCodeWithEmptyOutputIsOne() = assertEquals(1, FloordayBuild.versionCodeFrom(""))

    @Test fun versionCodeWithGarbageIsOne() =
        assertEquals(1, FloordayBuild.versionCodeFrom("fatal: not a git repository"))

    @Test fun versionCodeZeroIsOne() = assertEquals(1, FloordayBuild.versionCodeFrom("0"))

    @Test fun versionCodeIsCappedAtPlayLimit() =
        assertEquals(2_100_000_000, FloordayBuild.versionCodeFrom("2100000001"))

    @Test fun jvmTestTargetOnlyInCoreDataAndItsDependencies() = assertEquals(
        listOf(true, true, true, false, false),
        listOf(":core:domain", ":core:data", ":core:testing", ":core:designsystem", ":feature:today")
            .map(FloordayBuild::hasJvmTestTarget),
    )
}
