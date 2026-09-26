package app.condo.presentation.screens

import app.condo.design.*
import app.condo.domain.*
import app.condo.presentation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.days

@Composable
fun ParcelScreen(controller: AppController, state: AppState, expanded: Boolean) {
    val snapshot = state.snapshot ?: return
    val selected = state.destination.id?.let { id -> snapshot.parcels.find { it.id == id } }
    if (expanded) {
        Row(horizontalArrangement = Arrangement.spacedBy(Tokens.xl)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Tokens.lg)) { ParcelList(controller, state) }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Tokens.lg)) {
