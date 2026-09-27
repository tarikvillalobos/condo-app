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
        val rotated = anonymous(ApiRoutes.refresh(), body("refreshToken" to refreshToken), mutation = true)
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
        return session(anonymous(ApiRoutes.refresh(), body("refreshToken" to refreshToken), mutation = true))
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
            is Command.ReportCollected -> call(ApiRoutes.manualPickup(membershipId, command.parcelId),
                mutation = true, ifMatch = "\"${version(ApiRoutes.item(membershipId, "parcels", command.parcelId))}\"")
            is Command.IssuePickupCode -> credential = code(call(ApiRoutes.pickup(membershipId, command.parcelId)), "pickup:${command.parcelId}")
            is Command.IssueVisitCode -> credential = code(call(ApiRoutes.credential(membershipId, command.visitId)), "visit:${command.visitId}")
            is Command.CancelBooking -> call(ApiRoutes.cancelReservation(membershipId, command.bookingId), mutation = true)
            is Command.ReadNotice -> call(ApiRoutes.action(membershipId, "inbox", command.noticeId, "read"))
            is Command.CreateRequest -> when (command.category) {
                "Privacidade" -> call(ApiRoutes.dataRequests(),
                    body("kind" to if (command.subject.contains("exclusão", true)) "deletion" else "export"), mutation = true)
                "Ocorrência" -> call(ApiRoutes.create(membershipId, "occurrences"),
                    buildJsonObject { put("category", "other"); put("description", command.body)
                        put("occurredAt", clock.now().toString()); put("anonymous", false) }.toString(), mutation = true)
                else -> call(ApiRoutes.create(membershipId, "requests"),
                    body("category" to "other", "title" to command.subject,
                        "description" to command.body), mutation = true)
            }
            is Command.Reserve -> call(ApiRoutes.create(membershipId, "reservations"),
                body("spaceId" to command.facilityId, "startsAt" to command.startsAt.toString(),
                    "endsAt" to command.endsAt.toString()), mutation = true)
            is Command.SaveVisit -> saveVisit(membershipId, command.visit)
            is Command.SetVisitStatus -> if (command.status == VisitStatus.REVOKED) {
                call(ApiRoutes.action(membershipId, "access-invites", command.visitId, "revoke"), mutation = true)
            } else unsupported()
            is Command.SavePet -> savePet(membershipId, command.pet)
            is Command.ReportPet -> call(ApiRoutes.create(membershipId, "pet-alerts"),
                body("kind" to command.kind, "description" to command.description, "species" to "other"), mutation = true)
            is Command.SaveVehicle -> saveVehicle(membershipId, command.vehicle)
            is Command.SavePreferences -> unsupported()
            is Command.ConsumePickupCode, is Command.DepositParcel, is Command.ApplyLockerEvent,
            is Command.ConsumeVisitCode -> unsupported()
        }
        return Outcome(load(membershipId), credential)
    }
    private suspend fun saveVisit(id: String, visit: Visit) {
        val payload = buildJsonObject {
            if (visit.id.isBlank()) putJsonObject("visitor") {
                put("name", visit.name); put("kind", if (visit.provider) "service_provider" else "visitor")
                put("notes", visit.purpose)
            }
            put("validFrom", visit.startsAt.toString()); put("validUntil", visit.expiresAt.toString())
            put("singleUse", true)
        }.toString()
        if (visit.id.isBlank()) call(ApiRoutes.create(id, "access-invites"), payload, mutation = true)
        else {
            val route = ApiRoutes.item(id, "access-invites", visit.id)
            val existing = call(route)
            val visitor = existing.value("visitor")
            call(ApiRoutes.patch(id, "visitors", visitor.text("id")),
                body("name" to visit.name, "notes" to visit.purpose), mutation = true,
                ifMatch = "\"${visitor.number("version")}\"")
            call(ApiRoutes.patch(id, "access-invites", visit.id), payload, mutation = true,
                ifMatch = "\"${existing.number("version")}\"")
        }
    }
    private suspend fun savePet(id: String, pet: Pet) {
        val species = when (pet.species.lowercase()) { "cão", "cao", "dog" -> "dog"; "gato", "cat" -> "cat"; "pássaro", "bird" -> "bird"; else -> "other" }
        val payload = buildJsonObject {
            put("name", pet.name)
            if (pet.id.isBlank()) { put("species", species); put("sex", "unknown") }
            if (pet.breed.isNotBlank()) put("breed", pet.breed)
            if (pet.birthDate.isNotBlank()) put("birthDate", pet.birthDate)
            if (pet.microchip.isNotBlank()) put("microchip", pet.microchip)
        }.toString()
        if (pet.id.isBlank()) call(ApiRoutes.create(id, "pets"), payload, mutation = true)
        else {
            val route = ApiRoutes.item(id, "pets", pet.id)
            call(ApiRoutes.patch(id, "pets", pet.id), payload, mutation = true, ifMatch = "\"${version(route)}\"")
        }
    }
    private suspend fun saveVehicle(id: String, vehicle: Vehicle) {
        val current = page(ApiRoutes.collection(id, "vehicles")).firstOrNull { it.text("id") == vehicle.id }
        val payload = body("plate" to vehicle.plate, "model" to vehicle.model, "kind" to "car")
        if (current == null) call(ApiRoutes.create(id, "vehicles"), payload, mutation = true)
        else {
            requireInput(current.text("plate") == vehicle.plate, "Para trocar a placa, cadastre outro veículo.")
            call(ApiRoutes.patch(id, "vehicles", vehicle.id), body("model" to vehicle.model),
                mutation = true, ifMatch = "\"${current.number("version")}\"")
        }
    }
    override suspend fun updateAccount(name: String, phone: String): Account = unsupported()
    override suspend fun linkMembership(invitation: String): Session {
        call(ApiRoutes.link(invitation), mutation = true)
        val renewed = session(tokens ?: unsupported())
        return renewed
    }
    override suspend fun activate(invitation: String, name: String, password: String): Session = unsupported()
    override suspend fun recover(identifier: String): String {
        val challenge = anonymous(ApiRoutes.recovery(), body("identifier" to identifier,
            "channel" to if (identifier.contains("@")) "email" else "sms"), mutation = true)
        return challenge.text("id")
    }
    override suspend fun verifyRecovery(challengeId: String, code: String, newPassword: String): Session =
        session(anonymous(ApiRoutes.verifyRecovery(challengeId),
            body("code" to code, "newPassword" to newPassword), mutation = true))
    override suspend fun changePassword(current: String, replacement: String): String {
        call(ApiRoutes.password(), body("currentPassword" to current, "newPassword" to replacement), mutation = true)
        return "Senha alterada."
    }
    override suspend fun logout() {
        if (tokens != null) try { call(ApiRoutes.logout(), mutation = true) }
        catch (_: AppFailure) { /* Local logout still completes. */ }
        finally { tokens = null; currentSession = null }
    }
    override fun scenario(value: DemoScenario) = Unit
}
