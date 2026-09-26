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
