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
