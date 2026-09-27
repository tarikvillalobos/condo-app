package app.condo.presentation

import app.condo.domain.*
import kotlinx.datetime.*
import kotlin.time.Instant

val condominiumZone = TimeZone.of("America/Sao_Paulo")
fun Instant.dateLabel(): String {
    val value = toLocalDateTime(condominiumZone)
    return "${value.day.toString().padStart(2, '0')}/${value.month.number.toString().padStart(2, '0')}"
}
fun Instant.timeLabel(): String {
    val value = toLocalDateTime(condominiumZone)
    return "${value.hour.toString().padStart(2, '0')}:${value.minute.toString().padStart(2, '0')}"
}
fun Instant.fullLabel() = "${dateLabel()} às ${timeLabel()}"
fun Parcel.statusLabel() = when (status) {
    ParcelStatus.WAITING -> "Aguardando retirada"
    ParcelStatus.MANUAL_REPORT -> "Retirada informada · aguardando locker"
}
fun Visit.statusLabel(now: Instant): String = if (expiresAt <= now && status != VisitStatus.ENTERED) {
    "Expirado"
} else when (status) {
    VisitStatus.SCHEDULED -> "Agendada"
    VisitStatus.AT_GATE -> "Na portaria"
    VisitStatus.AUTHORIZED -> "Autorizado"
    VisitStatus.ENTERED -> "Entrada registrada"
    VisitStatus.DENIED -> "Recusada"
    VisitStatus.REVOKED -> "Convite revogado"
}

fun Pet.vaccineStatus(now: Instant): Pair<String, Boolean> {
    val today = now.toLocalDateTime(condominiumZone).date
    val due = runCatching { LocalDate.parse(vaccineDue) }.getOrNull()
        ?: return "Vencimento não informado" to true
    val days = today.daysUntil(due)
    return when {
        days < 0 -> "Vacina vencida há ${-days} dias" to true
        days <= 30 -> "Vacina vence em $days dias" to true
        else -> "Vacinas em dia · próxima em $vaccineDue" to false
    }
}
