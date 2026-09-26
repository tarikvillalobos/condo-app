package app.condo.platform

import app.condo.APP_ENVIRONMENT
import app.condo.BRAND_ID
import app.condo.domain.*
import java.awt.Desktop
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.Base64

class DesktopServices : PlatformServices {
    override val store = DesktopStore()
    override val vault = DesktopVault("app.condo.$BRAND_ID.$APP_ENVIRONMENT")
    override val biometricStatus = "Biometria não habilitada nesta versão desktop."
    override val notificationStatus = "Permissão de notificações: gerenciada pelo sistema operacional."
    override fun copy(text: String) { Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(text), null) }
    override fun share(text: String): String {
        copy(text)
        return "Convite copiado. Cole no aplicativo de mensagens de sua preferência."
    }
    override fun open(url: String): String = runCatching {
        require(URI(url).scheme in setOf("https", "mailto", "tel"))
        Desktop.getDesktop().browse(URI(url))
        "Link aberto no aplicativo do sistema."
    }.getOrDefault("Não foi possível abrir o link neste sistema.")
    override fun openNotificationSettings() = "Abra as configurações de notificações do seu sistema operacional. O serviço de push ainda não está configurado."
}
class DesktopStore(private val directory: Path = Path.of(
    System.getProperty("user.home"), ".condo-app", BRAND_ID, APP_ENVIRONMENT,
)) : LocalStore {
    private fun path(key: String): Path = directory.resolve(Base64.getUrlEncoder().withoutPadding().encodeToString(key.toByteArray()))
    override fun read(key: String): String? = path(key).takeIf(Files::exists)?.let(Files::readString)
    override fun write(key: String, value: String) {
        Files.createDirectories(directory)
        val target = path(key)
        val temporary = Files.createTempFile(directory, "write-", ".tmp")
        Files.writeString(temporary, value)
        Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
    }
    override fun remove(key: String) { Files.deleteIfExists(path(key)) }
}
