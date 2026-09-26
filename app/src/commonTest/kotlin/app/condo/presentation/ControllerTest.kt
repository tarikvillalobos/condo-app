package app.condo.presentation

import app.condo.demo.DemoRepository
import app.condo.domain.*
import app.condo.platform.PlatformServices
import kotlinx.coroutines.*
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
        val controller = AppController(DemoRepository(services.store, clock, 0), services, clock, this,
            StandardTestDispatcher(testScheduler))
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
        val controller = AppController(repository, services, clock, this, StandardTestDispatcher(testScheduler))
        controller.login("alex@condo.demo", "Demo1234!", false)
        advanceUntilIdle()
        controller.execute(Command.IssuePickupCode("p1"))
        advanceUntilIdle()
        assertNotNull(controller.state.value.code)
        repository.scenario(DemoScenario.NETWORK_ERROR)
        controller.refresh()
        advanceUntilIdle()
        assertTrue(controller.state.value.stale)
        assertNull(controller.state.value.code)
        assertNotNull(controller.state.value.snapshot)
    }
    @Test fun logoutDiscardsLateAuthenticationAndSerializesTheNextLogin() = runTest {
        val services = TestServices()
        val demo = DemoRepository(services.store, clock, 0)
        val repository = object : CondoRepository by demo {
            override suspend fun login(identifier: String, password: String) = withContext(NonCancellable) {
                delay(100)
                demo.login(identifier, password)
            }
        }
        val controller = AppController(repository, services, clock, this, StandardTestDispatcher(testScheduler))
        controller.login("alex@condo.demo", "Demo1234!", true)
        runCurrent()
        controller.logout()
        advanceUntilIdle()
        assertNull(controller.state.value.session)
        assertNull(services.vault.read())
        assertFailsWith<AppFailure> { demo.load("aurora") }
        controller.login("bia@condo.demo", "Demo1234!", true)
        advanceUntilIdle()
        assertEquals("bia", controller.state.value.session?.account?.id)
        assertTrue(services.vault.read()!!.startsWith("demo|bia|"))
    }
    @Test fun logoutWaitsForAnOlderLogoutBeforeAcceptingAnotherLogin() = runTest {
        val services = TestServices()
        val demo = DemoRepository(services.store, clock, 0)
        val repository = object : CondoRepository by demo {
            override suspend fun login(identifier: String, password: String) = withContext(NonCancellable) {
                delay(100)
                demo.login(identifier, password)
            }
        }
        val controller = AppController(repository, services, clock, this, StandardTestDispatcher(testScheduler))
        controller.login("alex@condo.demo", "Demo1234!", true)
        runCurrent()
        controller.logout()
        controller.logout()
        controller.login("bia@condo.demo", "Demo1234!", true)
        advanceUntilIdle()
        assertEquals("bia", controller.state.value.session?.account?.id)
        assertNotNull(controller.state.value.snapshot)
        assertTrue(services.vault.read()!!.startsWith("demo|bia|"))
    }
    @Test fun navigationDiscardsLateCodesEvenWhenReturningToTheSamePage() = runTest {
        val services = TestServices()
        val controller = AppController(DemoRepository(services.store, clock, 100), services, clock, this,
            StandardTestDispatcher(testScheduler))
        controller.login("alex@condo.demo", "Demo1234!", false)
        advanceUntilIdle()
        controller.navigate(Route.PARCEL_DETAIL, "p1")
        controller.execute(Command.IssuePickupCode("p1"))
        runCurrent()
        controller.navigate(Route.HOME)
        controller.navigate(Route.PARCEL_DETAIL, "p1")
        advanceUntilIdle()
        assertEquals(Destination(Route.PARCEL_DETAIL, "p1"), controller.state.value.destination)
        assertNull(controller.state.value.code)
        assertFalse(controller.state.value.showCode)
    }
    @Test fun navigationPreservesNewPageWhilePublishingCompletedChanges() = runTest {
        val services = TestServices()
        val controller = AppController(DemoRepository(services.store, clock, 100), services, clock, this,
            StandardTestDispatcher(testScheduler))
        controller.login("alex@condo.demo", "Demo1234!", false)
        advanceUntilIdle()
        controller.navigate(Route.REQUEST_FORM)
        controller.execute(Command.CreateRequest("Manutenção", "Elevador", "Verificar o elevador"), "Salvo") { controller.back() }
        runCurrent()
        controller.navigate(Route.PROFILE)
        advanceUntilIdle()
        assertEquals(Route.PROFILE, controller.state.value.destination.route)
        assertEquals("Elevador", controller.state.value.snapshot!!.requests.single().subject)
        assertNull(controller.state.value.message)
        controller.navigate(Route.PROFILE_FORM)
        controller.saveAccount("Novo Nome", "")
        runCurrent()
        controller.navigate(Route.HOME)
        advanceUntilIdle()
        assertEquals(Route.HOME, controller.state.value.destination.route)
        assertEquals("Novo Nome", controller.state.value.session?.account?.name)
    }
    @Test fun cancelledSlotCanBeReservedAgainWithoutDuplicatingRetries() = runTest {
        val services = TestServices()
        val controller = AppController(DemoRepository(services.store, clock, 0), services, clock, this,
            StandardTestDispatcher(testScheduler))
        controller.login("alex@condo.demo", "Demo1234!", false)
        advanceUntilIdle()
        controller.reserve("court", "2026-09-27", 10)
        advanceUntilIdle()
        val original = controller.state.value.snapshot!!.bookings.single()
        controller.reserve("court", "2026-09-27", 10)
        advanceUntilIdle()
        assertEquals(1, controller.state.value.snapshot!!.bookings.size)
        controller.execute(Command.CancelBooking(original.id))
        advanceUntilIdle()
        controller.reserve("court", "2026-09-27", 10)
        advanceUntilIdle()
        val bookings = controller.state.value.snapshot!!.bookings
        assertEquals(2, bookings.size)
        assertEquals(1, bookings.count { !it.cancelled })
        assertNotEquals(original.id, bookings.single { !it.cancelled }.id)
    }
    @Test fun vehicleSaveKeepsItsIdentityAcrossNavigationAndClearsCompletedForm() = runTest {
        val services = TestServices()
        val controller = AppController(DemoRepository(services.store, clock, 100), services, clock, this,
            StandardTestDispatcher(testScheduler))
        controller.login("alex@condo.demo", "Demo1234!", false)
        advanceUntilIdle()
        controller.navigate(Route.VEHICLES)
        controller.field("vehicle.model", "Sedan")
        controller.field("vehicle.plate", "ABC1D23")
        controller.submitVehicle()
        runCurrent()
        controller.navigate(Route.PROFILE)
        advanceUntilIdle()
        controller.navigate(Route.VEHICLES)
        controller.submitVehicle()
        advanceUntilIdle()
        assertEquals(1, controller.state.value.snapshot!!.vehicles.count { it.plate == "ABC1D23" })
        assertEquals("", controller.formValue("vehicle.model"))
        controller.submitVehicle()
        advanceUntilIdle()
        assertEquals(1, controller.state.value.snapshot!!.vehicles.count { it.plate == "ABC1D23" })
    }
}
