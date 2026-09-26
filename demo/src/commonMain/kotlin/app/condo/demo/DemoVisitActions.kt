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
        VisitStatus.AUTHORIZED, VisitStatus.DENIED -> visit.status == VisitStatus.AT_GATE
        VisitStatus.REVOKED -> visit.status in setOf(VisitStatus.SCHEDULED, VisitStatus.AUTHORIZED)
        else -> false
    }
    requireInput(allowed && now < visit.expiresAt, "Esta visita não permite essa ação ou expirou.")
    snapshot = snapshot.copy(visits = snapshot.visits.map { if (it.id == id) it.copy(status = status) else it })
    if (status in setOf(VisitStatus.REVOKED, VisitStatus.DENIED)) codes.removeAll { it.ownerId == "visit:$id" }
}
internal fun DemoMutation.visitCode(id: String): AccessCode {
    val visit = snapshot.visits.find { it.id == id } ?: missing()
    requireInput(visit.status in setOf(VisitStatus.SCHEDULED, VisitStatus.AUTHORIZED), "Convite indisponível.")
    requireInput(now < visit.expiresAt, "Convite expirado.")
    return issueCode("visit:$id", visit.expiresAt)
}
internal fun DemoMutation.consumeVisit(payload: String) {
    val code = codes.find { it.payload == payload } ?: missing()
    requireInput(!code.consumed && now < code.expiresAt, "Convite expirado ou já utilizado.")
    val visit = snapshot.visits.find { "visit:${it.id}" == code.ownerId } ?: missing()
    requireInput(visit.isUsable(now), "Convite ainda não válido, revogado ou utilizado.")
    snapshot = snapshot.copy(visits = snapshot.visits.map {
        if (it.id == visit.id) it.copy(status = VisitStatus.ENTERED) else it
    })
    codes.replaceAllMatching(code.ownerId) { it.copy(consumed = true) }
}
