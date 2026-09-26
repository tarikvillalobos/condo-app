package app.condo.demo

import app.condo.domain.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.time.Instant

@Serializable
internal data class DemoRow(val type: String, val fields: Map<String, String>)
@Serializable
internal data class DemoDocument(
    val version: Int = 1,
    val rows: List<DemoRow>,
    val events: Set<String> = emptySet(),
    val operations: Set<String> = emptySet(),
    val codes: List<DemoCodeDto> = emptyList(),
)
@Serializable
internal data class DemoCodeDto(
    val owner: String,
    val payload: String,
    val number: String,
    val expires: String,
    val consumed: Boolean,
) {
    fun toDomain() = AccessCode(owner, payload, number, Instant.parse(expires), consumed)
}
internal fun AccessCode.toDto() = DemoCodeDto(
    ownerId, payload, numericCode, expiresAt.toString(), consumed,
)
internal val demoJson = Json { ignoreUnknownKeys = true }
internal fun row(type: String, vararg fields: Pair<String, Any?>) = DemoRow(
    type, fields.associate { (key, value) -> key to (value?.toString() ?: "") },
)
internal fun DemoRow.s(key: String) = fields[key].orEmpty()
internal fun DemoRow.time(key: String) = Instant.parse(s(key))
internal fun DemoRow.flag(key: String) = s(key).toBooleanStrict()
