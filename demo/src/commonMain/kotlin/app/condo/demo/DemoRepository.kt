package app.condo.demo

import app.condo.domain.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlin.random.Random
import kotlin.time.Duration.Companion.days

class DemoRepository(
    private val store: LocalStore,
    private val clock: AppClock,
    private val latencyMillis: Long = 180,
) : CondoRepository {
    override val isDemo = true
    private val mutex = Mutex()
    private var session: Session? = null
    private var activeScenario = DemoScenario.NORMAL
    private var password = "Demo1234!"
    override fun scenario(value: DemoScenario) { activeScenario = value }
    private suspend fun available() {
        delay(latencyMillis)
        when (activeScenario) {
            DemoScenario.NETWORK_ERROR -> throw AppFailure(FailureKind.NETWORK, "Sem conexão demonstrativa. Tente novamente.")
            DemoScenario.SESSION_EXPIRED -> throw AppFailure(FailureKind.EXPIRED, "Sua sessão expirou. Entre novamente.")
            DemoScenario.ACCESS_DENIED -> throw AppFailure(FailureKind.DENIED, "Acesso não autorizado neste cenário.")
            else -> Unit
        }
    }
    override suspend fun login(identifier: String, password: String): Session = mutex.withLock {
        available()
        validateLogin(identifier, password)
        requireInput(password == this.password, "Credenciais demonstrativas inválidas.")
        val user = when (identifier.lowercase()) {
            "alex@condo.demo", "00000000000" -> "alex"
            "bia@condo.demo" -> "bia"
            else -> throw AppFailure(FailureKind.DENIED, "Use uma conta demonstrativa indicada na tela.")
        }
        createSession(user)
