package app.condo.presentation

import app.condo.demo.DemoRepository
import app.condo.domain.*
import app.condo.platform.PlatformServices
import kotlinx.coroutines.test.*
import kotlin.test.*
import kotlin.time.Instant

class TestServices : PlatformServices {
    val values = mutableMapOf<String, String>()
    override val store = object : LocalStore {
        override fun read(key: String) = values[key]
        override fun write(key: String, value: String) { values[key] = value }
        override fun remove(key: String) { values.remove(key) }
    }
    override val vault = object : SessionVault {
        override val available = true
        var value: String? = null
        override fun read() = value
        override fun write(value: String): Boolean { this.value = value; return true }
        override fun clear() { value = null }
    }
    override val biometricStatus = "Teste"
    override val notificationStatus = "Teste"
    override fun copy(text: String) = Unit
    override fun share(text: String) = "Teste"
    override fun open(url: String) = "Teste"
    override fun openNotificationSettings() = "Teste"
}
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ControllerTest {
    private val clock = AppClock { Instant.parse("2026-09-26T12:00:00Z") }
    @Test fun updatesAllScreensAndClearsContextSynchronously() = runTest {
        val services = TestServices()
        val controller = AppController(DemoRepository(services.store, clock, 0), services, clock, this)
        controller.login("alex@condo.demo", "Demo1234!", true)
        advanceUntilIdle()
        assertEquals(2, controller.state.value.snapshot!!.parcels.count { it.status != ParcelStatus.COLLECTED })
        controller.execute(Command.ApplyLockerEvent(LockerEvent("event", "p1", clock.now(), true)))
        advanceUntilIdle()
        assertEquals(1, controller.state.value.snapshot!!.parcels.count { it.status != ParcelStatus.COLLECTED })
        controller.field("secretDraft", "private")
        controller.switchMembership("aguas")
        assertNull(controller.state.value.snapshot)
        assertTrue(controller.state.value.forms.isEmpty())
        advanceUntilIdle()
        assertEquals("aguas", controller.state.value.snapshot!!.membership.id)
        controller.navigate(Route.PETS)
        assertEquals(Route.HOME, controller.state.value.destination.route)
        controller.logout()
        advanceUntilIdle()
        assertNull(controller.state.value.session)
        assertNull(services.vault.read())
    }
    @Test fun failuresKeepStaleDataAndInvalidateDisplayedCodes() = runTest {
        val services = TestServices()
        val repository = DemoRepository(services.store, clock, 0)
        val controller = AppController(repository, services, clock, this)
        controller.login("alex@condo.demo", "Demo1234!", false)
