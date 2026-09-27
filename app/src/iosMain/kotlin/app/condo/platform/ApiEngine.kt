package app.condo.data

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin

internal actual fun createApiEngine(): HttpClientEngine = Darwin.create()
