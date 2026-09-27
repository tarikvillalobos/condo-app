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
    fun clear()
}
enum class Module(val label: String) {
    PARCELS("Encomendas"), CAMERAS("Câmeras"), VISITS("Visitas"),
    PETS("Pets"), BOOKINGS("Reservas"), NOTICES("Avisos"),
    SERVICES("Solicitações"), EVENTS("Agenda"), CONCIERGE("Portaria")
}
data class Account(val id: String, val name: String, val email: String, val phone: String)
data class Membership(
    val id: String,
    val name: String,
    val unit: String,
    val modules: Set<Module> = Module.entries.toSet(),
    val cameraAccess: Boolean = true,
    val recordingAccess: Boolean = false,
)
data class Session(val account: Account, val memberships: List<Membership>, val sessionReference: String? = null)
data class Preferences(
    val parcels: Boolean = true,
    val visits: Boolean = true,
    val notices: Boolean = true,
)
