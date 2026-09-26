package app.condo

import app.condo.design.*
import app.condo.domain.*
import app.condo.platform.PlatformServices
import app.condo.presentation.*
import app.condo.presentation.screens.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CondoApp(controller: AppController) {
    val state by controller.state.collectAsState()
    CondoTheme {
        Surface(Modifier.fillMaxSize(), color = Tokens.background) {
            BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding().imePadding()) {
                val wide = maxWidth >= Tokens.compact
                val expanded = maxWidth >= Tokens.expanded
                Column {
                    if (controller.repository.isDemo) {
                        Surface(onClick = { controller.navigate(Route.DEMO) }, color = Tokens.tint) {
                            Text("Demonstração · dados fictícios", Modifier.fillMaxWidth().padding(Tokens.sm),
                                color = LocalBrand.current.dark, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Row(Modifier.weight(1f)) {
                        if (state.session != null && wide) SideNavigation(controller, state, expanded)
                        Column(Modifier.weight(1f)) {
                            if (state.session != null) AppHeader(controller, state)
                            if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
                            val scroll = key(state.destination) { rememberScrollState() }
                            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                                Column(
                                    Modifier.widthIn(max = Tokens.maxContent).fillMaxWidth()
                                        .verticalScroll(scroll).padding(Tokens.page),
