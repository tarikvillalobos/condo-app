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
                Muted("Preferências salvas no app. Serviço de push externo ainda não configurado.")
                TextButton({ controller.message(controller.platform.openNotificationSettings()) }) { Text("Configurações do sistema") }
            }
            Heading("Conta e segurança")
            listOf(Route.PROFILE_FORM, Route.SECURITY, Route.PRIVACY, Route.HELP).forEach { route ->
                MenuRow(route.title, glyph = if (route == Route.SECURITY) Glyph.LOCK else Glyph.USER) { controller.navigate(route) }
            }
            TextButton(controller::logout) { Text("Sair da conta", color = Tokens.danger) }
        }
        Route.PROFILE_FORM -> {
            FormField(controller, "profile.name", "Nome", session.account.name)
            FormField(controller, "profile.phone", "Telefone", session.account.phone)
            Muted("E-mail: ${session.account.email}. Alterações de e-mail dependem de verificação pela API.")
            PrimaryButton("Salvar dados", !state.submitting) {
                controller.saveAccount(state.forms["profile.name"] ?: session.account.name, state.forms["profile.phone"] ?: session.account.phone)
            }
        }
        Route.MEMBERS -> {
            Text(snapshot.membership.unit)
            snapshot.members.forEach { MenuRow(it.name, it.relationship, Glyph.USER) {
