package app.condo.demo

import app.condo.domain.*
import kotlin.random.Random
import kotlin.time.Instant

internal class DemoMutation(
    var snapshot: Snapshot,
    private val previous: DemoDocument,
    val now: Instant,
) {
    val codes = previous.codes.map { it.toDomain() }.toMutableList()
    val events = previous.events.toMutableSet()
    val operations = previous.operations.toMutableSet()
    fun id(prefix: String) = "$prefix-${now.toEpochMilliseconds()}-${Random.nextInt(100000, 999999)}"
    fun apply(command: Command): Outcome {
        var code: AccessCode? = null
        when (command) {
            is Command.ReportCollected -> reportCollected(command.parcelId)
            is Command.IssuePickupCode -> code = pickupCode(command.parcelId)
            is Command.ApplyLockerEvent -> lockerEvent(command.event)
            is Command.SaveVisit -> saveVisit(command.visit)
            is Command.SetVisitStatus -> visitStatus(command.visitId, command.status)
            is Command.IssueVisitCode -> code = visitCode(command.visitId)
            is Command.ConsumeVisitCode -> consumeVisit(command.payload)
            is Command.SavePet -> {
                val pet = command.pet
                requireInput(pet.name.isNotBlank() && pet.species.isNotBlank(), "Informe nome e espécie.")
                requireInput(pet.weight.toDoubleOrNull()?.let { it > 0 } == true, "Informe um peso válido.")
                val saved = pet.copy(id = pet.id.ifBlank { id("pet") })
                snapshot = snapshot.copy(pets = snapshot.pets.filterNot { it.id == saved.id } + saved)
            }
            is Command.ReportPet -> {
                requireInput(command.description.length in 8..1000, "Descreva o pet e o local (8 a 1000 caracteres).")
                snapshot = snapshot.copy(petAlerts = snapshot.petAlerts + PetAlert(id("alert"), command.description, now))
            }
            is Command.Reserve -> reserve(command)
            is Command.CancelBooking -> {
                val booking = snapshot.bookings.find { it.id == command.bookingId } ?: missing()
                requireInput(booking.startsAt > now, "Uma reserva passada não pode ser cancelada.")
