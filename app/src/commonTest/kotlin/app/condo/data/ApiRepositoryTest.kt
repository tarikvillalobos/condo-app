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
