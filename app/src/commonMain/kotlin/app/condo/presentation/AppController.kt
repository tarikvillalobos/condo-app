package app.condo.presentation

import app.condo.domain.*
import app.condo.design.Brands
import app.condo.platform.PlatformServices
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration.Companion.days

class AppController(
    val repository: CondoRepository,
    val platform: PlatformServices,
    val clock: AppClock = SystemAppClock,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
    private val storageDispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    private val mutable = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = mutable.asStateFlow()
    private var contextVersion = 0
    private var navigationVersion = 0
    private var activeId: String? = null
    private var loadingJob: Job? = null
    private var restorationJob: Job? = null
    private var logoutJob: Job? = null
    private val actionJobs = mutableSetOf<Job>()
    private val vaultMutex = Mutex()
    init {
        runAction(restoring = true) { version, _ ->
            val reference = vault { platform.vault.read() } ?: return@runAction
            if (version != contextVersion) return@runAction
            val session = repository.restore(reference)
            if (version != contextVersion) return@runAction
            mutable.update { it.copy(session = session) }
            switchMembership(session.memberships.first().id)
        }
    }
    fun field(key: String, value: String) = mutable.update { it.copy(forms = it.forms + (key to value)) }
    fun filter(key: String, value: String) = mutable.update { it.copy(filters = it.filters + (key to value)) }
    fun clearMessage() = mutable.update { it.copy(message = null, error = null) }
    fun message(value: String) = mutable.update { it.copy(message = value) }
    fun navigate(route: Route, id: String? = null) {
        if (route.module != null && route.module !in state.value.snapshot?.membership?.modules.orEmpty().intersect(Brands.current.modules)) {
            message("Este módulo não está disponível neste condomínio.")
            return
        }
        navigationVersion++
        mutable.update {
            it.copy(destination = Destination(route, id), history = it.history + it.destination, code = null, showCode = false)
        }
    }
    fun back() {
        navigationVersion++
        mutable.update {
            it.copy(destination = it.history.lastOrNull() ?: Destination(), history = it.history.dropLast(1), code = null, showCode = false)
        }
    }
    fun login(identifier: String, password: String, remember: Boolean) = runAction {
        validateLogin(identifier, password)
        val session = repository.login(identifier.trim(), password)
        platform.vault.clear()
        if (remember) {
            val saved = platform.vault.write("demo|${session.account.id}|${(clock.now() + 7.days).toEpochMilliseconds()}")
            if (!saved) message("Armazenamento seguro indisponível. A sessão durará apenas enquanto o app estiver aberto.")
        }
        mutable.update { it.copy(session = session, forms = emptyMap(), destination = Destination(), history = emptyList()) }
        switchMembership(session.memberships.first().id)
    }
    fun switchMembership(id: String) {
        if (state.value.session?.memberships?.none { it.id == id } != false) return
        contextVersion++
        loadingJob?.cancel()
        activeId = id
        mutable.update { it.copy(snapshot = null, code = null, showCode = false, forms = emptyMap(),
            filters = emptyMap(), history = emptyList(), destination = Destination(), error = null, stale = false, submitting = false) }
        refresh()
    }
    fun refresh() {
        val id = activeId ?: return
        val version = contextVersion
        loadingJob?.cancel()
        loadingJob = scope.launch {
            mutable.update { it.copy(loading = true, error = null, code = null, showCode = false) }
            try {
                val snapshot = repository.load(id)
                if (version == contextVersion) mutable.update { it.copy(snapshot = snapshot, stale = false) }
            } catch (cancelled: CancellationException) { throw cancelled
            } catch (failure: Exception) {
                if (version == contextVersion) handle(failure)
            } finally {
                if (version == contextVersion) mutable.update { it.copy(loading = false) }
            }
        }
    }
    fun execute(command: Command, success: String? = null, after: (() -> Unit)? = null) = runAction {
        val id = activeId ?: return@runAction
        val version = contextVersion
        val outcome = repository.execute(id, command)
        if (version != contextVersion) return@runAction
        mutable.update { it.copy(snapshot = outcome.snapshot, code = outcome.code,
            showCode = outcome.code != null, stale = false, message = success) }
        after?.invoke()
    }
    fun dismissCode() = mutable.update { it.copy(showCode = false, code = null) }
    fun saveAccount(name: String, phone: String) = runAction {
        val account = repository.updateAccount(name, phone)
        mutable.update { it.copy(session = it.session?.copy(account = account), message = "Dados salvos na demonstração.") }
        back()
    }
    fun link(invitation: String) = runAction {
        val session = repository.linkMembership(invitation)
        mutable.update { it.copy(session = session, message = "Condomínio vinculado na demonstração.") }
    }
    fun activate(invitation: String, name: String, password: String) = runAction {
        val session = repository.activate(invitation, name, password)
        mutable.update { it.copy(session = session, forms = emptyMap(), destination = Destination()) }
        switchMembership(session.memberships.first().id)
    }
    fun recover(identifier: String) = runAction { message(repository.recover(identifier)) }
    fun password(current: String, replacement: String) = runAction {
        message(repository.changePassword(current, replacement))
        mutable.update { it.copy(forms = emptyMap()) }
    }
    fun scenario(value: DemoScenario) {
        repository.scenario(value)
        refresh()
    }
    fun logout() {
        contextVersion++
        loadingJob?.cancel()
        activeId = null
        platform.vault.clear()
        if (repository.isDemo) repository.scenario(DemoScenario.NORMAL)
        mutable.value = AppState()
        scope.launch { repository.logout() }
    }
    private fun runAction(block: suspend () -> Unit) {
        if (state.value.submitting) return
        mutable.update { it.copy(submitting = true, error = null) }
        val version = contextVersion
        scope.launch {
            try { block() } catch (cancelled: CancellationException) { throw cancelled
            } catch (failure: Exception) { if (version == contextVersion) handle(failure)
            } finally { if (version == contextVersion) mutable.update { it.copy(submitting = false) } }
        }
    }
    private fun handle(failure: Exception) {
        if (failure is AppFailure && failure.kind == FailureKind.EXPIRED) {
            logout()
            mutable.update { it.copy(error = failure.message) }
        } else mutable.update { it.copy(error = (failure as? AppFailure)?.message
            ?: "Não foi possível concluir. Tente novamente.", stale = it.snapshot != null, code = null, showCode = false) }
    }
    fun close() = scope.cancel()
}
