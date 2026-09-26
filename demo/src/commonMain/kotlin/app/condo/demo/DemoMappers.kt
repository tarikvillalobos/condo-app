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
