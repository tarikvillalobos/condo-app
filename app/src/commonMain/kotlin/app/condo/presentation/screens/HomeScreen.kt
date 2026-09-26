package app.condo.presentation.screens

import app.condo.design.*
import app.condo.domain.*
import app.condo.presentation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(controller: AppController, state: AppState) {
    val snapshot = state.snapshot ?: return
    Text("Olá, ${state.session?.account?.name?.substringBefore(' ')}", style = MaterialTheme.typography.headlineMedium, color = LocalBrand.current.dark)
    MenuRow(snapshot.membership.name, snapshot.membership.unit, Glyph.BUILDING) { controller.navigate(Route.CONDOMINIUMS) }
    val pending = snapshot.parcels.filter { it.status != ParcelStatus.COLLECTED }
    if (state.allows(Route.PARCELS)) Surface(color = LocalBrand.current.dark, shape = Tokens.largeCorner) {
        Column(Modifier.padding(Tokens.page), verticalArrangement = Arrangement.spacedBy(Tokens.lg)) {
            Row(horizontalArrangement = Arrangement.spacedBy(Tokens.md), verticalAlignment = Alignment.CenterVertically) {
                AppIcon(Glyph.BOX, modifier = Modifier.size(36.dp), tint = Color.White)
                Column(Modifier.weight(1f)) {
                    Text(if (pending.isEmpty()) "Tudo retirado por aqui" else "${pending.size} encomenda${if (pending.size > 1) "s" else ""} no locker",
                        style = MaterialTheme.typography.titleMedium, color = Color.White)
                    Text(if (pending.isEmpty()) "Avisaremos quando chegar uma novidade." else "Retire até ${pending.minOf { it.deadline }.dateLabel()}",
                        style = MaterialTheme.typography.bodySmall, color = Color.White)
                }
            }
            Button({ controller.navigate(Route.PARCELS) }, Modifier.fillMaxWidth().heightIn(min = Tokens.touch),
                shape = Tokens.controlCorner, colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = LocalBrand.current.dark)) {
                AppIcon(Glyph.QR)
                Spacer(Modifier.width(Tokens.sm))
                Text(if (pending.isEmpty()) "Ver histórico" else "Ver QR Code de retirada")
            }
        }
    }
    val shortcuts = listOf(Route.PARCELS, Route.VISITS, Route.CAMERAS, Route.PETS,
        Route.BOOKINGS, Route.NOTICES, Route.SERVICES, Route.CONCIERGE).filter(state::allows)
    AdaptiveGrid(shortcuts, minimum = 70.dp, maximumColumns = 4) { route ->
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            FilledTonalIconButton({ controller.navigate(route) }, Modifier.size(60.dp), shape = Tokens.corner) {
                AppIcon(route.glyph(), route.title, Modifier.size(26.dp))
            }
            Text(route.title, Modifier.padding(top = Tokens.sm), style = MaterialTheme.typography.bodySmall)
        }
    }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Heading("Avisos do condomínio", Modifier.weight(1f))
        TextButton({ controller.navigate(Route.NOTICES) }) { Text("Ver todos") }
    }
    if (snapshot.bulletins.isEmpty()) EmptyState("Nenhum aviso novo", "As novidades da administração aparecerão aqui.", Glyph.NOTICE)
    snapshot.bulletins.take(2).forEach { bulletin ->
        MenuRow(bulletin.title, "${bulletin.date.dateLabel()} · ${bulletin.body.substringBefore('.')}", Glyph.CALENDAR) {
            controller.navigate(Route.BULLETIN, bulletin.id)
        }
    }
}
