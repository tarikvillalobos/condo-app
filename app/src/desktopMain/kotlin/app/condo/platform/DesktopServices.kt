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
