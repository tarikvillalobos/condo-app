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
    }
    private fun createSession(user: String): Session {
        requireInput(user in setOf("alex", "bia"), "Sessão demonstrativa inválida.")
        val name = store.read("demo.profile.$user.name") ?: if (user == "alex") "Alex Exemplo" else "Bia Exemplo"
        val phone = store.read("demo.profile.$user.phone").orEmpty()
        val memberships = DemoSeed.memberships.map {
            if (user == "bia") it.copy(unit = "Bloco C · Apto 201") else it
        }.let { if (store.read("demo.link.$user") == "yes") it + linkedMembership else it }
        return Session(Account(user, name, "$user@condo.demo", phone), memberships).also { session = it }
    }
    override suspend fun restore(sessionReference: String): Session = mutex.withLock {
        available()
        val parts = sessionReference.split('|')
        requireInput(parts.size == 3 && parts[0] == "demo", "Sessão inválida.")
        val expires = parts[2].toLongOrNull() ?: 0
        if (clock.now().toEpochMilliseconds() >= expires) {
            throw AppFailure(FailureKind.EXPIRED, "Sessão expirada. Entre novamente.")
        }
        createSession(parts[1])
    }
    private fun membership(id: String): Membership = session?.memberships?.find { it.id == id }
        ?: throw AppFailure(FailureKind.DENIED, "Condomínio não vinculado a esta conta.")
    private fun key(id: String) = "demo.database.${session!!.account.id}.$id"
    private fun document(id: String): Pair<Snapshot, DemoDocument> {
        val member = membership(id)
        val initial = DemoSeed.snapshot(member, clock.now())
        val saved = store.read(key(id))?.let { demoJson.decodeFromString<DemoDocument>(it) }
        requireInput(saved == null || saved.version == 1, "Dados locais incompatíveis. Contate o suporte.")
        val doc = saved ?: DemoDocument(rows = initial.toRows())
        return doc.rows.restore(initial) to doc
    }
    override suspend fun load(membershipId: String): Snapshot = mutex.withLock {
        available()
        val (snapshot, doc) = document(membershipId)
        if (activeScenario == DemoScenario.EMPTY) return@withLock Snapshot(
            membership = snapshot.membership, facilities = snapshot.facilities, updatedAt = clock.now(),
        )
        store.write(key(membershipId), demoJson.encodeToString(doc))
        snapshot.copy(updatedAt = clock.now())
    }
    override suspend fun execute(membershipId: String, command: Command): Outcome = mutex.withLock {
        available()
        val (snapshot, doc) = document(membershipId)
        requireModule(snapshot.membership, command)
        val mutation = DemoMutation(snapshot, doc, clock.now())
        val outcome = mutation.apply(command)
        store.write(key(membershipId), demoJson.encodeToString(mutation.document()))
        outcome
    }
    override suspend fun updateAccount(name: String, phone: String): Account = mutex.withLock {
        available()
        requireInput(name.trim().length in 2..100, "Informe um nome entre 2 e 100 caracteres.")
        val current = session ?: throw AppFailure(FailureKind.EXPIRED, "Entre novamente.")
        store.write("demo.profile.${current.account.id}.name", name.trim())
        store.write("demo.profile.${current.account.id}.phone", phone)
        createSession(current.account.id).account
    }
    override suspend fun linkMembership(invitation: String): Session = mutex.withLock {
        available()
        requireInput(invitation == "VINCULAR-DEMO", "Convite demonstrativo inválido ou expirado.")
