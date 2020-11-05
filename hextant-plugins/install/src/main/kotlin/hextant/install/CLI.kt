/**
 *@author Nikolaus Knop
 */

package hextant.install

import java.io.File

class CLI private constructor(private var workingDirectory: File) {
    fun prompt(message: String): String? {
        println(message)
        return readLine()?.takeUnless { it.isBlank() }
    }

    fun git(vararg command: String) {
        run("git", *command)
    }

    fun gradle(vararg command: String) {
        val os = System.getProperty("os.name").toLowerCase()
        when {
            "windows" in os               -> run(workingDirectory.resolve("gradlew.bat").absolutePath, *command)
            "nux" in os                   -> run("sh", "gradlew", *command)
            "mac" in os || "darwin" in os -> run("sh", "gradlew", *command)
            else                          -> error("Unknown operating system: $os")
        }
    }

    fun java(vararg command: String) {
        run("java", *command)
    }

    private fun run(vararg command: String) {
        println("Running ${command.joinToString(" ")}")
        val exitCode = ProcessBuilder()
            .directory(workingDirectory)
            .command(*command)
            .inheritIO()
            .start()
            .waitFor()
        if (exitCode != 0) {
            val cmd = command.joinToString(separator = " ")
            error("Command $cmd finished with non zero")
        }
    }

    fun cd(subDirectory: String): Boolean {
        val wd = workingDirectory.resolve(subDirectory)
        return if (wd.exists()) {
            workingDirectory = wd
            true
        } else false
    }

    companion object {
        operator fun invoke(workingDirectory: File = File("/"), body: CLI.() -> Unit) {
            CLI(workingDirectory).body()
        }
    }
}