package app.condo.platform

import android.content.*
import android.app.NotificationManager
import android.provider.Settings
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import app.condo.APP_ENVIRONMENT
import app.condo.BRAND_ID
import app.condo.domain.*
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class AndroidServices(private val context: Context) : PlatformServices {
    private val preferences = context.getSharedPreferences("$BRAND_ID.$APP_ENVIRONMENT", Context.MODE_PRIVATE)
    override val store = object : LocalStore {
        override fun read(key: String) = preferences.getString(key, null)
        override fun write(key: String, value: String) { check(preferences.edit().putString(key, value).commit()) }
        override fun remove(key: String) { preferences.edit().remove(key).commit() }
    }
    override val vault = AndroidVault(store)
    override val biometricStatus = "Biometria depende de cadastro no sistema e integração de sessão com a API."
    override val notificationStatus: String get() {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return if (manager.areNotificationsEnabled()) "Permissão do sistema: notificações habilitadas."
        else "Permissão do sistema: notificações bloqueadas."
    }
    override fun copy(text: String) {
        (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("Condo App", text))
    }
    override fun share(text: String): String = runCatching {
        val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
        context.startActivity(Intent.createChooser(send, "Compartilhar convite").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        "Escolha um aplicativo para compartilhar."
    }.getOrDefault("Nenhum aplicativo disponível para compartilhar.")
    override fun open(url: String): String = runCatching {
        val uri = android.net.Uri.parse(url)
        require(uri.scheme in setOf("https", "tel", "mailto"))
        context.startActivity(Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        "Link aberto."
    }.getOrDefault("Não foi possível abrir o link.")
    override fun openNotificationSettings(): String {
        context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        return "Permissão controlada pelas configurações do Android."
    }
}
private class AndroidVault(private val store: LocalStore) : SessionVault {
    private val alias = "condo.$BRAND_ID.$APP_ENVIRONMENT.session"
    override val available = true
    private fun key(): SecretKey {
        val keystore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (keystore.getKey(alias, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build())
