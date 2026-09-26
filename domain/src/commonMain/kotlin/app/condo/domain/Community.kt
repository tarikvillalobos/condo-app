package app.condo.domain

import kotlin.time.Instant

data class Facility(val id: String, val name: String, val description: String, val rules: String)
data class Booking(
    val id: String,
    val facilityId: String,
    val startsAt: Instant,
    val endsAt: Instant,
    val cancelled: Boolean = false,
)
fun Booking.overlaps(start: Instant, end: Instant): Boolean =
    !cancelled && startsAt < end && start < endsAt

data class Camera(
    val id: String,
    val name: String,
    val location: String,
    val online: Boolean,
