package studio.mialdea.floorday.core.data.repository

import studio.mialdea.floorday.core.data.db.inMemoryDatabase
import studio.mialdea.floorday.core.testing.contract.RepositoryContractTest

/** The Room repositories pass the same contract as the in-memory fakes of :core:testing. */
class RoomRepositoryContractTest : RepositoryContractTest() {
    override fun createRepositories(): Repositories {
        val database = inMemoryDatabase()
        return Repositories(
            habits = RoomHabitRepository(database),
            completions = RoomCompletionRepository(database),
            close = database::close,
        )
    }
}
