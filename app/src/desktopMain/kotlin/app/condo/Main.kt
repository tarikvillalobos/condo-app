package app.condo

import app.condo.design.Brands
import app.condo.domain.SystemAppClock
import app.condo.platform.DesktopServices
import app.condo.presentation.AppController
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*

fun main() = application {
    val platform = remember { DesktopServices() }
    DisposableEffect(controller) { onDispose { controller.close() } }
    Window(
        onCloseRequest = ::exitApplication,
        title = Brands.current.name,
        state = rememberWindowState(width = 1100.dp, height = 860.dp),
    ) { CondoApp(controller) }
}
