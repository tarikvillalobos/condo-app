package app.condo.data

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO

internal actual fun createApiEngine(): HttpClientEngine = CIO.create()
