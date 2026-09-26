package app.condo.domain

import kotlin.time.Instant

enum class VisitStatus { SCHEDULED, AT_GATE, AUTHORIZED, ENTERED, DENIED, REVOKED }
data class Visit(
    val id: String,
    val name: String,
    val purpose: String,
    val provider: Boolean,
    val startsAt: Instant,
    val expiresAt: Instant,
    val status: VisitStatus = VisitStatus.SCHEDULED,
    val frequent: Boolean = false,
)
fun Visit.isUsable(now: Instant): Boolean =
    now >= startsAt && now < expiresAt &&
        status in setOf(VisitStatus.SCHEDULED, VisitStatus.AUTHORIZED)

data class Pet(
    val id: String,
    val name: String,
    val species: String,
    val breed: String,
    val birthDate: String,
    val size: String,
    val weight: String,
    val microchip: String,
    val vaccine: String,
    val vaccineDue: String,
)
data class PetAlert(val id: String, val description: String, val createdAt: Instant)
