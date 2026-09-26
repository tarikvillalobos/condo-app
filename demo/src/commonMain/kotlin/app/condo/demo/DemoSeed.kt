package app.condo.demo

import app.condo.domain.*
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

internal object DemoSeed {
    val memberships = listOf(
        Membership("aurora", "Residencial Jardim Aurora", "Bloco B · Apto 304"),
        Membership(
            "aguas", "Vila das Águas", "Bloco A · Apto 102",
            Module.entries.toSet() - Module.PETS, cameraAccess = false,
        ),
    )
    fun snapshot(membership: Membership, now: Instant): Snapshot = Snapshot(
        membership = membership,
        parcels = listOf(
            Parcel("p1", "Mercado Livre", "DEMO-001", "Portaria", "14 · M", now - 3.hours, now + 2.days),
            Parcel("p2", "Correios", null, "Portaria", "08 · P", now - 1.hours, now + 2.days),
