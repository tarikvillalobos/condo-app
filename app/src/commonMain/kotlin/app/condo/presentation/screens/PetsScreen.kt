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
            PrimaryButton("Salvar pet", !state.submitting, controller::submitPet)
        }
        Route.PET_ALERTS -> {
            Heading("Ajude a encontrar um amigo")
            FormField(controller, "pet.alert", "Descreva o pet, local e se está perdido ou foi encontrado", multiline = true)
            PrimaryButton("Publicar alerta demonstrativo", !state.submitting) {
                controller.execute(Command.ReportPet(state.forms["pet.alert"].orEmpty()), "Alerta salvo localmente na demonstração.")
            }
            snapshot.petAlerts.forEach { Panel { Text(it.description); Muted(it.createdAt.fullLabel()) } }
            if (snapshot.petAlerts.isEmpty()) EmptyState("Nenhum alerta ativo", "Alertas de pets perdidos e encontrados aparecerão aqui.", Glyph.PAW)
        }
        Route.PET_DETAIL -> {
            val pet = snapshot.pets.find { it.id == state.destination.id }
            if (pet != null) {
                PetCard(pet, controller.clock.now())
                Panel {
                    Text("Nascimento: ${pet.birthDate}")
                    Text("Microchip: ${pet.microchip.ifBlank { "Não informado" }}")
                    Text("Vacina: ${pet.vaccine.ifBlank { "Não informada" }}")
                    Text("Vencimento: ${pet.vaccineDue}")
                }
                PrimaryButton("Editar cadastro") { controller.beginPet(pet) }
            }
        }
        else -> {
            PrimaryButton("+ Cadastrar pet") { controller.beginPet() }
            if (snapshot.pets.isEmpty()) EmptyState("Seu pet tem espaço aqui", "Cadastre para acompanhar informações e vacinas.", Glyph.PAW)
            AdaptiveGrid(snapshot.pets) { pet ->
                Column(verticalArrangement = Arrangement.spacedBy(Tokens.sm)) {
                    SecondaryButton("Ver ${pet.name}") { controller.navigate(Route.PET_DETAIL, pet.id) }
                }
            }
            Heading("No condomínio")
            MenuRow("Pets perdidos e achados", "${snapshot.petAlerts.size} alertas ativos", Glyph.PAW) { controller.navigate(Route.PET_ALERTS) }
            MenuRow("Regras para pets", "Áreas permitidas, coleira e elevador", Glyph.SHIELD) {
                controller.message("Regras demonstrativas: use coleira nas áreas comuns, recolha os resíduos e utilize o elevador de serviço. Regras oficiais dependem do condomínio via API.")
            }
        }
    }
}
@Composable
private fun PetCard(pet: Pet) {
    Panel {
        AppIcon(Glyph.PAW)
        Heading(pet.name)
        Muted("${pet.species} · ${pet.breed}")
        Text("Porte ${pet.size} · ${pet.weight} kg")
        StatusChip("Vacina · vencimento ${pet.vaccineDue}", warning = true)
    }
}
