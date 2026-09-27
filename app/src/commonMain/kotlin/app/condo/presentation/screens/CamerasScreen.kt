package app.condo.presentation.screens

import app.condo.design.*
import app.condo.domain.*
import app.condo.presentation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun CamerasScreen(controller: AppController, state: AppState) {
    val snapshot = state.snapshot!!
    if (!snapshot.membership.cameraAccess) {
        EmptyState("Acesso não autorizado", "Seu perfil não tem permissão para as câmeras deste condomínio.", Glyph.LOCK)
        return
    }
    if (state.destination.route == Route.CAMERA_DETAIL) {
        val camera = snapshot.cameras.find { it.id == state.destination.id }
        if (camera == null || !camera.permitted) { EmptyState("Acesso negado", "Câmera indisponível para esta conta.", Glyph.LOCK); return }
        if (!controller.repository.isDemo) {
            Heading(camera.name)
            EmptyState("Vídeo indisponível", "A transmissão desta câmera ainda não está configurada no app.", Glyph.CAMERA)
            return
        }
        var connecting by remember(camera.id) { mutableStateOf(true) }
        LaunchedEffect(camera.id) { delay(650); connecting = false }
        Heading(camera.name)
        if (connecting) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
            Text("Conectando à visualização demonstrativa…")
        } else if (!camera.online) EmptyState("Câmera indisponível", "O cenário selecionado está offline.", Glyph.CAMERA)
        else CameraPreview(camera.name)
        Muted("Esta imagem é ilustrativa. Transmissão real depende das URLs, credenciais e permissões fornecidas pela API.")
    } else {
        StatusChip("${snapshot.cameras.count { it.online }} câmeras online")
        val filter = state.filters["cameras"] ?: "Todas"
        FilterChips(listOf("Todas", "Portaria", "Garagem", "Lazer"), filter) { controller.filter("cameras", it) }
        val cameras = snapshot.cameras.filter { filter == "Todas" || it.location == filter }
        if (cameras.isEmpty()) EmptyState("Nenhuma câmera neste local", "Selecione outra localização.", Glyph.CAMERA)
        AdaptiveGrid(cameras, minimum = 220.dp, maximumColumns = 3) { camera ->
            Panel {
                else EmptyState("Offline", "Sinal indisponível", Glyph.CAMERA)
                Heading(camera.name)
                SecondaryButton("Visualizar câmera") { controller.navigate(Route.CAMERA_DETAIL, camera.id) }
            }
        }
    }
    SecondaryButton("Falar com a portaria") { controller.navigate(Route.CONCIERGE) }
    SecondaryButton("Gravações") {
        controller.message(if (!snapshot.membership.recordingAccess) "Seu perfil não tem permissão para gravações. Solicite acesso à administração."
        else "Gravações indisponíveis: a API externa ainda não forneceu catálogo e URLs autorizadas.")
    }
}
@Composable
private fun CameraPreview(name: String) {
    Box(Modifier.fillMaxWidth().aspectRatio(1.65f).background(Color(0xFFD1DAD3), Tokens.corner)) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(Color(0xFFA7B8AA), topLeft = Offset(0f, size.height * .62f), size = Size(size.width, size.height * .38f))
            drawRect(Color(0xFFDBDFDA), topLeft = Offset(size.width * .08f, size.height * .12f), size = Size(size.width * .6f, size.height * .5f))
            repeat(5) { index ->
                val x = size.width * (.1f + index * .115f)
                drawRect(Color(0xFF627A70), topLeft = Offset(x, size.height * .18f), size = Size(size.width * .08f, size.height * .36f))
            }
            drawCircle(Color(0xFF446454), size.width * .12f, Offset(size.width * .83f, size.height * .42f))
            drawLine(Color(0xFF6F7464), Offset(size.width * .83f, size.height * .45f), Offset(size.width * .83f, size.height * .8f), 7f)
        }
        Surface(color = Color(0xCC083933), modifier = Modifier.align(Alignment.TopStart).padding(Tokens.sm), shape = Tokens.controlCorner) {
            Text("SIMULAÇÃO", Modifier.padding(Tokens.sm), color = Color.White, style = MaterialTheme.typography.labelSmall)
        }
        Text(name, Modifier.align(Alignment.BottomStart).padding(Tokens.md), color = Color(0xFF10201C), style = MaterialTheme.typography.titleSmall)
    }
}
