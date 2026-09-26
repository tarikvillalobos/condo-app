package app.condo.presentation.screens

import app.condo.design.*
import app.condo.domain.*
import app.condo.presentation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*

@Composable
fun CommunityScreen(controller: AppController, state: AppState) {
    val snapshot = state.snapshot!!
    when (state.destination.route) {
        Route.BULLETIN -> {
            snapshot.bulletins.find { it.id == state.destination.id }?.let {
                Heading(it.title)
                StatusChip(if (it.event) "Evento · ${it.date.fullLabel()}" else "Aviso · ${it.date.dateLabel()}")
                Panel { Text(it.body) }
            }
        }
        Route.NOTICES, Route.EVENTS -> {
            val items = snapshot.bulletins.filter { state.destination.route != Route.EVENTS || it.event }
            if (items.isEmpty()) EmptyState("Nenhuma novidade", "Os avisos e eventos aparecerão aqui.", Glyph.NOTICE)
            AdaptiveGrid(items) { bulletin ->
                MenuRow(bulletin.title, bulletin.date.fullLabel(), Glyph.CALENDAR) { controller.navigate(Route.BULLETIN, bulletin.id) }
            }
        }
        Route.NOTIFICATIONS -> {
            val filter = state.filters["notifications"] ?: "Todas"
            FilterChips(listOf("Todas", "Não lidas"), filter) { controller.filter("notifications", it) }
            val notices = snapshot.notices.filter { filter == "Todas" || !it.read }
            if (notices.isEmpty()) EmptyState("Tudo em dia", "Você não tem notificações neste filtro.", Glyph.BELL)
            notices.forEach { notice ->
                MenuRow(notice.title, if (notice.read) "Lida" else "Não lida", Glyph.BELL) {
                    controller.execute(Command.ReadNotice(notice.id)) {
                        val parts = notice.target.split(':', limit = 2)
                        if (parts.size == 2) controller.navigate(if (parts[0] == "parcel") Route.PARCEL_DETAIL else Route.BULLETIN, parts[1])
                    }
                }
            }
        }
        Route.REQUEST_FORM -> {
            FilterChips(listOf("Solicitação", "Ocorrência", "Privacidade"), state.forms["request.category"] ?: "Solicitação") {
                controller.field("request.category", it)
            }
            FormField(controller, "request.subject", "Assunto")
            FormField(controller, "request.body", "Descreva o que aconteceu", multiline = true)
            Muted("Não inclua senhas ou códigos de acesso. Na demonstração, o registro fica somente neste dispositivo.")
            PrimaryButton("Registrar", !state.submitting, controller::submitRequest)
        }
        Route.REQUEST_DETAIL -> snapshot.requests.find { it.id == state.destination.id }?.let {
            Heading(it.subject)
            StatusChip(it.status)
            Muted("Protocolo ${it.id} · ${it.createdAt.fullLabel()}")
            Panel { Text(it.description) }
            Heading("Histórico")
            Text("✓ ${it.createdAt.fullLabel()} · Registro demonstrativo criado")
            Muted("Respostas da administração dependem da integração real.")
        }
        Route.SERVICES -> {
            PrimaryButton("+ Nova solicitação ou ocorrência") { controller.navigate(Route.REQUEST_FORM) }
            val filter = state.filters["requests"] ?: "Todas"
            FilterChips(listOf("Todas", "Solicitação", "Ocorrência", "Privacidade"), filter) { controller.filter("requests", it) }
            val requests = snapshot.requests.filter { filter == "Todas" || it.category == filter }
            if (requests.isEmpty()) EmptyState("Nenhum registro ainda", "Acompanhe solicitações e ocorrências neste espaço.", Glyph.SHIELD)
            requests.forEach {
                MenuRow(it.subject, "${it.category} · ${it.status}", Glyph.NOTICE) { controller.navigate(Route.REQUEST_DETAIL, it.id) }
            }
        }
        Route.CONCIERGE -> {
            Panel {
                AppIcon(Glyph.SHIELD)
                Heading("Estamos por perto")
                Text(snapshot.membership.name)
                Muted("Atendimento demonstrativo · nenhum contato real configurado.")
                PrimaryButton("Contato da portaria") { controller.message("O telefone da portaria ainda não foi informado pela API deste condomínio.") }
                SecondaryButton("Criar solicitação") { controller.navigate(Route.REQUEST_FORM) }
            }
            Heading("Encomendas e visitantes")
            MenuRow("Minhas encomendas", "Retirada e problemas com entregas", Glyph.BOX) { controller.navigate(Route.PARCELS) }
