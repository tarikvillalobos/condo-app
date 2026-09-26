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
