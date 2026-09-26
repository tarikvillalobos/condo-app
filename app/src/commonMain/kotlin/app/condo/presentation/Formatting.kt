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
    ParcelStatus.COLLECTED -> "Retirada confirmada no simulador"
