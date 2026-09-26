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
