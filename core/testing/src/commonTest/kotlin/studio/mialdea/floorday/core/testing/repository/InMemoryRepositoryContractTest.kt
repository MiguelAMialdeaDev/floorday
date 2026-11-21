package studio.mialdea.floorday.core.testing.repository

import studio.mialdea.floorday.core.testing.contract.RepositoryContractTest

class InMemoryRepositoryContractTest : RepositoryContractTest() {
    override fun createRepositories(): Repositories {
        val store = InMemoryFloordayStore()
        return Repositories(InMemoryHabitRepository(store), InMemoryCompletionRepository(store))
    }
}
