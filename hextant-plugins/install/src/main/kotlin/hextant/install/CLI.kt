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
        run("gradle", *command)
    }

    fun java(vararg command: String) {
        run("java", *command)
    }

    private fun run(vararg command: String) {
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
        workingDirectory = workingDirectory.resolve(subDirectory)
        return workingDirectory.exists()
    }

    companion object {
        operator fun invoke(workingDirectory: File = File("/"), body: CLI.() -> Unit) {
            CLI(workingDirectory).body()
        }
    }
}

/*
java
--module-path D:\programs\Java\javafx-sdk-11.0.2\lib
--add-modules javafx.controls
--add-opens java.base/jdk.internal.loader=ALL-UNNAMED
-classpath C:\Users\Nikolaus Knop\hextant\launcher.jar;C:\Users\Nikolaus Knop\hextant\plugins\core.jar hextant.launcher.Main
 */