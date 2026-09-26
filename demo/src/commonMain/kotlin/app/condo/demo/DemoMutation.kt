package app.condo.demo

import app.condo.domain.*
import kotlin.random.Random
import kotlin.time.Instant

internal class DemoMutation(
    var snapshot: Snapshot,
    private val previous: DemoDocument,
    val now: Instant,
) {
    val codes = previous.codes.map { it.toDomain() }.toMutableList()
    val events = previous.events.toMutableSet()
    val operations = previous.operations.toMutableSet()
    fun id(prefix: String) = "$prefix-${now.toEpochMilliseconds()}-${Random.nextInt(100000, 999999)}"
    fun apply(command: Command): Outcome {
        var code: AccessCode? = null
        when (command) {
            is Command.ReportCollected -> reportCollected(command.parcelId)
            is Command.IssuePickupCode -> code = pickupCode(command.parcelId)
