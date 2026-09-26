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
            CFRelease(account)
            CFRelease(serviceString)
            CFRelease(dictionary)
        }
    }
    override fun read(): String? = query { query ->
        CFDictionarySetValue(query, kSecReturnData, kCFBooleanTrue)
        CFDictionarySetValue(query, kSecMatchLimit, kSecMatchLimitOne)
        memScoped {
            val result = alloc<CFTypeRefVar>()
            if (SecItemCopyMatching(query, result.ptr) != errSecSuccess) return@memScoped null
            val data = result.value?.reinterpret<__CFData>() ?: return@memScoped null
            try {
                CFDataGetBytePtr(data)?.readBytes(CFDataGetLength(data).toInt())?.decodeToString()
            } finally { CFRelease(data) }
        }
    }
    override fun write(value: String): Boolean = query { query ->
        SecItemDelete(query)
        val bytes = value.encodeToByteArray()
        bytes.usePinned { pinned ->
            val data = CFDataCreate(null, pinned.addressOf(0).reinterpret(), bytes.size.toLong())!!
            try {
                CFDictionarySetValue(query, kSecValueData, data)
                CFDictionarySetValue(query, kSecAttrAccessible, kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly)
                SecItemAdd(query, null) == errSecSuccess
            } finally { CFRelease(data) }
        }
    }
    override fun clear() { query { SecItemDelete(it) } }
}
