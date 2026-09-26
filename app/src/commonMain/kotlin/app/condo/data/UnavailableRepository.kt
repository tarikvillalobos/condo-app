package app.condo.data

import app.condo.domain.*

/** Production deliberately fails closed until a documented API adapter is supplied. */
class UnavailableRepository : CondoRepository {
    override val isDemo = false
    private fun unavailable(): Nothing = throw AppFailure(
        FailureKind.UNAVAILABLE,
        "Integração indisponível: a documentação e o ambiente da API externa ainda não foram configurados.",
    )
    override suspend fun login(identifier: String, password: String): Session = unavailable()
    override suspend fun restore(sessionReference: String): Session = unavailable()
    override suspend fun load(membershipId: String): Snapshot = unavailable()
    override suspend fun execute(membershipId: String, command: Command): Outcome = unavailable()
    override suspend fun updateAccount(name: String, phone: String): Account = unavailable()
    override suspend fun linkMembership(invitation: String): Session = unavailable()
    override suspend fun activate(invitation: String, name: String, password: String): Session = unavailable()
    override suspend fun recover(identifier: String): String = unavailable()
    override suspend fun changePassword(current: String, replacement: String): String = unavailable()
