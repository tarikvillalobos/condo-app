package app.condo.demo

import app.condo.domain.*
import kotlin.time.Instant

internal fun Snapshot.toRows(): List<DemoRow> = buildList {
    parcels.forEach { value ->
        add(row("parcels",
            "id" to value.id,
            "carrier" to value.carrier,
            "tracking" to value.tracking,
            "locker" to value.locker,
            "compartment" to value.compartment,
            "receivedAt" to value.receivedAt,
            "deadline" to value.deadline,
            "status" to value.status,
            "collectedAt" to value.collectedAt,
        ))
    }
    visits.forEach { value ->
        add(row("visits",
            "id" to value.id,
            "name" to value.name,
            "purpose" to value.purpose,
            "provider" to value.provider,
            "startsAt" to value.startsAt,
            "expiresAt" to value.expiresAt,
            "status" to value.status,
            "frequent" to value.frequent,
        ))
    }
    pets.forEach { value ->
        add(row("pets",
            "id" to value.id,
            "name" to value.name,
            "species" to value.species,
            "breed" to value.breed,
            "birthDate" to value.birthDate,
            "size" to value.size,
            "weight" to value.weight,
            "microchip" to value.microchip,
            "vaccine" to value.vaccine,
            "vaccineDue" to value.vaccineDue,
        ))
    }
    petAlerts.forEach { value ->
        add(row("petAlerts",
            "id" to value.id,
            "description" to value.description,
            "createdAt" to value.createdAt,
        ))
    }
    bookings.forEach { value ->
        add(row("bookings",
            "id" to value.id,
            "facilityId" to value.facilityId,
            "startsAt" to value.startsAt,
            "endsAt" to value.endsAt,
            "cancelled" to value.cancelled,
        ))
    }
    requests.forEach { value ->
        add(row("requests",
            "id" to value.id,
            "category" to value.category,
            "subject" to value.subject,
            "description" to value.description,
            "createdAt" to value.createdAt,
            "status" to value.status,
        ))
    }
    notices.forEach { value ->
        add(row("notices",
            "id" to value.id,
            "title" to value.title,
            "target" to value.target,
            "read" to value.read,
        ))
    }
    vehicles.forEach { value ->
        add(row("vehicles",
            "id" to value.id,
            "model" to value.model,
            "plate" to value.plate,
        ))
    }
    add(row("preferences", "parcels" to preferences.parcels,
        "visits" to preferences.visits, "notices" to preferences.notices))
    add(row("metadata", "updatedAt" to updatedAt))
}

internal fun List<DemoRow>.restore(seed: Snapshot): Snapshot = seed.copy(
    parcels = filter { it.type == "parcels" }.map {
        Parcel(
            id = it.s("id"),
            carrier = it.s("carrier"),
            tracking = it.s("tracking").ifEmpty { null },
            locker = it.s("locker"),
            compartment = it.s("compartment"),
            receivedAt = it.time("receivedAt"),
            deadline = it.time("deadline"),
            status = ParcelStatus.valueOf(it.s("status")),
            collectedAt = it.s("collectedAt").takeIf(String::isNotEmpty)?.let(Instant::parse),
        )
    },
    visits = filter { it.type == "visits" }.map {
        Visit(
            id = it.s("id"),
            name = it.s("name"),
            purpose = it.s("purpose"),
            provider = it.flag("provider"),
            startsAt = it.time("startsAt"),
            expiresAt = it.time("expiresAt"),
            status = VisitStatus.valueOf(it.s("status")),
            frequent = it.flag("frequent"),
        )
    },
    pets = filter { it.type == "pets" }.map {
        Pet(
            id = it.s("id"),
