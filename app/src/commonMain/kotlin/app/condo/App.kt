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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun CondoApp(controller: AppController) {
    val state by controller.state.collectAsState()
    CondoTheme {
        Surface(Modifier.fillMaxSize(), color = Color.White) {
            BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding().imePadding()) {
                val wide = maxWidth >= Tokens.compact
                val expanded = maxWidth >= Tokens.expanded
                val compactLabels = maxWidth < 360.dp
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
                            if (state.session != null) AppHeader(controller, state, compactLabels || LocalDensity.current.fontScale > 1.3f)
                            if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
                            val scroll = key(state.destination) { rememberScrollState() }
                            LaunchedEffect(state.session?.account?.id, state.snapshot?.membership?.id, state.destination) {
                                scroll.scrollTo(0)
                            }
                            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                                Column(
                                    Modifier.widthIn(max = Tokens.maxContent).fillMaxWidth()
                                        .verticalScroll(scroll).padding(Tokens.page),
                                    verticalArrangement = Arrangement.spacedBy(Tokens.lg),
                                ) {
                                    state.error?.let {
                                        Panel(color = Tokens.warning) {
                                            Text(it, color = Tokens.onWarning)
                                            if (state.stale) Muted("Exibindo dados anteriores. Códigos e confirmações exigem atualização.")
                                            SecondaryButton("Tentar novamente") { controller.refresh() }
                                        }
                                    }
                                    if (state.session == null) AuthScreen(controller, state)
                                    else if (state.snapshot == null) {
                                        if (!state.loading) EmptyState("Conteúdo indisponível", "Atualize para carregar este condomínio.")
                                    } else ScreenRouter(controller, state, expanded)
                                    Spacer(Modifier.height(Tokens.md))
                                }
                            }
                            if (state.session != null && !wide) BottomNavigation(controller, state, compactLabels)
                        }
                    }
                }
                if (state.submitting) LinearProgressIndicator(Modifier.fillMaxWidth().align(Alignment.TopCenter))
                if (state.showCode) CodeDialog(controller, state)
                state.message?.let { message ->
                    AlertDialog(
                        onDismissRequest = controller::clearMessage,
                        title = { Text(LocalBrand.current.name) }, text = { Text(message) },
                        confirmButton = { TextButton(controller::clearMessage) { Text("Entendi") } },
                    )
                }
            }
        }
    }
}
@Composable
private fun AppHeader(controller: AppController, state: AppState, stacked: Boolean) {
    val title = if (state.destination.route == Route.HOME) LocalBrand.current.name else state.destination.route.title
    Column {
    Row(Modifier.fillMaxWidth().background(Color.White).padding(horizontal = Tokens.page, vertical = Tokens.sm),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Tokens.md)) {
        if (state.destination.route != Route.HOME) {
            IconButton(controller::back) { AppIcon(Glyph.BACK, "Voltar") }
        } else AppIcon(Glyph.BUILDING, modifier = Modifier.size(32.dp))
        Text(if (state.destination.route == Route.HOME) LocalBrand.current.name else state.destination.route.title,
            Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, color = LocalBrand.current.dark)
        IconButton({ controller.navigate(Route.NOTIFICATIONS) }) {
            BadgedBox(badge = {
                val count = state.snapshot?.notices?.count { !it.read } ?: 0
                if (count > 0) {
                    if (LocalDensity.current.fontScale > 1.3f) Badge()
                    else Badge { Text(count.toString()) }
                }
            }) { AppIcon(Glyph.BELL, "Notificações, ${state.snapshot?.notices?.count { !it.read } ?: 0} não lidas") }
        }
        IconButton(controller::refresh) { AppIcon(Glyph.CLOCK, "Atualizar conteúdo") }
    }
}
