package app.condo.platform

import app.condo.domain.*

interface PlatformServices {
    val store: LocalStore
    val vault: SessionVault
    val biometricStatus: String
    val notificationStatus: String
    fun copy(text: String)
    fun share(text: String): String
    fun open(url: String): String
    fun openNotificationSettings(): String
}
/** Video URLs and credentials must come from the documented external API. */
interface VideoAdapter {
    suspend fun connect(authorizedStreamUrl: String): VideoState
    fun close()
}
sealed interface VideoState {
    data object Connecting : VideoState
    data object Playing : VideoState
    data class Unavailable(val reason: String) : VideoState
}
class UnconfiguredVideoAdapter : VideoAdapter {
    override suspend fun connect(authorizedStreamUrl: String) = VideoState.Unavailable(
        "Nenhum fornecedor de vídeo foi configurado pela API externa.",
    )
    override fun close() = Unit
}
