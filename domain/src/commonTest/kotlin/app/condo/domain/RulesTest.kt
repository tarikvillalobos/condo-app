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
        assertNull(parcelMetrics(emptyList(), now, now + 1.hours).averageMinutes)
    }
    @Test fun adjacentBookingsDoNotOverlap() {
        val booking = Booking("1", "court", now, now + 1.hours)
        assertFalse(booking.overlaps(now + 1.hours, now + 2.hours))
        assertTrue(booking.overlaps(now, now + 2.hours))
        assertFalse(booking.copy(cancelled = true).overlaps(now, now + 1.hours))
    }
    @Test fun invitationRequiresActiveWindowAndStatus() {
        val visit = Visit("1", "Fictício", "Visita", false, now, now + 1.hours)
        assertTrue(visit.isUsable(now))
        assertFalse(visit.isUsable(now - 1.hours))
        assertFalse(visit.isUsable(now + 1.hours))
        assertFalse(visit.copy(status = VisitStatus.REVOKED).isUsable(now))
        assertFalse(visit.copy(status = VisitStatus.ENTERED).isUsable(now))
    }
    @Test fun loginValidatesIdentifiersWithoutTreatingCpfAsAuthentication() {
        assertFailsWith<AppFailure> { validateLogin("bad", "password") }
        assertFailsWith<AppFailure> { validateLogin("alex@condo.demo", "x") }
        validateLogin("00000000000", "password")
