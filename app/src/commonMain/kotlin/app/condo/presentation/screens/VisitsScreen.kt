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
        Muted("Agende uma visita e compartilhe um convite com validade. Na demonstração, o código não libera acesso real.")
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
