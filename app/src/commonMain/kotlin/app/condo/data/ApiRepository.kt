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
