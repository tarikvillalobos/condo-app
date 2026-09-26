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
