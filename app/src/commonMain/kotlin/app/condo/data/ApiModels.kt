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
