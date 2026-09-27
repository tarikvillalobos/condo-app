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
        requireInput(memberships.isNotEmpty(), "Nenhum vínculo ativo para esta conta.")
        return Session(account(profile), memberships, newTokens.toString()).also { currentSession = it }
    }
    override suspend fun login(identifier: String, password: String) =
        session(anonymous(ApiRoutes.login(), body("identifier" to identifier, "password" to password)))
    override suspend fun restore(sessionReference: String): Session {
        val old = try { ApiModels.parse(sessionReference) } catch (_: Exception) {
            throw AppFailure(FailureKind.EXPIRED, "Sessão expirada. Entre novamente.")
        }
        val refreshToken = old.text("refreshToken")
        requireInput(refreshToken.isNotBlank(), "Sessão expirada. Entre novamente.")
        return session(anonymous(ApiRoutes.refresh(), body("refreshToken" to refreshToken)))
    }
    private suspend fun page(route: ApiRoutes.Route): List<JsonObject> {
        val all = mutableListOf<JsonObject>()
        var cursor: String? = null
        repeat(100) {
            val path = route.path + (cursor?.let { "?cursor=${it.encodeURLParameter()}" } ?: "")
            val result = call(route.copy(path = path))
            all += result.items()
            cursor = result.value("page").optional("nextCursor")
                ?: result.value("pageInfo").optional("nextCursor")
            if (cursor == null) return all
        }
        throw AppFailure(FailureKind.UNAVAILABLE, "A lista excedeu o limite de páginas suportado.")
    }
    override suspend fun load(membershipId: String): Snapshot {
        val m = currentSession?.memberships?.firstOrNull { it.id == membershipId }
            ?: throw AppFailure(FailureKind.DENIED, "Vínculo não autorizado.")
        suspend fun list(name: String) = page(ApiRoutes.collection(membershipId, name))
        val parcels = if (Module.PARCELS in m.modules) list("parcels").map(ApiModels::parcel) else emptyList()
        val invites = if (Module.VISITS in m.modules) list("access-invites").map(ApiModels::visit) else emptyList()
        val pets = if (Module.PETS in m.modules) list("pets").map(ApiModels::pet) else emptyList()
        val petAlerts = if (Module.PETS in m.modules) list("pet-alerts").map(ApiModels::petAlert) else emptyList()
        val spaces = if (Module.BOOKINGS in m.modules) list("spaces").map(ApiModels::facility) else emptyList()
        val bookings = if (Module.BOOKINGS in m.modules) list("reservations").map(ApiModels::booking) else emptyList()
        val cameras = if (Module.CAMERAS in m.modules) list("cameras").map(ApiModels::camera) else emptyList()
        val announcements = if (Module.NOTICES in m.modules) list("announcements").map(ApiModels::announcement) else emptyList()
        val events = if (Module.EVENTS in m.modules) list("events").map(ApiModels::event) else emptyList()
        val inbox = if (Module.NOTICES in m.modules) list("inbox").map(ApiModels::notice) else emptyList()
        val requests = if (Module.SERVICES in m.modules) list("requests").map(ApiModels::request) else emptyList()
        val vehicles = if (m.unit.isNotBlank()) list("vehicles").map(ApiModels::vehicle) else emptyList()
        val residents = if (m.unit.isNotBlank()) call(ApiRoutes.collection(membershipId, "unit"))
            .items("residents").map { UnitMember(it.text("name"), it.text("role")) } else emptyList()
        return Snapshot(m, parcels, invites, pets, petAlerts, spaces, bookings, cameras,
            announcements + events, inbox, requests, vehicles, residents, updatedAt = clock.now())
    }
    private fun unsupported(): Nothing = throw AppFailure(FailureKind.UNAVAILABLE,
        "Esta ação requer um fluxo ou permissão que o app ainda não oferece.")
    private suspend fun version(route: ApiRoutes.Route): String = call(route).number("version").toString()
    override suspend fun execute(membershipId: String, command: Command): Outcome {
        var credential: AccessCode? = null
        when (command) {
            is Command.ReportCollected -> call(ApiRoutes.manualPickup(membershipId, command.parcelId), mutation = true)
            is Command.IssuePickupCode -> credential = code(call(ApiRoutes.pickup(membershipId, command.parcelId)), "pickup:${command.parcelId}")
            is Command.IssueVisitCode -> credential = code(call(ApiRoutes.credential(membershipId, command.visitId)), "visit:${command.visitId}")
            is Command.CancelBooking -> call(ApiRoutes.cancelReservation(membershipId, command.bookingId), mutation = true)
            is Command.ReadNotice -> call(ApiRoutes.action(membershipId, "inbox", command.noticeId, "read"), mutation = true)
            is Command.CreateRequest -> when (command.category) {
                "Privacidade" -> call(ApiRoutes.dataRequests(),
