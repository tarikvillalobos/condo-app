package app.condo.presentation.screens

import app.condo.design.*
import app.condo.domain.*
import app.condo.presentation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(controller: AppController, state: AppState) {
    val snapshot = state.snapshot ?: return
    Text("Olá, ${state.session?.account?.name?.substringBefore(' ')}", style = MaterialTheme.typography.headlineMedium, color = LocalBrand.current.dark)
    MenuRow(snapshot.membership.name, snapshot.membership.unit, Glyph.BUILDING) { controller.navigate(Route.CONDOMINIUMS) }
    val pending = snapshot.parcels.filter { it.status != ParcelStatus.COLLECTED }
    if (state.allows(Route.PARCELS)) Surface(color = LocalBrand.current.dark, shape = Tokens.largeCorner) {
        Column(Modifier.padding(Tokens.page), verticalArrangement = Arrangement.spacedBy(Tokens.lg)) {
