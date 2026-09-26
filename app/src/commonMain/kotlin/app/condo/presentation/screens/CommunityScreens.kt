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
