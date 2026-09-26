package app.condo.domain

import kotlin.time.Instant

sealed interface Command {
    data class ReportCollected(val parcelId: String) : Command
    data class IssuePickupCode(val parcelId: String) : Command
    data class ApplyLockerEvent(val event: LockerEvent) : Command
    data class SaveVisit(val visit: Visit) : Command
    data class SetVisitStatus(val visitId: String, val status: VisitStatus) : Command
    data class IssueVisitCode(val visitId: String) : Command
    data class ConsumeVisitCode(val payload: String) : Command
    data class SavePet(val pet: Pet) : Command
    data class ReportPet(val description: String) : Command
    data class Reserve(
        val facilityId: String,
        val startsAt: Instant,
        val endsAt: Instant,
        val operationId: String,
    ) : Command
    data class CancelBooking(val bookingId: String) : Command
    data class ReadNotice(val noticeId: String) : Command
    data class CreateRequest(val category: String, val subject: String, val body: String) : Command
    data class SaveVehicle(val vehicle: Vehicle) : Command
    data class SavePreferences(val preferences: Preferences) : Command
}
data class Outcome(val snapshot: Snapshot, val code: AccessCode? = null)
enum class FailureKind { VALIDATION, NETWORK, EXPIRED, DENIED, CONFLICT, UNAVAILABLE }
class AppFailure(val kind: FailureKind, override val message: String) : Exception(message)
enum class DemoScenario { NORMAL, EMPTY, NETWORK_ERROR, SESSION_EXPIRED, ACCESS_DENIED }
interface CondoRepository {
    val isDemo: Boolean
    suspend fun login(identifier: String, password: String): Session
    suspend fun restore(sessionReference: String): Session
    suspend fun load(membershipId: String): Snapshot
    suspend fun execute(membershipId: String, command: Command): Outcome
    suspend fun updateAccount(name: String, phone: String): Account
    suspend fun linkMembership(invitation: String): Session
    suspend fun activate(invitation: String, name: String, password: String): Session
    suspend fun recover(identifier: String): String
