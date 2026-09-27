package app.condo.presentation

import app.condo.domain.*
import kotlinx.datetime.*
import kotlin.random.Random
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
    execute(Command.SaveVisit(visit), if (repository.isDemo) "Visita salva na demonstração." else "Visita salva.") { back() }
}
fun AppController.beginPet(pet: Pet? = null) {
    mapOf(
        "pet.name" to pet?.name.orEmpty(), "pet.species" to (pet?.species ?: "Cão"),
        "pet.breed" to pet?.breed.orEmpty(), "pet.birth" to (pet?.birthDate ?: "2024-01-01"),
        "pet.size" to (pet?.size ?: "Pequeno"), "pet.weight" to pet?.weight.orEmpty(),
        "pet.microchip" to pet?.microchip.orEmpty(), "pet.vaccine" to pet?.vaccine.orEmpty(),
        "pet.due" to (pet?.vaccineDue ?: clock.now().formDate()),
    ).forEach { (key, value) -> field(key, value) }
    navigate(Route.PET_FORM, pet?.id)
}
fun AppController.submitPet() = parseAction {
    val birth = LocalDate.parse(formValue("pet.birth"))
    requireInput(birth <= clock.now().toLocalDateTime(condominiumZone).date, "Nascimento não pode ser no futuro.")
    LocalDate.parse(formValue("pet.due"))
    val pet = Pet(state.value.destination.id.orEmpty(), formValue("pet.name"), formValue("pet.species"),
        formValue("pet.breed"), formValue("pet.birth"), formValue("pet.size"),
        formValue("pet.weight").replace(',', '.'), formValue("pet.microchip"), formValue("pet.vaccine"), formValue("pet.due"))
    execute(Command.SavePet(pet), if (repository.isDemo) "Pet salvo na demonstração." else "Pet salvo.") { back() }
}
fun AppController.submitRequest() = execute(Command.CreateRequest(
    formValue("request.category").ifBlank { "Solicitação" },
    formValue("request.subject"), formValue("request.body"),
), if (repository.isDemo) "Solicitação registrada localmente." else "Solicitação registrada.") { back() }
fun AppController.submitVehicle() {
    val id = formValue("vehicle.id").ifBlank { "vehicle-${clock.now().toEpochMilliseconds()}-${Random.nextLong()}" }
    field("vehicle.id", id)
    val submittedFields = state.value.forms
    execute(Command.SaveVehicle(Vehicle(id, formValue("vehicle.model"), formValue("vehicle.plate").uppercase())),
        if (repository.isDemo) "Veículo salvo na demonstração." else "Veículo salvo.") {
        if (state.value.forms == submittedFields) listOf("id", "model", "plate").forEach { field("vehicle.$it", "") }
    }
}
fun AppController.reserve(facility: String, date: String, hour: Int) = parseAction {
    val start = LocalDate.parse(date).atTime(hour, 0).toInstant(condominiumZone)
    val attempt = state.value.snapshot?.bookings?.count {
        it.facilityId == facility && it.startsAt == start && it.cancelled
    } ?: 0
    val operation = "booking:$facility:$date:$hour:$attempt"
    execute(Command.Reserve(facility, start, start + 4.hours, operation), "Reserva confirmada apenas na demonstração.")
}
