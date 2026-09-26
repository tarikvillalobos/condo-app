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
