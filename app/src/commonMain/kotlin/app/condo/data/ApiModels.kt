package app.condo.data

import app.condo.domain.*
import kotlinx.serialization.json.*
import kotlin.time.Instant

internal object ApiModels {
    val json = Json { ignoreUnknownKeys = true }
    fun parse(body: String): JsonObject = json.parseToJsonElement(body).jsonObject
    fun JsonObject.text(key: String): String = this[key]?.jsonPrimitive?.contentOrNull.orEmpty()
    fun JsonObject.optional(key: String): String? = this[key]?.jsonPrimitive?.contentOrNull
    fun JsonObject.value(key: String): JsonObject = this[key]?.jsonObject ?: JsonObject(emptyMap())
    fun JsonObject.items(key: String = "items"): List<JsonObject> = this[key]?.jsonArray?.map { it.jsonObject }.orEmpty()
    fun JsonObject.flag(key: String): Boolean = this[key]?.jsonPrimitive?.booleanOrNull == true
    fun JsonObject.whenAt(key: String): Instant? = optional(key)?.let(Instant::parse)
    fun JsonObject.number(key: String): Int = this[key]?.jsonPrimitive?.intOrNull ?: 0
    fun membership(o: JsonObject): Membership {
        val flags = o.value("modules")
        val available = if (flags.isEmpty()) {
            setOf(Module.PARCELS)
        } else buildSet {
            if (flags.flag("parcels")) add(Module.PARCELS)
            if (flags.flag("cameras")) add(Module.CAMERAS)
            if (flags.flag("visitors")) add(Module.VISITS)
            if (flags.flag("pets")) add(Module.PETS)
            if (flags.flag("reservations")) add(Module.BOOKINGS)
            if (flags.flag("announcements")) add(Module.NOTICES)
            if (flags.flag("requests")) add(Module.SERVICES)
            if (flags.flag("events")) add(Module.EVENTS)
        }
        val permissions = o["permissions"]?.jsonArray?.map { it.jsonPrimitive.content }.orEmpty()
        return Membership(o.text("id"), o.optional("condominiumName") ?: o.text("locationName"),
            o.text("unitLabel"), available, Module.CAMERAS in available,
            permissions.any { it.startsWith("cameras.recordings") })
    }
    fun account(o: JsonObject) = Account(o.text("id"), o.text("name"), o.text("email"), o.text("phone"))
    fun parcel(o: JsonObject) = Parcel(o.text("id"), o.text("carrier"), o.optional("tracking"),
        o.value("locker").text("name"), o.text("compartment"), o.whenAt("depositedAt")!!,
        o.whenAt("deadline")!!, when (o.text("status")) {
            "collected" -> ParcelStatus.COLLECTED; "manual" -> ParcelStatus.MANUAL_REPORT; else -> ParcelStatus.WAITING
        }, o.whenAt("collectedAt"))
    fun visit(o: JsonObject): Visit {
        val visitor = o.value("visitor")
        val status = when (o.text("status")) {
            "active" -> VisitStatus.AUTHORIZED; "used" -> VisitStatus.ENTERED
            "revoked" -> VisitStatus.REVOKED; else -> VisitStatus.SCHEDULED
        }
        return Visit(o.text("id"), visitor.text("name"), visitor.text("notes"),
            visitor.text("kind") == "service_provider", o.whenAt("validFrom")!!,
            o.whenAt("validUntil")!!, status)
    }
    fun pet(o: JsonObject) = Pet(o.text("id"), o.text("name"), o.text("species"), o.text("breed"),
        o.text("birthDate"), "", "", o.text("microchip"),
        o.items("vaccinations").firstOrNull()?.text("vaccine").orEmpty(),
        o.items("vaccinations").firstOrNull()?.text("nextDueAt").orEmpty())
    fun petAlert(o: JsonObject) = PetAlert(o.text("id"), o.text("description"), o.whenAt("createdAt")!!)
    fun facility(o: JsonObject) = Facility(o.text("id"), o.text("name"), o.text("description"), "")
    fun booking(o: JsonObject) = Booking(o.text("id"), o.value("space").text("id"),
        o.whenAt("startsAt")!!, o.whenAt("endsAt")!!, o.text("status") in setOf("cancelled", "rejected"), o.text("status"))
    fun camera(o: JsonObject) = Camera(o.text("id"), o.text("name"), o.text("area"),
        o.text("status") == "online", o.flag("liveAllowed"))
    fun announcement(o: JsonObject) = Bulletin(o.text("id"), o.text("title"), o.text("body"), o.whenAt("publishedAt")!!)
    fun event(o: JsonObject) = Bulletin(o.text("id"), o.text("title"), o.text("description"), o.whenAt("startsAt")!!, true)
    fun notice(o: JsonObject) = Notice(o.text("id"), o.text("title"), "", o.optional("readAt") != null)
    fun request(o: JsonObject) = ServiceRequest(o.text("id"), o.text("category"), o.text("title"),
        o.text("description"), o.whenAt("createdAt")!!, o.text("status"))
    fun vehicle(o: JsonObject) = Vehicle(o.text("id"), o.text("model"), o.text("plate"))
    fun code(o: JsonObject, owner: String) = AccessCode(owner, o.text("qrPayload"), o.text("code"), o.whenAt("expiresAt")!!)
    fun body(vararg fields: Pair<String, String>): String = buildJsonObject {
        fields.forEach { (key, value) -> put(key, value) }
    }.toString()
}
