package app.condo.demo

import app.condo.domain.*
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

internal fun DemoMutation.reserve(command: Command.Reserve) {
    requireInput(command.operationId.isNotBlank(), "Identificador da operação ausente.")
    val signature = "${command.operationId}|${command.facilityId}|${command.startsAt}|${command.endsAt}"
    val previous = operations.find { it.substringBefore('|') == command.operationId }
    if (previous != null) {
        if (previous != signature) throw AppFailure(FailureKind.CONFLICT, "Operação repetida com dados diferentes.")
        return
    }
    requireInput(snapshot.facilities.any { it.id == command.facilityId }, "Espaço não encontrado.")
    requireInput(command.startsAt > now && command.startsAt <= now + 30.days, "Reserve de amanhã até 30 dias à frente.")
    val duration = command.endsAt - command.startsAt
    requireInput(duration > 0.hours && duration <= 4.hours, "A duração máxima é de quatro horas.")
    if (snapshot.bookings.any {
        it.facilityId == command.facilityId && it.overlaps(command.startsAt, command.endsAt)
