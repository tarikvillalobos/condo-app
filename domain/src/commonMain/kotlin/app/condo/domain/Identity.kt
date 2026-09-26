package app.condo.domain

import kotlin.time.Clock
import kotlin.time.Instant

fun interface AppClock {
    fun now(): Instant
}
object SystemAppClock : AppClock {
    override fun now(): Instant = Clock.System.now()
}
interface LocalStore {
    fun read(key: String): String?
    fun write(key: String, value: String)
    fun remove(key: String)
}
interface SessionVault {
    val available: Boolean
    fun read(): String?
    fun write(value: String): Boolean
