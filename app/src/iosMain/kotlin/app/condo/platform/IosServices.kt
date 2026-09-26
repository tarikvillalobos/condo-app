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
