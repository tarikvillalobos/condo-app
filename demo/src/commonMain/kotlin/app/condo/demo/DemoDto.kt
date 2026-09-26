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
