package app.condo.data

import app.condo.domain.*
import app.condo.data.ApiModels.account
import app.condo.data.ApiModels.body
import app.condo.data.ApiModels.code
import app.condo.data.ApiModels.items
import app.condo.data.ApiModels.membership
import app.condo.data.ApiModels.number
import app.condo.data.ApiModels.optional
import app.condo.data.ApiModels.text
import app.condo.data.ApiModels.value
import io.ktor.http.*
import kotlinx.serialization.json.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/** Resident API adapter; only the secure session vault receives refresh credentials. */
@OptIn(ExperimentalUuidApi::class)
class ApiRepository(private val transport: ApiTransport, private val clock: AppClock = SystemAppClock,
                    private val vault: SessionVault? = null) : CondoRepository {
    override val isDemo = false
    private var tokens: JsonObject? = null
    private val refreshMutex = Mutex()
    private var currentSession: Session? = null
    private val access: String get() = tokens?.text("accessToken")?.takeIf(String::isNotBlank)
        ?: throw AppFailure(FailureKind.EXPIRED, "Sessão expirada. Entre novamente.")
    private fun key() = Uuid.random().toString()
    private suspend fun call(route: ApiRoutes.Route, payload: String? = null, mutation: Boolean = false,
                             ifMatch: String? = null): JsonObject {
        val idempotency = if (mutation) key() else null
        val oldAccess = access
        val response = try { transport.request(route, oldAccess, payload, idempotency, ifMatch) }
        catch (failure: AppFailure) {
            if (failure.kind != FailureKind.EXPIRED) throw failure
            refreshMutex.withLock {
                if (tokens?.text("accessToken") == oldAccess) refreshTokens()
            }
            transport.request(route, access, payload, idempotency, ifMatch)
        }
        return if (response.body.isBlank()) JsonObject(emptyMap()) else ApiModels.parse(response.body)
    }
    private suspend fun anonymous(route: ApiRoutes.Route, payload: String, mutation: Boolean = false): JsonObject {
        val response = transport.request(route, body = payload, idempotencyKey = if (mutation) key() else null)
        return ApiModels.parse(response.body)
    }
    private suspend fun refreshTokens() {
        val refreshToken = tokens?.text("refreshToken") ?: throw AppFailure(FailureKind.EXPIRED, "Sessão expirada.")
        val rotated = anonymous(ApiRoutes.refresh(), body("refreshToken" to refreshToken))
        requireInput(rotated.text("brandId") == transport.brandId, "Marca da sessão inválida.")
        tokens = rotated
        if (vault?.read() != null) vault.write(rotated.toString())
    }
    private suspend fun session(newTokens: JsonObject): Session {
        requireInput(newTokens.text("brandId") == transport.brandId, "Marca da sessão inválida.")
        tokens = newTokens
        val profile = call(ApiRoutes.profile())
        val memberships = call(ApiRoutes.memberships()).items().map(::membership)
