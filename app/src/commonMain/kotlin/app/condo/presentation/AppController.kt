package app.condo.presentation

import app.condo.domain.*
import app.condo.platform.PlatformServices
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.time.Duration.Companion.days

class AppController(
    val repository: CondoRepository,
    val platform: PlatformServices,
    val clock: AppClock = SystemAppClock,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
) {
    private val mutable = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = mutable.asStateFlow()
    private var contextVersion = 0
    private var activeId: String? = null
    private var loadingJob: Job? = null
    init {
        platform.vault.read()?.let { reference ->
            runAction {
                val session = repository.restore(reference)
                mutable.update { it.copy(session = session) }
                switchMembership(session.memberships.first().id)
            }
        }
    }
    fun field(key: String, value: String) = mutable.update { it.copy(forms = it.forms + (key to value)) }
    fun filter(key: String, value: String) = mutable.update { it.copy(filters = it.filters + (key to value)) }
    fun clearMessage() = mutable.update { it.copy(message = null, error = null) }
    fun message(value: String) = mutable.update { it.copy(message = value) }
    fun navigate(route: Route, id: String? = null) {
        if (route.module != null && route.module !in state.value.snapshot?.membership?.modules.orEmpty()) {
            message("Este módulo não está disponível neste condomínio.")
            return
        }
        mutable.update {
            it.copy(destination = Destination(route, id), history = it.history + it.destination, code = null, showCode = false)
        }
