package app.condo.data

import app.condo.domain.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlin.test.*

class ApiTransportTest {
    private val config = ApiConfiguration("https://contract.example.invalid", "staging")
    @Serializable private data class FixtureDto(val value: String)
    @Test fun mapsControlledResponseWithoutDefiningProductionContract() = runTest {
        val transport = ApiTransport(MockEngine { respond("""{"value":"fixture"}""") }, config)
        val response = transport.request("/test-fixture", HttpMethod.Get)
        assertEquals("FIXTURE", transport.map(response, FixtureDto.serializer()) { it.value.uppercase() })
        transport.close()
    }
    @Test fun expiredAndDeniedStatusesAreExplicitAndNeverRetried() = runTest {
        var requests = 0
        val transport = ApiTransport(MockEngine { requests++; respond("", HttpStatusCode.Unauthorized) }, config)
        val failure = assertFailsWith<AppFailure> { transport.request("/test-fixture", HttpMethod.Post, contractBody = "test") }
        assertEquals(FailureKind.EXPIRED, failure.kind)
        assertEquals(1, requests)
        transport.close()
    }
    @Test fun cancelsInFlightRequests() = runTest {
        val transport = ApiTransport(MockEngine { delay(10_000); respond("ok") }, config)
        val job = launch { transport.request("/test-fixture", HttpMethod.Get) }
        delay(1)
        job.cancelAndJoin()
        assertTrue(job.isCancelled)
        transport.close()
    }
    @Test fun invalidBodyDoesNotLeakResponseContents() {
        val transport = ApiTransport(MockEngine { respond("") }, config)
        val failure = assertFailsWith<AppFailure> {
            transport.map(ApiResponseDto(200, "private-secret"), FixtureDto.serializer()) { it.value }
        }
        assertFalse(failure.message.contains("private-secret"))
        transport.close()
    }
    @Test fun externalConnectionsRequireTls() {
        assertFailsWith<IllegalArgumentException> { ApiConfiguration("http://external.invalid", "production") }
    }
}
