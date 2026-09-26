package app.condo.demo

import app.condo.domain.*
import kotlin.random.Random
import kotlin.time.Duration.Companion.minutes

internal fun DemoMutation.reportCollected(parcelId: String) {
    val parcel = snapshot.parcels.find { it.id == parcelId } ?: missing()
    requireInput(parcel.status != ParcelStatus.COLLECTED, "Esta encomenda já foi retirada.")
    snapshot = snapshot.copy(parcels = snapshot.parcels.map {
        if (it.id == parcelId) it.copy(status = ParcelStatus.MANUAL_REPORT) else it
    })
}
internal fun DemoMutation.pickupCode(parcelId: String): AccessCode {
    val parcel = snapshot.parcels.find { it.id == parcelId } ?: missing()
    requireInput(parcel.status != ParcelStatus.COLLECTED, "Esta encomenda já foi retirada.")
    requireInput(now < parcel.deadline, "Prazo encerrado. Procure a portaria.")
    return issueCode("pickup:$parcelId", minOf(now + 5.minutes, parcel.deadline))
}
internal fun DemoMutation.issueCode(owner: String, expiry: kotlin.time.Instant): AccessCode {
    codes.removeAll { it.ownerId == owner }
    return AccessCode(
        owner,
        "condo-demo:v1:${snapshot.membership.id}:$owner:${id("token")}",
        Random.nextInt(100000, 999999).toString(),
        expiry,
    ).also { codes.add(it) }
}
internal fun DemoMutation.lockerEvent(event: LockerEvent) {
    if (event.eventId in events) return
    val parcel = snapshot.parcels.find { it.id == event.parcelId } ?: missing()
    requireInput(event.occurredAt <= now, "Evento futuro inválido.")
    requireInput(event.occurredAt >= parcel.receivedAt, "Evento anterior ao depósito.")
    events.add(event.eventId)
    if (!event.collected || parcel.status == ParcelStatus.COLLECTED) return
    snapshot = snapshot.copy(parcels = snapshot.parcels.map {
        if (it.id == parcel.id) it.copy(status = ParcelStatus.COLLECTED, collectedAt = event.occurredAt) else it
    })
    codes.replaceAllMatching("pickup:${parcel.id}") { it.copy(consumed = true) }
}
internal fun MutableList<AccessCode>.replaceAllMatching(owner: String, transform: (AccessCode) -> AccessCode) {
    indices.forEach { index -> if (this[index].ownerId == owner) this[index] = transform(this[index]) }
}
