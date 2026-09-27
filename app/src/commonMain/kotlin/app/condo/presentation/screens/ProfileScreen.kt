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
                if (controller.repository.isDemo) SecondaryButton("Editar perfil") { controller.navigate(Route.PROFILE_FORM) }
            }
            AdaptiveGrid(listOf(
                Triple(Route.MEMBERS, "${snapshot.members.size} na unidade", Glyph.PEOPLE),
                Triple(Route.VEHICLES, "${snapshot.vehicles.size} cadastrados", Glyph.CAR),
                Triple(Route.CONDOMINIUMS, "${session.memberships.size} vinculados", Glyph.BUILDING),
            )) { (route, subtitle, glyph) -> MenuRow(route.title, subtitle, glyph) { controller.navigate(route) } }
            Heading("Notificações")
            Panel {
                val prefs = snapshot.preferences
                PreferenceRow("Encomendas", "Avisos de novas entregas", prefs.parcels, controller.repository.isDemo) {
                    controller.execute(Command.SavePreferences(prefs.copy(parcels = it)))
                }
                PreferenceRow("Visitas", "Entrada e chegada de visitantes", prefs.visits, controller.repository.isDemo) {
                    controller.execute(Command.SavePreferences(prefs.copy(visits = it)))
                }
                PreferenceRow("Avisos do condomínio", "Comunicados da administração", prefs.notices, controller.repository.isDemo) {
                    controller.execute(Command.SavePreferences(prefs.copy(notices = it)))
                }
                Muted(controller.platform.notificationStatus)
                Muted(if (controller.repository.isDemo) "Preferências salvas no app." else
                    "Preferências por assunto ainda não disponíveis nesta API.")
                TextButton({ controller.message(controller.platform.openNotificationSettings()) }) { Text("Configurações do sistema") }
            }
            Heading("Conta e segurança")
            (if (controller.repository.isDemo) listOf(Route.PROFILE_FORM, Route.SECURITY, Route.PRIVACY, Route.HELP)
                else listOf(Route.SECURITY, Route.PRIVACY, Route.HELP)).forEach { route ->
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
                controller.message("${it.name} · ${it.relationship}. Alterações de moradores exigem aprovação da administração pela API.")
            } }
        }
        Route.VEHICLES -> {
            snapshot.vehicles.forEach { vehicle ->
                MenuRow(vehicle.model, vehicle.plate, Glyph.CAR) {
                    controller.field("vehicle.id", vehicle.id)
                    controller.field("vehicle.model", vehicle.model)
                    controller.field("vehicle.plate", vehicle.plate)
                }
            }
            Heading("Cadastrar ou editar veículo")
            FormField(controller, "vehicle.model", "Modelo e cor")
            FormField(controller, "vehicle.plate", "Placa")
            PrimaryButton("Salvar veículo", !state.submitting, controller::submitVehicle)
        }
        Route.CONDOMINIUMS -> {
            Text("Escolha qual condomínio você quer ver agora")
            session.memberships.forEach { member ->
                MenuRow(member.name, member.unit, Glyph.BUILDING) { controller.switchMembership(member.id) }
            }
            Heading("Vincular outro condomínio")
            FormField(controller, "link.invitation", "Código do convite")
            if (controller.repository.isDemo) Muted("Convite demonstrativo: VINCULAR-DEMO")
            PrimaryButton("Vincular condomínio", !state.submitting) { controller.link(state.forms["link.invitation"].orEmpty()) }
        }
        Route.SECURITY -> {
            Heading("Alterar senha")
            FormField(controller, "security.current", "Senha atual", secret = true)
            FormField(controller, "security.new", "Nova senha", secret = true)
            PrimaryButton("Alterar senha", !state.submitting) { controller.password(state.forms["security.current"].orEmpty(), state.forms["security.new"].orEmpty()) }
            Heading("Biometria")
            Text(controller.platform.biometricStatus)
            Muted("Ativação de login biométrico depende do contrato de sessão da API. Nenhum dado biométrico é coletado pelo app.")
        }
        Route.PRIVACY -> {
            Heading("Seus dados, suas escolhas")
            Text(LocalBrand.current.privacy)
            Text(LocalBrand.current.terms)
            SecondaryButton("Solicitar acesso aos meus dados") { privacyRequest(controller, "Solicitação de acesso aos dados") }
            SecondaryButton("Solicitar exclusão dos meus dados") { privacyRequest(controller, "Solicitação de exclusão dos dados") }
        }
        Route.HELP -> {
            Heading("Como podemos ajudar?")
            MenuRow("Não consigo retirar uma encomenda", glyph = Glyph.BOX) {
                controller.message("Confira o prazo e solicite um código atualizado. Códigos demonstrativos não funcionam em equipamentos reais.")
            }
            MenuRow("Reservas e convites", glyph = Glyph.CALENDAR) {
                controller.message("Consulte a validade dos convites e o histórico de reservas. A confirmação real depende da API externa do condomínio.")
            }
            SecondaryButton("Abrir solicitação de suporte") { controller.navigate(Route.REQUEST_FORM) }
            Muted("${LocalBrand.current.name} · versão 0.1.0 · desenvolvimento")
        }
        Route.DEMO -> DemoControls(controller)
        else -> Unit
    }
}
private fun privacyRequest(controller: AppController, subject: String) {
    controller.field("request.category", "Privacidade")
    controller.field("request.subject", subject)
    controller.navigate(Route.REQUEST_FORM)
}
@Composable
private fun PreferenceRow(title: String, subtitle: String, checked: Boolean, enabled: Boolean, changed: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) { Text(title); Muted(subtitle) }
        Switch(checked, changed)
    }
}
@Composable
private fun DemoControls(controller: AppController) {
    Heading("Explore diferentes situações")
    Muted("Os cenários são executados dentro do app e não se conectam a um servidor.")
    SecondaryButton("Simular depósito de encomenda") {
        val now = controller.clock.now()
        val id = "deposit-${now.toEpochMilliseconds()}"
        controller.execute(Command.DepositParcel(id, Parcel(id, "Transportadora Demo", null,
            "Portaria", "09 · M", now, now + kotlin.time.Duration.parse("2d"))), "Depósito simulado registrado.")
    }
    listOf(
        DemoScenario.NORMAL to "Dados de exemplo",
        DemoScenario.EMPTY to "Listas vazias",
        DemoScenario.NETWORK_ERROR to "Falha de conexão",
        DemoScenario.ACCESS_DENIED to "Acesso negado",
        DemoScenario.SESSION_EXPIRED to "Sessão expirada",
    ).forEach { (scenario, label) -> SecondaryButton(label) { controller.scenario(scenario) } }
}
