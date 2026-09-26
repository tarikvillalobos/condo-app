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
