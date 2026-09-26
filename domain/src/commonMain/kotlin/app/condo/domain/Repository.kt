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
