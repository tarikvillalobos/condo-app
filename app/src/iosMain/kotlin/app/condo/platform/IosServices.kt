@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
package app.condo.platform

import app.condo.APP_ENVIRONMENT
import app.condo.BRAND_ID
import app.condo.domain.*
import platform.Foundation.*
import platform.UIKit.*
import platform.LocalAuthentication.*

class IosServices : PlatformServices {
    private val defaults = NSUserDefaults(suiteName = "app.condo.$BRAND_ID.$APP_ENVIRONMENT")
    override val store = object : LocalStore {
        override fun read(key: String): String? = defaults.stringForKey(key)
        override fun write(key: String, value: String) { defaults.setObject(value, key) }
        override fun remove(key: String) { defaults.removeObjectForKey(key) }
    }
    override val vault = IosVault("app.condo.$BRAND_ID.$APP_ENVIRONMENT")
    override val biometricStatus: String get() = if (LAContext().canEvaluatePolicy(LAPolicyDeviceOwnerAuthenticationWithBiometrics, null)) {
        "Biometria disponível no dispositivo; login biométrico aguarda integração de sessão."
    } else "Biometria indisponível ou não cadastrada neste dispositivo."
    override val notificationStatus = "Permissão de notificações: consulte os Ajustes do iOS."
    override fun copy(text: String) { UIPasteboard.generalPasteboard.string = text }
    override fun share(text: String): String {
        val root = UIApplication.sharedApplication.keyWindow?.rootViewController
            ?: return "Não foi possível abrir o compartilhamento."
        val sheet = UIActivityViewController(listOf(text), null)
        sheet.popoverPresentationController?.sourceView = root.view
        sheet.popoverPresentationController?.sourceRect = root.view.bounds
        root.presentViewController(sheet, true, null)
        return "Escolha um aplicativo para compartilhar."
    }
    override fun open(url: String): String {
        val target = NSURL.URLWithString(url) ?: return "Link inválido."
        if (target.scheme !in setOf("https", "mailto", "tel")) return "Link não permitido."
        UIApplication.sharedApplication.openURL(target, emptyMap<Any?, Any?>(), null)
        return "Link aberto pelo sistema."
    }
    override fun openNotificationSettings(): String {
        val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return "Abra os Ajustes do iOS."
        UIApplication.sharedApplication.openURL(url, emptyMap<Any?, Any?>(), null)
        return "Permissão controlada pelos Ajustes do iOS."
    }
}
