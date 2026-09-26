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
        process.exitValue() to output
    }.getOrDefault(-1 to "")
    override fun read(): String? {
        val result = if (mac) command(listOf("/usr/bin/security", "find-generic-password", "-s", service, "-a", "session", "-w"))
        else secretTool?.let { command(listOf(it, "lookup", "service", service, "account", "session")) } ?: return null
        return result.second.takeIf { result.first == 0 && it.isNotBlank() }
    }
    override fun write(value: String): Boolean {
        if (!available) return false
        // Credentials are sent through stdin, never exposed as process arguments.
        if (mac) {
            require(value.all { it.isLetterOrDigit() || it in "|._:-" })
            val result = command(listOf("/usr/bin/security", "-i"),
                "add-generic-password -U -s $service -a session -w $value\n")
            return result.first == 0 && read() == value
        }
        return command(listOf(secretTool!!, "store", "--label=Condo App session", "service", service, "account", "session"), value).first == 0
    }
    override fun clear() {
        if (mac) command(listOf("/usr/bin/security", "delete-generic-password", "-s", service, "-a", "session"))
        else secretTool?.let { command(listOf(it, "clear", "service", service, "account", "session")) }
    }
}
