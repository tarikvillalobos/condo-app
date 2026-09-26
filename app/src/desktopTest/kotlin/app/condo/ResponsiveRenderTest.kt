package app.condo

import app.condo.demo.DemoRepository
import app.condo.domain.*
import app.condo.presentation.*
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.unit.Density
import kotlinx.coroutines.*
import java.io.File
import kotlin.test.*
import kotlin.time.Instant

@OptIn(ExperimentalComposeUiApi::class)
class ResponsiveRenderTest {
    @Test fun renderReferenceScreensAtAllRequestedWidths() = runBlocking(Dispatchers.Main) {
        val clock = AppClock { Instant.parse("2026-09-26T12:00:00Z") }
        val platform = TestServices()
        val demo = DemoRepository(platform.store, clock, 0)
        var extendedLists = false
        val repository = object : CondoRepository by demo {
            override suspend fun load(membershipId: String): Snapshot {
                val snapshot = demo.load(membershipId)
                if (!extendedLists) return snapshot
                return snapshot.copy(
                    parcels = List(60) { index -> snapshot.parcels.first().copy(
                        id = "long-$index", carrier = "Transportadora com nome extenso para validar o cartão $index",
                    ) },
                    cameras = List(12) { index -> snapshot.cameras.first().copy(
                        id = "camera-$index", name = "Área comum com identificação extensa $index",
                    ) },
                )
            }
        }
        val controller = AppController(repository, platform, clock, storageDispatcher = Dispatchers.Main.immediate)
        val folder = File(if (BRAND_ID == "condo") "build/validation" else "build/validation-$BRAND_ID").apply { mkdirs() }
        fun render(name: String, width: Int, height: Int, scale: Float = 1f) {
            val scene = ImageComposeScene(width, height, Density(1f, scale)) { CondoApp(controller) }
            try {
                repeat(3) { scene.render(it * 16_000_000L) }
                val image = scene.render(64_000_000L)
                val data = image.encodeToData()!!
                File(folder, "$name-$width-$height-$scale.png").writeBytes(data.bytes)
                assertEquals(width, image.width)
                assertEquals(height, image.height)
                image.close()
                data.close()
            } finally { scene.close() }
        }
        for (width in listOf(320, 390, 430, 600, 840, 1200)) render("login", width, 844)
        render("login-font200", 390, 844, 2f)
        controller.login("alex@condo.demo", "Demo1234!", false)
        yield()
        assertNotNull(controller.state.value.snapshot)
        if (BRAND_ID == "viva") {
            controller.navigate(Route.CAMERAS)
            assertEquals(Route.HOME, controller.state.value.destination.route)
            assertNotNull(controller.state.value.message)
            controller.clearMessage()
        }
        val routes = listOf(Route.HOME, Route.PARCELS, Route.PARCEL_DETAIL, Route.CAMERAS,
            Route.VISITS, Route.PETS, Route.BOOKINGS, Route.PROFILE)
        for (width in listOf(320, 390, 430, 600, 840, 1200)) {
            for (route in routes.filter { controller.state.value.allows(it) }) {
                controller.navigate(route, if (route == Route.PARCEL_DETAIL) "p1" else null)
                render(route.name.lowercase(), width, 844)
            }
        }
        controller.navigate(Route.HOME)
        render("home-font200", 390, 844, 2f)
        controller.navigate(Route.PROFILE)
        render("profile-font200", 390, 844, 2f)
        controller.navigate(Route.BOOKINGS)
        render("bookings-landscape", 844, 390)
        controller.beginVisit()
        controller.field("visit.name", "Visitante com nome extenso para testar quebra de texto e preservação")
        render("visit-form", 390, 844)
        render("visit-form-wide", 1200, 844)
        assertTrue(controller.formValue("visit.name").startsWith("Visitante com nome"))
        controller.scenario(DemoScenario.EMPTY)
        yield()
        controller.navigate(Route.PARCELS)
        render("parcels-empty", 390, 844)
        if (controller.state.value.allows(Route.CAMERAS)) {
            controller.navigate(Route.CAMERAS)
            render("cameras-empty", 600, 844)
        }
        extendedLists = true
        controller.scenario(DemoScenario.NORMAL)
        yield()
        assertEquals(60, controller.state.value.snapshot!!.parcels.size)
        controller.navigate(Route.PARCELS)
        render("parcels-extensive", 320, 844)
        render("parcels-extensive", 1200, 844)
        controller.navigate(Route.CAMERAS)
        render("cameras-extensive", 1200, 844)
        controller.navigate(Route.BOOKINGS)
        render("bookings-font200", 390, 844, 2f)
        controller.close()
    }
}
