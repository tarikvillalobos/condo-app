package app.condo.data

import app.condo.domain.*
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class UnavailableRepositoryTest {
    @Test fun productionNeverAuthenticatesOrFallsBackWithoutContract() = runTest {
        val repository = UnavailableRepository()
        assertFalse(repository.isDemo)
        assertFailsWith<AppFailure> { repository.login("alex@condo.demo", "Demo1234!") }
        assertFailsWith<AppFailure> { repository.load("aurora") }
        assertFailsWith<AppFailure> { repository.restore("demo|alex|9999999999999") }
    }
}
