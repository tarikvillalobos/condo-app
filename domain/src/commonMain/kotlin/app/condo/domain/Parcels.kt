package app.condo.domain

import kotlin.time.Instant

enum class ParcelStatus { WAITING, MANUAL_REPORT, COLLECTED }
data class Parcel(
    val id: String,
    val carrier: String,
    val tracking: String?,
    val locker: String,
    val compartment: String,
    val receivedAt: Instant,
    val deadline: Instant,
    val status: ParcelStatus = ParcelStatus.WAITING,
    val collectedAt: Instant? = null,
)
data class ParcelMetrics(val received: Int, val pending: Int, val averageMinutes: Long?)
fun parcelMetrics(parcels: List<Parcel>, since: Instant, until: Instant): ParcelMetrics {
    val period = parcels.filter { it.receivedAt >= since && it.receivedAt < until }
    val durations = period.mapNotNull { parcel ->
        parcel.collectedAt?.takeIf {
            parcel.status == ParcelStatus.COLLECTED && it >= parcel.receivedAt && it < until
        }?.let { (it - parcel.receivedAt).inWholeMinutes }
    }
    return ParcelMetrics(
        period.size,
        parcels.count { it.status != ParcelStatus.COLLECTED },
        durations.takeIf { it.isNotEmpty() }?.average()?.toLong(),
    )
}
data class AccessCode(
    val ownerId: String,
    val payload: String,
    val numericCode: String,
    val expiresAt: Instant,
    val consumed: Boolean = false,
)
data class LockerEvent(
    val eventId: String,
    val parcelId: String,
    val occurredAt: Instant,
    val collected: Boolean,
)
