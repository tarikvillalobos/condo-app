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
        val controller = AppController(DemoRepository(platform.store, clock, 0), platform, clock)
        val folder = File("build/validation").apply { mkdirs() }
