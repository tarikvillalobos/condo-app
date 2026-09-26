@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
package app.condo.platform

import app.condo.domain.SessionVault
import kotlinx.cinterop.*
import platform.CoreFoundation.*
import platform.Security.*

class IosVault(private val service: String) : SessionVault {
    override val available = true
    private fun <T> query(block: (CFMutableDictionaryRef) -> T): T {
        val dictionary = CFDictionaryCreateMutable(null, 0, null, null)!!
        val serviceString = CFStringCreateWithCString(null, service, kCFStringEncodingUTF8)!!
        val account = CFStringCreateWithCString(null, "session", kCFStringEncodingUTF8)!!
        try {
            CFDictionarySetValue(dictionary, kSecClass, kSecClassGenericPassword)
            CFDictionarySetValue(dictionary, kSecAttrService, serviceString)
            CFDictionarySetValue(dictionary, kSecAttrAccount, account)
            return block(dictionary)
        } finally {
