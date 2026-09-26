package app.condo.data

import app.condo.domain.*
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json

/** Transport infrastructure only. Routes, DTOs and auth await the external contract. */
data class ApiConfiguration(val baseUrl: String, val environment: String) {
    init {
        require(baseUrl.startsWith("https://")) { "External API requires HTTPS." }
        require(environment in setOf("staging", "production"))
    }
}
data class ApiResponseDto(val status: Int, val body: String)
class ApiTransport(engine: HttpClientEngine, private val config: ApiConfiguration) {
    private val client = HttpClient(engine) {
        expectSuccess = false
        followRedirects = false
        install(HttpTimeout) {
            requestTimeoutMillis = 20_000
            connectTimeoutMillis = 10_000
            socketTimeoutMillis = 20_000
        }
    }
    private val json = Json { ignoreUnknownKeys = true }
    suspend fun request(
        contractPath: String,
        contractMethod: HttpMethod,
        contractHeaders: Map<String, String> = emptyMap(),
        contractBody: String? = null,
    ): ApiResponseDto {
        require(contractPath.startsWith('/') && !contractPath.startsWith("//"))
        return try {
