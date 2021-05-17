/**
 *@author Nikolaus Knop
 */

package hextant.cli

import hextant.cli.OperatingSystem.*
import java.io.File
import java.lang.ProcessBuilder.Redirect.INHERIT

class CLI private constructor(private var workingDirectory: File) {
    fun prompt(message: String): String? {
        println(message)
        return readLine()?.takeUnless { it.isBlank() }
    }

    fun git(vararg command: String): ProcessHandle = run("git", *command)

    fun gradle(vararg command: String): ProcessHandle = when (OperatingSystem.get()) {
        Windows -> run(workingDirectory.resolve("gradlew.bat").absolutePath, *command)
        Linux, Mac -> run("sh", "gradlew", *command)
    }

    fun java(vararg command: String): ProcessHandle = run("java", *command)

    fun run(vararg command: String): ProcessHandle {
        println("Running ${command.joinToString(" ")}")
        val proc = ProcessBuilder()
            .directory(workingDirectory)
            .inheritIO()
            .command(*command)
            .start()
        runningProcesses.add(proc)
        val handle = ProcessHandle(command.joinToString(" "), proc)
        proc.onExit().whenComplete { _, exc ->
            if (exc != null && !handle.isJoined) {
                val cmd = command.joinToString(separator = " ")
                System.err.println("Command $cmd finished with non-zero exit code")
            }
            runningProcesses.remove(proc)
        }
        return handle
    }

    fun cd(subDirectory: String): Boolean {
        val wd = workingDirectory.resolve(subDirectory)
        return if (wd.exists()) {
            workingDirectory = wd
            true
        } else false
    }

    val sep get() = File.pathSeparatorChar

    companion object {
        operator fun <R> invoke(workingDirectory: File = File("/"), body: CLI.() -> R): R {
            return CLI(workingDirectory).body()
        }

        private val runningProcesses = mutableSetOf<Process>()

        fun destroyAllChildProcesses() {
            for (process in runningProcesses) process.destroy()
            runningProcesses.clear()
        }
    }
}