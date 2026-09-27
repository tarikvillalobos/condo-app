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
