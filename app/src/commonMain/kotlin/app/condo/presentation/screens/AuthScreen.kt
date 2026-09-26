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
            AppIcon(Glyph.BUILDING, modifier = Modifier.size(56.dp))
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
                    Muted("Demonstração: use o convite PRIMEIRO-DEMO, válido para uma ativação local.")
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
