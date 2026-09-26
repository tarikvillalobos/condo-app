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
