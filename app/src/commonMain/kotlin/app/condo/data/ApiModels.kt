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
        val available = if (flags.isEmpty()) Module.entries.toSet() else buildSet {
            if (flags.flag("parcels")) add(Module.PARCELS)
            if (flags.flag("cameras")) add(Module.CAMERAS)
            if (flags.flag("visitors")) add(Module.VISITS)
            if (flags.flag("pets")) add(Module.PETS)
            if (flags.flag("reservations")) add(Module.BOOKINGS)
            if (flags.flag("announcements")) add(Module.NOTICES)
            if (flags.flag("requests")) add(Module.SERVICES)
            if (flags.flag("events")) add(Module.EVENTS)
        }
        return Membership(o.text("id"), o.optional("condominiumName") ?: o.text("locationName"),
            o.text("unitLabel"), available, Module.CAMERAS in available, Module.CAMERAS in available)
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
