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
        EmptyState("Selecione uma encomenda", "Os detalhes e as opções de retirada aparecerão aqui.", Glyph.BOX)
        return
    }
    Heading(parcel.carrier)
    Muted(parcel.tracking?.let { "Rastreio $it" } ?: "Código de rastreio não informado")
    StatusChip(parcel.statusLabel(), parcel.status != ParcelStatus.COLLECTED)
    Panel {
        Heading("Locker ${parcel.locker}")
        Text("Compartimento ${parcel.compartment}")
        Text("Retire até ${parcel.deadline.fullLabel()}")
        Muted("Apresente o QR Code ao leitor ou digite o código numérico no painel do locker.")
        if (parcel.status != ParcelStatus.COLLECTED) PrimaryButton("Ver QR Code de retirada", !state.stale && !state.submitting) {
            controller.execute(Command.IssuePickupCode(parcel.id))
        }
    }
    Panel {
        Heading("Acompanhe sua encomenda")
        Text("✓ Depositada · ${parcel.receivedAt.fullLabel()}")
        Text("✓ Aviso disponível no aplicativo")
        Text("◷ Prazo · ${parcel.deadline.fullLabel()}")
        parcel.collectedAt?.let { Text("✓ Evento de retirada simulado · ${it.fullLabel()}") }
        if (parcel.status == ParcelStatus.MANUAL_REPORT) Muted("Você informou a retirada. Nenhuma confirmação física real foi recebida.")
    }
    if (parcel.status == ParcelStatus.WAITING) SecondaryButton("Já retirei a encomenda") {
        controller.execute(Command.ReportCollected(parcel.id), "Retirada informada. A confirmação física depende do locker via API.")
    }
    SecondaryButton("Relatar um problema") {
        controller.field("request.subject", "Problema com encomenda ${parcel.id}")
        controller.navigate(Route.REQUEST_FORM)
    }
    if (controller.repository.isDemo && parcel.status != ParcelStatus.COLLECTED) {
        TextButton({ controller.execute(Command.ApplyLockerEvent(
            LockerEvent("pickup-${parcel.id}", parcel.id, controller.clock.now(), true)),
            "Evento simulado recebido. Nenhum equipamento real foi acionado.") }) {
            Text("Simular evento de retirada do locker")
        }
    }
}
