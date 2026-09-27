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
        FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("${date.month.number.toString().padStart(2, '0')}/${date.year}", Modifier.align(Alignment.CenterVertically), style = MaterialTheme.typography.titleMedium, maxLines = 1)
            TextButton({ controller.field("booking.page", if (page == 0) "1" else "0") }) { Text(if (page == 0) "Próximas datas" else "Datas anteriores") }
        }
        AdaptiveGrid(dates, minimum = 52.dp, maximumColumns = 7) { day ->
            FilterChip(date == day, { controller.field("booking.date", day.toString()) },
                { Text(day.day.toString(), maxLines = 1) }, modifier = Modifier.heightIn(min = Tokens.touch))
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(Tokens.sm)) {
            listOf(10, 14, 18).forEach { startHour ->
                val starts = date.atTime(startHour, 0).toInstant(condominiumZone)
                val occupied = snapshot.bookings.any { it.facilityId == facility.id && it.overlaps(starts, starts + 4.hours) }
                FilterChip(hour == startHour, { controller.field("booking.hour", startHour.toString()) },
                    { Text("${startHour}h–${startHour + 4}h${if (occupied) " · ocupado" else ""}") }, enabled = !occupied)
            }
        }
        Text(facility.rules, style = MaterialTheme.typography.bodySmall)
        PrimaryButton(if (controller.repository.isDemo) "Confirmar reserva demonstrativa" else "Solicitar reserva", !state.submitting && !state.stale) {
            controller.reserve(facility.id, date.toString(), hour)
        }
        Muted(if (controller.repository.isDemo) "Disponibilidade local simulada." else "Horários sujeitos à disponibilidade confirmada pelo servidor.")
    }
    Heading("Áreas comuns")
    AdaptiveGrid(snapshot.facilities, minimum = 200.dp) { area ->
        MenuRow(area.name, area.description, Glyph.CALENDAR) { controller.field("booking.facility", area.id) }
    }
    Heading("Minhas reservas")
    val filter = state.filters["bookings"] ?: "Próximas"
    FilterChips(listOf("Próximas", "Histórico"), filter) { controller.filter("bookings", it) }
    val bookings = snapshot.bookings.filter {
        if (filter == "Próximas") !it.cancelled && it.endsAt > controller.clock.now()
        else it.cancelled || it.endsAt <= controller.clock.now()
    }
    if (bookings.isEmpty()) EmptyState("Nenhuma reserva neste período", "Escolha uma área e um horário para começar.", Glyph.CALENDAR)
    bookings.forEach { booking ->
        Panel {
            Heading(snapshot.facilities.find { it.id == booking.facilityId }?.name ?: "Área comum")
            Text("${booking.startsAt.fullLabel()} – ${booking.endsAt.timeLabel()}")
            StatusChip(if (booking.cancelled) "Cancelada" else if (controller.repository.isDemo) "Confirmada na demonstração" else "Reservada")
            if (!booking.cancelled && booking.startsAt > controller.clock.now()) {
                SecondaryButton("Cancelar reserva") { controller.execute(Command.CancelBooking(booking.id), "Reserva cancelada na demonstração.") }
            }
        }
    }
    SecondaryButton("Ver agenda de eventos") { controller.navigate(Route.EVENTS) }
}
