package app.condo.demo

import app.condo.domain.*
import kotlin.time.Duration.Companion.days

internal fun DemoMutation.saveVisit(visit: Visit) {
    requireInput(visit.name.trim().length in 2..100, "Informe o nome do visitante.")
    requireInput(visit.expiresAt > visit.startsAt && visit.expiresAt > now, "Validade inválida.")
    requireInput(visit.expiresAt - visit.startsAt <= 7.days, "O convite pode durar até sete dias.")
    if (visit.id.isNotBlank()) {
        val previous = snapshot.visits.find { it.id == visit.id } ?: missing()
        requireInput(previous.status in setOf(VisitStatus.SCHEDULED, VisitStatus.AUTHORIZED), "Esta visita não pode ser editada.")
    }
    val saved = visit.copy(id = visit.id.ifBlank { id("visit") })
    snapshot = snapshot.copy(visits = snapshot.visits.filterNot { it.id == saved.id } + saved)
    codes.removeAll { it.ownerId == "visit:${saved.id}" }
}
internal fun DemoMutation.visitStatus(id: String, status: VisitStatus) {
    val visit = snapshot.visits.find { it.id == id } ?: missing()
    val allowed = when (status) {
