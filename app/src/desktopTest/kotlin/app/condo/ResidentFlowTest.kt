package app.condo

import app.condo.demo.DemoRepository
import app.condo.domain.*
import app.condo.presentation.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test
import kotlin.time.Instant

class ResidentFlowTest {
    @get:Rule val compose = createComposeRule()
    @Test fun residentCanLoginOpenParcelAndReportCollection() {
        val platform = TestServices()
        val clock = AppClock { Instant.parse("2026-09-26T12:00:00Z") }
        val controller = AppController(DemoRepository(platform.store, clock, 0), platform, clock)
        compose.setContent { CondoApp(controller) }
        compose.onNodeWithText("Entrar na demonstração").performScrollTo().performClick()
        compose.waitUntil(5000) { controller.state.value.snapshot != null }
