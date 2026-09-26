package app.condo.presentation.screens

import app.condo.design.*
import app.condo.domain.*
import app.condo.presentation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.days

@Composable
fun ParcelScreen(controller: AppController, state: AppState, expanded: Boolean) {
    val snapshot = state.snapshot ?: return
    val selected = state.destination.id?.let { id -> snapshot.parcels.find { it.id == id } }
    if (expanded) {
        Row(horizontalArrangement = Arrangement.spacedBy(Tokens.xl)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Tokens.lg)) { ParcelList(controller, state) }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Tokens.lg)) {
                ParcelDetail(controller, state, selected ?: snapshot.parcels.firstOrNull())
            }
        }
    } else if (state.destination.route == Route.PARCEL_DETAIL) ParcelDetail(controller, state, selected)
    else ParcelList(controller, state)
}
@Composable
private fun ParcelList(controller: AppController, state: AppState) {
    val parcels = state.snapshot!!.parcels
    val filter = state.filters["parcels"] ?: "Todas"
    FilterChips(listOf("Todas", "Aguardando", "Histórico"), filter) { controller.filter("parcels", it) }
    val now = controller.clock.now()
    val metrics = parcelMetrics(parcels, now - 30.days, now)
    AdaptiveGrid(listOf("Recebidas nos últimos 30 dias" to "${metrics.received} encomendas",
        "Tempo médio de retirada" to (metrics.averageMinutes?.let { "${it / 60}h ${it % 60}min" } ?: "Sem retiradas válidas")), minimum = 145.dp, maximumColumns = 2) {
        Panel(color = Tokens.background) {
            Muted(it.first)
            Heading(it.second)
        }
    }
    val visible = parcels.filter {
        when (filter) {
            "Aguardando" -> it.status != ParcelStatus.COLLECTED
            "Histórico" -> it.status == ParcelStatus.COLLECTED
            else -> true
        }
    }
    if (visible.isEmpty()) EmptyState("Nenhuma encomenda", "As encomendas deste filtro aparecerão aqui.", Glyph.BOX)
    visible.forEach { parcel ->
        Panel {
            MenuRow(parcel.carrier, "${parcel.receivedAt.fullLabel()} · ${parcel.locker}", Glyph.BOX) {
                controller.navigate(Route.PARCEL_DETAIL, parcel.id)
            }
            StatusChip(parcel.statusLabel(), parcel.status != ParcelStatus.COLLECTED)
        }
    }
}
@Composable
private fun ParcelDetail(controller: AppController, state: AppState, parcel: Parcel?) {
    if (parcel == null) {
