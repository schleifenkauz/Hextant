/**
 *@author Nikolaus Knop
 */

package hextant.install

import hextant.install.OperatingSystem.*
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
        when (OperatingSystem.get()) {
            Windows -> run(workingDirectory.resolve("gradlew.bat").absolutePath, *command)
            Linux, Mac -> run("sh", "gradlew", *command)
        }
    }

    fun java(vararg command: String) {
        run("java", *command)
    }

    fun run(vararg command: String) {
        println("Running ${command.joinToString(" ")}")
        val proc = ProcessBuilder()
            .directory(workingDirectory)
            .command(*command)
            .start()
        runningProcesses.add(proc)
        proc.inputStream.transferTo(System.out)
        proc.errorStream.transferTo(System.err)
        val exitCode = proc.waitFor()
        runningProcesses.remove(proc)
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

        private val runningProcesses = mutableSetOf<Process>()

        fun destroyAllChildProcesses() {
            for (process in runningProcesses) process.destroy()
            runningProcesses.clear()
        }
    }
}