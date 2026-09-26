package app.condo.presentation.screens

import app.condo.design.*
import app.condo.domain.*
import app.condo.presentation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun PetsScreen(controller: AppController, state: AppState) {
    val snapshot = state.snapshot!!
    when (state.destination.route) {
        Route.PET_FORM -> Column(Modifier.widthIn(max = Tokens.maxForm), verticalArrangement = Arrangement.spacedBy(Tokens.md)) {
            listOf("name" to "Nome", "species" to "Espécie", "breed" to "Raça",
                "birth" to "Nascimento · AAAA-MM-DD", "size" to "Porte", "weight" to "Peso em kg",
                "microchip" to "Microchip (opcional)", "vaccine" to "Vacina", "due" to "Vencimento da vacina · AAAA-MM-DD").forEach {
                FormField(controller, "pet.${it.first}", it.second)
            }
