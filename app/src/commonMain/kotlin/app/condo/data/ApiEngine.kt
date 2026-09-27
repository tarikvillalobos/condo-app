package app.condo.data

import io.ktor.client.engine.HttpClientEngine

internal expect fun createApiEngine(): HttpClientEngine
