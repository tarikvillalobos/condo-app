package app.condo.data

import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class ApiRepositoryTest {
    private val config = ApiConfiguration("https://api.example.invalid/v1", "staging", "condo")
    @Test fun passwordLoginUsesContractRouteBrandAndBearer() = runTest {
        val requests = mutableListOf<String>()
        val engine = MockEngine { request ->
            requests += "${request.method.value} ${request.url.encodedPath} ${request.headers["X-Brand-Id"]}"
            when (request.url.encodedPath) {
                "/v1/auth/password/login" -> {
                    assertEquals("condo", request.headers["X-Brand-Id"])
                    assertEquals("application/json", request.body.contentType?.toString())
                    respond("""{"brandId":"condo","accessToken":"access","refreshToken":"refresh"}""",
                        headers = headersOf(HttpHeaders.ContentType, "application/json"))
                }
                "/v1/me" -> {
                    assertEquals("Bearer access", request.headers[HttpHeaders.Authorization])
                    respond("""{"id":"user","name":"Ana","email":"ana@example.test","phone":""}""")
                }
                "/v1/me/memberships" -> respond("""{"items":[{"id":"member","locationName":"Condo","unitLabel":"101","modules":{"parcels":true}}]}""")
                else -> error("Unexpected route: ${request.url}")
            }
        }
        val transport = ApiTransport(engine, config)
        val session = ApiRepository(transport).login("ana@example.test", "password")
        assertEquals("Ana", session.account.name)
        assertEquals("member", session.memberships.single().id)
        assertEquals(listOf("POST /v1/auth/password/login condo", "GET /v1/me condo",
            "GET /v1/me/memberships condo"), requests)
        transport.close()
    }
    @Test fun routeParametersAreEncoded() {
        assertEquals("/memberships/a%2Fb/parcels/x%2Fy/pickup-credential", ApiRoutes.pickup("a/b", "x/y").path)
        assertEquals(HttpMethod.Post, ApiRoutes.cancelReservation("m", "r").method)
        assertEquals("/me/invitations/a%2Fb/link", ApiRoutes.link("a/b").path)
    }
    @Test fun snapshotFollowsCursorWithinMembership() = runTest {
        val paths = mutableListOf<String>()
        val engine = MockEngine { request ->
            val path = request.url.encodedPath + request.url.encodedQuery.let { if (it.isBlank()) "" else "?$it" }
            paths += path
            val response = when (request.url.encodedPath) {
                "/v1/auth/password/login" -> """{"brandId":"condo","accessToken":"a","refreshToken":"r"}"""
                "/v1/me" -> """{"id":"u","name":"Ana"}"""
                "/v1/me/memberships" -> """{"items":[{"id":"m","locationName":"Condo","unitLabel":"","modules":{"parcels":true}}]}"""
                "/v1/memberships/m/parcels" -> if (request.url.parameters["cursor"] == null)
                    """{"items":[{"id":"p1","carrier":"C","locker":{"name":"L"},"compartment":"1","depositedAt":"2026-09-01T00:00:00Z","deadline":"2026-10-01T00:00:00Z","status":"waiting"}],"pageInfo":{"nextCursor":"next"}}"""
                    else """{"items":[{"id":"p2","carrier":"C","locker":{"name":"L"},"compartment":"2","depositedAt":"2026-09-02T00:00:00Z","deadline":"2026-10-02T00:00:00Z","status":"waiting"}],"pageInfo":{"nextCursor":null}}"""
                else -> error("Unexpected path: $path")
            }
            respond(response)
        }
        val transport = ApiTransport(engine, config)
        val repository = ApiRepository(transport)
        repository.login("ana@example.test", "password")
        assertEquals(listOf("p1", "p2"), repository.load("m").parcels.map { it.id })
        assertTrue(paths.contains("/v1/memberships/m/parcels?cursor=next"))
        transport.close()
    }
    @Test fun rotatesTokenAndRetriesProtectedReadOnce() = runTest {
        var refreshes = 0
        val engine = MockEngine { request ->
            when (request.url.encodedPath) {
                "/v1/auth/password/login" -> respond("""{"brandId":"condo","accessToken":"old","refreshToken":"r1"}""")
                "/v1/auth/refresh" -> { refreshes++; respond("""{"brandId":"condo","accessToken":"new","refreshToken":"r2"}""") }
                "/v1/me" -> if (request.headers[HttpHeaders.Authorization] == "Bearer old")
                    respond("", HttpStatusCode.Unauthorized)
                    else respond("""{"id":"u","name":"Ana"}""")
                "/v1/me/memberships" -> respond("""{"items":[{"id":"m","locationName":"Condo","unitLabel":""}]}""")
                else -> error("Unexpected path: ${request.url}")
            }
        }
        val transport = ApiTransport(engine, config)
        assertEquals("Ana", ApiRepository(transport).login("ana@example.test", "password").account.name)
        assertEquals(1, refreshes)
        transport.close()
    }
}
