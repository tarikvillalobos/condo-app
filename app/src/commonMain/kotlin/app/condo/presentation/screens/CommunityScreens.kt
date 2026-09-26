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
