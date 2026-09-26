package app.condo.presentation

import app.condo.demo.DemoRepository
import app.condo.domain.*
import app.condo.platform.PlatformServices
import kotlinx.coroutines.test.*
import kotlin.test.*
import kotlin.time.Instant

class TestServices : PlatformServices {
    val values = mutableMapOf<String, String>()
    override val store = object : LocalStore {
        override fun read(key: String) = values[key]
        override fun write(key: String, value: String) { values[key] = value }
        override fun remove(key: String) { values.remove(key) }
    }
    override val vault = object : SessionVault {
        override val available = true
        var value: String? = null
        override fun read() = value
