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
