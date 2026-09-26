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
