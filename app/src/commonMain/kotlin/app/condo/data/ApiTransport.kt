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

/** HTTPS transport for the versioned Condo Platform contract. */
data class ApiConfiguration(val baseUrl: String, val environment: String, val brandId: String = "condo") {
    init {
        require(baseUrl.startsWith("https://")) { "External API requires HTTPS." }
        require(environment in setOf("staging", "production"))
        require(brandId.isNotBlank())
    }
}
data class ApiResponseDto(val status: Int, val body: String)
class ApiTransport(engine: HttpClientEngine, private val config: ApiConfiguration) {
    internal val brandId get() = config.brandId
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
    internal suspend fun request(route: ApiRoutes.Route, bearer: String? = null, body: String? = null,
                        idempotencyKey: String? = null, ifMatch: String? = null): ApiResponseDto {
        val headers = buildMap {
            put("X-Brand-Id", config.brandId)
            put("Accept", "application/json")
            if (body != null) put("Content-Type", "application/json")
            if (bearer != null) put("Authorization", "Bearer $bearer")
            if (idempotencyKey != null) put("Idempotency-Key", idempotencyKey)
            if (ifMatch != null) put("If-Match", ifMatch)
        }
        return request(route.path, route.method, headers, body)
    }
    suspend fun request(
        contractPath: String,
        contractMethod: HttpMethod,
        contractHeaders: Map<String, String> = emptyMap(),
        contractBody: String? = null,
    ): ApiResponseDto {
        require(contractPath.startsWith('/') && !contractPath.startsWith("//"))
        return try {
            val response = client.request(config.baseUrl.trimEnd('/') + contractPath) {
                method = contractMethod
                contractHeaders.forEach { (name, value) -> header(name, value) }
                contractBody?.let { contentType(ContentType.Application.Json); setBody(it) }
            }
            val dto = ApiResponseDto(response.status.value, response.bodyAsText())
            when (dto.status) {
                in 200..299 -> dto
                401 -> throw AppFailure(FailureKind.EXPIRED, "Sessão expirada. Entre novamente.")
                403 -> throw AppFailure(FailureKind.DENIED, "Você não tem permissão para esta ação.")
                409 -> throw AppFailure(FailureKind.CONFLICT, "Os dados mudaram. Atualize e tente novamente.")
                else -> throw AppFailure(FailureKind.UNAVAILABLE, "Serviço indisponível (HTTP ${dto.status}).")
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: AppFailure) {
            throw failure
        } catch (_: Exception) {
            throw AppFailure(FailureKind.NETWORK, "Não foi possível conectar ao serviço.")
        }
    }
    fun <Dto, Entity> map(
        response: ApiResponseDto,
        serializer: DeserializationStrategy<Dto>,
        mapper: (Dto) -> Entity,
    ): Entity = try {
        mapper(json.decodeFromString(serializer, response.body))
    } catch (_: Exception) {
        throw AppFailure(FailureKind.UNAVAILABLE, "Resposta incompatível com o contrato da API.")
    }
    fun close() = client.close()
}
