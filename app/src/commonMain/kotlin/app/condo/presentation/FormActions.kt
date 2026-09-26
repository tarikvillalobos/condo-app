package app.condo.presentation

import app.condo.domain.*
import kotlinx.datetime.*
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

fun AppController.formValue(key: String) = state.value.forms[key].orEmpty()
private fun AppController.parseAction(block: () -> Unit) {
    try { block() } catch (failure: AppFailure) { message(failure.message)
    } catch (_: IllegalArgumentException) { message("Confira os campos. Use datas AAAA-MM-DD e horários HH:MM.") }
}
private fun Instant.formDate() = toLocalDateTime(condominiumZone).date.toString()
fun AppController.beginVisit(visit: Visit? = null) {
    val start = visit?.startsAt ?: clock.now()
    val end = visit?.expiresAt ?: (start + 2.hours)
    mapOf(
        "visit.name" to visit?.name.orEmpty(), "visit.purpose" to (visit?.purpose ?: "Visita"),
        "visit.provider" to (visit?.provider ?: false).toString(),
        "visit.frequent" to (visit?.frequent ?: false).toString(),
        "visit.date" to start.formDate(), "visit.time" to start.timeLabel(),
        "visit.endDate" to end.formDate(), "visit.endTime" to end.timeLabel(),
    ).forEach { (key, value) -> field(key, value) }
    navigate(Route.VISIT_FORM, visit?.id)
}
fun AppController.submitVisit() = parseAction {
    fun instant(date: String, time: String) = LocalDateTime.parse(
        "${formValue(date)}T${formValue(time)}",
    ).toInstant(condominiumZone)
    val old = state.value.snapshot?.visits?.find { it.id == state.value.destination.id }
    val visit = Visit(
        old?.id.orEmpty(), formValue("visit.name"), formValue("visit.purpose"),
        formValue("visit.provider").toBoolean(),
        instant("visit.date", "visit.time"), instant("visit.endDate", "visit.endTime"),
        old?.status ?: VisitStatus.SCHEDULED, formValue("visit.frequent").toBoolean(),
    )
    execute(Command.SaveVisit(visit), "Visita salva na demonstração.") { back() }
}
fun AppController.beginPet(pet: Pet? = null) {
    mapOf(
