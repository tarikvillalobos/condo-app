package app.condo

import app.condo.domain.SystemAppClock
import app.condo.platform.IosServices
import app.condo.presentation.AppController
import androidx.compose.runtime.*
import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = ComposeUIViewController {
    val platform = remember { IosServices() }
    DisposableEffect(controller) { onDispose { controller.close() } }
    CondoApp(controller)
}
