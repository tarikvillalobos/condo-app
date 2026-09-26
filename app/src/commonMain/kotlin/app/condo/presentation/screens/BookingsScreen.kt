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
    val dates = (1..14).map { today.plus(it + page * 14, DateTimeUnit.DAY) }
    val date = state.forms["booking.date"]?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: dates.first()
    val hour = state.forms["booking.hour"]?.toIntOrNull() ?: 10
    Panel {
        Heading(facility.name)
        Muted("Selecione uma data · horário de Brasília")
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("${date.month.number.toString().padStart(2, '0')}/${date.year}", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
            TextButton({ controller.field("booking.page", if (page == 0) "1" else "0") }) { Text(if (page == 0) "Próximas datas" else "Datas anteriores") }
        }
        AdaptiveGrid(dates, minimum = 40.dp, maximumColumns = 7) { day ->
            FilterChip(date == day, { controller.field("booking.date", day.toString()) },
                { Text(day.day.toString()) }, modifier = Modifier.heightIn(min = Tokens.touch))
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(Tokens.sm)) {
            listOf(10, 14, 18).forEach { startHour ->
                val starts = date.atTime(startHour, 0).toInstant(condominiumZone)
                val occupied = snapshot.bookings.any { it.facilityId == facility.id && it.overlaps(starts, starts + 4.hours) }
                FilterChip(hour == startHour, { controller.field("booking.hour", startHour.toString()) },
                    { Text("${startHour}h–${startHour + 4}h${if (occupied) " · ocupado" else ""}") }, enabled = !occupied)
