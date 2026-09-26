package app.condo.domain

import kotlin.test.*
import kotlin.time.Instant
import kotlin.time.Duration.Companion.hours

class RulesTest {
    private val now = Instant.parse("2026-09-26T12:00:00Z")
    @Test fun metricsIgnoreManualInvalidAndOutsidePeriod() {
        val base = Parcel("1", "Teste", null, "L", "1", now - 4.hours, now + 4.hours)
        val items = listOf(
            base.copy(status = ParcelStatus.COLLECTED, collectedAt = now - 2.hours),
            base.copy(id = "2", status = ParcelStatus.MANUAL_REPORT, collectedAt = now),
            base.copy(id = "3", status = ParcelStatus.COLLECTED, collectedAt = now - 5.hours),
            base.copy(id = "4", receivedAt = now - 48.hours),
        )
        val metrics = parcelMetrics(items, now - 24.hours, now + 1.hours)
        assertEquals(3, metrics.received)
        assertEquals(2, metrics.pending)
        assertEquals(120, metrics.averageMinutes)
