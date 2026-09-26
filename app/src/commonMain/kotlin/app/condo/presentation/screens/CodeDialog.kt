package app.condo.presentation.screens

import app.condo.data.encodeQr
import app.condo.design.*
import app.condo.domain.*
import app.condo.presentation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun CodeDialog(controller: AppController, state: AppState) {
    val code = state.code ?: return
    var now by remember(code.payload) { mutableStateOf(controller.clock.now()) }
    LaunchedEffect(code.payload) { while (true) { delay(1000); now = controller.clock.now() } }
    val valid = now < code.expiresAt && !code.consumed && !state.stale
    AlertDialog(
        onDismissRequest = controller::dismissCode,
        title = { Text(if (code.ownerId.startsWith("pickup")) "Seu código de retirada" else "Convite de visita") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(Tokens.md)) {
                if (valid) {
                    QrCode(code.payload)
                    Text(code.numericCode.chunked(3).joinToString(" "), style = MaterialTheme.typography.headlineLarge,
                        modifier = Modifier.semantics { contentDescription = "Código numérico ${code.numericCode.toList().joinToString(" ")}" })
                    Text("Válido até ${code.expiresAt.fullLabel()}")
                    Muted("Código demonstrativo. Não abre lockers ou portarias reais.")
                    SecondaryButton("Copiar código") {
                        controller.platform.copy(code.numericCode)
                    }
                    if (code.ownerId.startsWith("visit")) {
                        SecondaryButton("Compartilhar convite") {
