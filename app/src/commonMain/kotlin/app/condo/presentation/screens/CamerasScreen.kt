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
