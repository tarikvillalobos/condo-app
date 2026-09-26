package app.condo.presentation.screens

import app.condo.design.*
import app.condo.domain.*
import app.condo.presentation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import kotlinx.datetime.*
import kotlin.time.Duration.Companion.hours

@Composable
fun BookingsScreen(controller: AppController, state: AppState) {
    val snapshot = state.snapshot!!
    val facility = snapshot.facilities.find { it.id == state.forms["booking.facility"] } ?: snapshot.facilities.firstOrNull()
    if (facility == null) { EmptyState("Nenhuma área disponível", "A administração ainda não disponibilizou espaços."); return }
    val today = controller.clock.now().toLocalDateTime(condominiumZone).date
    val page = state.forms["booking.page"]?.toIntOrNull() ?: 0
