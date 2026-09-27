package app.condo.presentation.screens

import app.condo.design.*
import app.condo.presentation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp

@Composable
fun AuthScreen(controller: AppController, state: AppState) {
    val route = state.destination.route
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        Column(Modifier.widthIn(max = Tokens.maxForm).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Tokens.lg)) {
            Spacer(Modifier.height(36.dp))
            AppIcon(LocalBrand.current.logo, modifier = Modifier.size(56.dp))
            Text(if (route in listOf(Route.RECOVERY, Route.ACTIVATE)) route.title else "Bem-vindo de volta",
                style = MaterialTheme.typography.headlineMedium, color = LocalBrand.current.dark)
            Text("Entre para acompanhar encomendas, visitas e tudo do seu condomínio.", color = Tokens.secondary)
            when (route) {
                Route.RECOVERY -> {
                    FormField(controller, "identity", "CPF ou e-mail")
                    PrimaryButton("Recuperar acesso", !state.submitting) { controller.recover(state.forms["identity"].orEmpty()) }
                    SecondaryButton("Voltar ao login", controller::back)
                }
                Route.ACTIVATE -> {
                    if (controller.repository.isDemo) Muted("Demonstração: use o convite PRIMEIRO-DEMO, válido para uma ativação local.")
                    FormField(controller, "invitation", "Código do convite")
                    FormField(controller, "name", "Seu nome")
                    FormField(controller, "password", "Crie uma senha", secret = true)
                    PrimaryButton("Ativar cadastro", !state.submitting) {
                        controller.activate(state.forms["invitation"].orEmpty(), state.forms["name"].orEmpty(), state.forms["password"].orEmpty())
                    }
                    SecondaryButton("Voltar ao login", controller::back)
                }
                else -> {
                    FormField(controller, "identity", "CPF ou e-mail")
                    FormField(controller, "password", "Senha", secret = true)
                    val remember = state.forms["remember"] == "true"
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(remember, { controller.field("remember", it.toString()) }, enabled = controller.platform.vault.available)
                        Text("Manter conectado", Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                    }
                    if (!controller.platform.vault.available) Muted("Sessão persistente indisponível: armazenamento seguro não encontrado neste dispositivo.")
                    TextButton({ controller.navigate(Route.RECOVERY) }) { Text("Esqueci a senha") }
                    PrimaryButton(if (state.submitting) "Entrando…" else "Entrar", !state.submitting) {
                        controller.login(state.forms["identity"].orEmpty(), state.forms["password"].orEmpty(), remember)
                    }
                    SecondaryButton("Primeiro acesso? Ative seu cadastro") { controller.navigate(Route.ACTIVATE) }
                    if (controller.repository.isDemo) Panel(color = Tokens.tint) {
                        Heading("Explore a demonstração")
                        Text("alex@condo.demo")
                        Text("Senha: Demo1234!")
                        Muted("Dados fictícios. Sem conexão com condomínios, portarias ou equipamentos reais.")
                        TextButton({ controller.login("alex@condo.demo", "Demo1234!", false) }) { Text("Entrar na demonstração") }
                    }
                }
            }
            Muted(LocalBrand.current.institution)
        }
    }
}
