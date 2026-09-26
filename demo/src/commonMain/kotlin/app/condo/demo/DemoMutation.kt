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
            is Command.ConsumePickupCode -> consumePickup(command.payload)
            is Command.DepositParcel -> deposit(command.eventId, command.parcel)
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
                snapshot = snapshot.copy(bookings = snapshot.bookings.map {
                    if (it.id == booking.id) it.copy(cancelled = true) else it
                })
            }
            is Command.ReadNotice -> snapshot = snapshot.copy(notices = snapshot.notices.map {
                if (it.id == command.noticeId) it.copy(read = true) else it
            })
            is Command.CreateRequest -> {
                requireInput(command.subject.length in 3..120, "Informe um assunto de 3 a 120 caracteres.")
                requireInput(command.body.length in 8..3000, "Descreva a solicitação (8 a 3000 caracteres).")
                snapshot = snapshot.copy(requests = snapshot.requests + ServiceRequest(
                    id("request"), command.category, command.subject, command.body, now,
                ))
            }
            is Command.SaveVehicle -> {
                requireInput(command.vehicle.model.isNotBlank(), "Informe o modelo do veículo.")
                requireInput(command.vehicle.plate.length in 6..8, "Informe uma placa válida.")
                val vehicle = command.vehicle.copy(id = command.vehicle.id.ifBlank { id("car") })
                snapshot = snapshot.copy(vehicles = snapshot.vehicles.filterNot { it.id == vehicle.id } + vehicle)
            }
            is Command.SavePreferences -> snapshot = snapshot.copy(preferences = command.preferences)
        }
        snapshot = snapshot.copy(updatedAt = now)
        return Outcome(snapshot, code)
    }
    fun document() = previous.copy(
        rows = snapshot.toRows(), events = events, operations = operations,
        codes = codes.map { it.toDto() },
    )
    fun missing(): Nothing = throw AppFailure(FailureKind.DENIED, "Registro não encontrado neste contexto.")
}

internal fun requireModule(member: Membership, command: Command) {
    val module = when (command) {
        is Command.ReportCollected, is Command.IssuePickupCode, is Command.ApplyLockerEvent,
        is Command.ConsumePickupCode, is Command.DepositParcel -> Module.PARCELS
        is Command.SaveVisit, is Command.SetVisitStatus, is Command.IssueVisitCode,
        is Command.ConsumeVisitCode -> Module.VISITS
        is Command.SavePet, is Command.ReportPet -> Module.PETS
        is Command.Reserve, is Command.CancelBooking -> Module.BOOKINGS
        is Command.CreateRequest -> Module.SERVICES
        is Command.ReadNotice -> Module.NOTICES
        else -> null
    }
    if (module != null && module !in member.modules) {
        throw AppFailure(FailureKind.DENIED, "Este módulo não está disponível neste condomínio.")
    }
}
