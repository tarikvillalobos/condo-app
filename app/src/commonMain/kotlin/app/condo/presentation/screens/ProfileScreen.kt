package app.condo.presentation.screens

import app.condo.design.*
import app.condo.domain.*
import app.condo.presentation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*

@Composable
fun ProfileScreen(controller: AppController, state: AppState) {
    val session = state.session!!
    val snapshot = state.snapshot!!
    when (state.destination.route) {
        Route.PROFILE -> {
            Panel {
                AppIcon(Glyph.USER)
                Heading(session.account.name)
                Muted(session.account.email)
                SecondaryButton("Editar perfil") { controller.navigate(Route.PROFILE_FORM) }
            }
            AdaptiveGrid(listOf(
                Triple(Route.MEMBERS, "${snapshot.members.size} na unidade", Glyph.PEOPLE),
                Triple(Route.VEHICLES, "${snapshot.vehicles.size} cadastrados", Glyph.CAR),
                Triple(Route.CONDOMINIUMS, "${session.memberships.size} vinculados", Glyph.BUILDING),
            )) { (route, subtitle, glyph) -> MenuRow(route.title, subtitle, glyph) { controller.navigate(route) } }
            Heading("Notificações")
            Panel {
                val prefs = snapshot.preferences
                PreferenceRow("Encomendas", "Avisos de novas entregas", prefs.parcels) {
                    controller.execute(Command.SavePreferences(prefs.copy(parcels = it)))
                }
                PreferenceRow("Visitas", "Entrada e chegada de visitantes", prefs.visits) {
                    controller.execute(Command.SavePreferences(prefs.copy(visits = it)))
                }
                PreferenceRow("Avisos do condomínio", "Comunicados da administração", prefs.notices) {
                    controller.execute(Command.SavePreferences(prefs.copy(notices = it)))
                }
                Muted(controller.platform.notificationStatus)
