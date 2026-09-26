package app.condo.platform

import app.condo.domain.SessionVault
import java.io.File
import java.util.concurrent.TimeUnit

/** macOS Keychain / Linux Secret Service. No plaintext credential fallback. */
class DesktopVault(private val service: String) : SessionVault {
    private val mac = System.getProperty("os.name").startsWith("Mac")
    private val secretTool = listOf("/usr/bin/secret-tool", "/usr/local/bin/secret-tool").firstOrNull { File(it).canExecute() }
    override val available = mac || secretTool != null
    private fun command(arguments: List<String>, input: String? = null): Pair<Int, String> = runCatching {
        val process = ProcessBuilder(arguments).redirectError(ProcessBuilder.Redirect.DISCARD).start()
        process.outputStream.bufferedWriter().use { it.write(input.orEmpty()) }
        val output = process.inputStream.bufferedReader().readText().trim()
        if (!process.waitFor(10, TimeUnit.SECONDS)) { process.destroyForcibly(); return@runCatching -1 to "" }
        process.exitValue() to output
    }.getOrDefault(-1 to "")
    override fun read(): String? {
        val result = if (mac) command(listOf("/usr/bin/security", "find-generic-password", "-s", service, "-a", "session", "-w"))
