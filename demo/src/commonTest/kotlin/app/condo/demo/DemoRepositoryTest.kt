package app.condo.demo

import app.condo.domain.*
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import kotlin.test.*
import kotlin.time.Instant
import kotlin.time.Duration.Companion.hours

class DemoRepositoryTest {
    private class MemoryStore : LocalStore {
        val values = mutableMapOf<String, String>()
        override fun read(key: String) = values[key]
        override fun write(key: String, value: String) { values[key] = value }
        override fun remove(key: String) { values.remove(key) }
    }
    private val store = MemoryStore()
    private var now = Instant.parse("2026-09-26T12:00:00Z")
    private fun repo() = DemoRepository(store, AppClock { now }, 0)
    private suspend fun DemoRepository.signIn() = login("alex@condo.demo", "Demo1234!")

    @Test fun authenticationAndMembershipIsolation() = runTest {
        val r = repo()
        assertFailsWith<AppFailure> { r.login("alex@condo.demo", "invalid!") }
        r.signIn()
        assertFailsWith<AppFailure> { r.load("not-linked") }
        r.execute("aurora", Command.ReportCollected("p1"))
        assertEquals(ParcelStatus.WAITING, r.load("aguas").parcels.first().status)
        r.logout()
        r.login("bia@condo.demo", "Demo1234!")
        assertEquals(ParcelStatus.WAITING, r.load("aurora").parcels.first().status)
    }
    @Test fun manualReportDoesNotClaimHardwareCollectionAndPersists() = runTest {
        val r = repo()
        r.signIn()
        r.execute("aurora", Command.ReportCollected("p1"))
        val restored = repo()
        restored.signIn()
        val parcel = restored.load("aurora").parcels.first()
        assertEquals(ParcelStatus.MANUAL_REPORT, parcel.status)
