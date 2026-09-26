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
    val permitted: Boolean = true,
)
data class Bulletin(
    val id: String,
    val title: String,
    val body: String,
    val date: Instant,
    val event: Boolean = false,
)
data class Notice(val id: String, val title: String, val target: String, val read: Boolean = false)
data class ServiceRequest(
    val id: String,
    val category: String,
    val subject: String,
    val description: String,
    val createdAt: Instant,
    val status: String = "Recebida na demonstração",
)
data class Vehicle(val id: String, val model: String, val plate: String)
data class UnitMember(val name: String, val relationship: String)
