package app.condo.presentation.screens

import app.condo.design.*
import app.condo.domain.*
import app.condo.presentation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import kotlinx.datetime.toLocalDateTime

@Composable
fun VisitsScreen(controller: AppController, state: AppState) {
    if (state.destination.route == Route.VISIT_FORM) {
        VisitForm(controller, state)
        return
    }
    val snapshot = state.snapshot!!
    PrimaryButton("+ Nova visita", !state.submitting) { controller.beginVisit() }
    Panel(color = Tokens.tint) {
        Heading("Convite com QR Code")
        Muted(if (controller.repository.isDemo) "Agende uma visita demonstrativa; o código não libera acesso real." else "Agende uma visita e compartilhe um convite com validade.")
    }
    val filter = state.filters["visits"] ?: "Hoje"
    FilterChips(listOf("Hoje", "Agendadas", "Histórico"), filter) { controller.filter("visits", it) }
    val now = controller.clock.now()
    val today = now.toLocalDateTime(condominiumZone).date
    val visits = snapshot.visits.filter { visit ->
        when (filter) {
            "Hoje" -> visit.startsAt.toLocalDateTime(condominiumZone).date == today
            "Agendadas" -> visit.expiresAt > now && visit.status in setOf(VisitStatus.SCHEDULED, VisitStatus.AUTHORIZED)
            else -> visit.expiresAt <= now || visit.status in setOf(VisitStatus.ENTERED, VisitStatus.REVOKED, VisitStatus.DENIED)
        }
    }
    if (visits.isEmpty()) EmptyState("Nenhuma visita neste período", "Use Nova visita para criar um convite.", Glyph.PEOPLE)
    AdaptiveGrid(visits) { visit ->
        Panel {
            Heading(visit.name)
            Muted("${if (visit.provider) "Prestador" else "Visitante"} · ${visit.purpose}")
            Text("${visit.startsAt.fullLabel()} até ${visit.expiresAt.fullLabel()}")
            StatusChip(visit.statusLabel(now), visit.status == VisitStatus.AT_GATE)
            if (visit.status == VisitStatus.AT_GATE) {
                PrimaryButton("Liberar entrada", !state.submitting) {
                    controller.execute(Command.SetVisitStatus(visit.id, VisitStatus.AUTHORIZED), "Autorização registrada no simulador.")
                }
                SecondaryButton("Recusar") { controller.execute(Command.SetVisitStatus(visit.id, VisitStatus.DENIED)) }
            }
            if (visit.status in setOf(VisitStatus.SCHEDULED, VisitStatus.AUTHORIZED) && visit.expiresAt > now) {
                PrimaryButton("Ver convite", !state.submitting && !state.stale) { controller.execute(Command.IssueVisitCode(visit.id)) }
                SecondaryButton("Editar visita") { controller.beginVisit(visit) }
                TextButton({ controller.execute(Command.SetVisitStatus(visit.id, VisitStatus.REVOKED), if (controller.repository.isDemo) "Convite revogado na demonstração." else "Convite revogado.") }) { Text("Revogar convite") }
            }
        }
    }
    Heading("Visitantes frequentes")
    snapshot.visits.filter { it.frequent }.distinctBy { it.name }.forEach { visit ->
        MenuRow(visit.name, visit.purpose, Glyph.PEOPLE) {
            controller.beginVisit(visit.copy(id = "", startsAt = now, expiresAt = now + kotlin.time.Duration.parse("2h")))
        }
    }
}
@Composable
private fun VisitForm(controller: AppController, state: AppState) {
    Column(Modifier.widthIn(max = Tokens.maxForm), verticalArrangement = Arrangement.spacedBy(Tokens.md)) {
        FormField(controller, "visit.name", "Nome completo")
        FormField(controller, "visit.purpose", "Motivo ou serviço")
        FilterChips(listOf("Visitante", "Prestador"), if (state.forms["visit.provider"] == "true") "Prestador" else "Visitante") {
            controller.field("visit.provider", (it == "Prestador").toString())
        }
        FormField(controller, "visit.date", "Data de início · AAAA-MM-DD")
        FormField(controller, "visit.time", "Horário de início · HH:MM")
        FormField(controller, "visit.endDate", "Data final · AAAA-MM-DD")
        FormField(controller, "visit.endTime", "Horário final · HH:MM")
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(state.forms["visit.frequent"] == "true", { controller.field("visit.frequent", it.toString()) })
            Text("Salvar como visitante frequente")
        }
        PrimaryButton("Salvar visita", !state.submitting, controller::submitVisit)
    }
}
