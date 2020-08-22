/**
 * @author Nikolaus Knop
 */

package hextant.main

import javafx.application.Application
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException
import kotlin.system.exitProcess

internal object Main {
    @JvmStatic fun main(args: Array<String>) {
        System.err.println(args.joinToString(" "))
        try {
            when (args.size) {
                0 -> launcher()
                1 -> {
                    val project = tryGetFile(args[0])
                    if (!project.exists()) fail("File with name $project does not exist")
                    open(project)
                }
                2 -> fail("Too many arguments")
            }
        } catch (io: IOException) {
            io.printStackTrace()
            fail("Unexpected IO error: ${io.message}")
        } catch (ex: Exception) {
            ex.printStackTrace()
            fail("Unexpected exception: ${ex.message}")
        } catch (err: Error) {
            err.printStackTrace()
            fail("Unexpected error: ${err.message}")
        }
    }

    fun tryGetFile(s: String): File = try {
        val f = File(s)
        f.canonicalPath
        f
    } catch (ex: IOException) {
        fail("Invalid path $s")
    }

    internal fun fail(message: String): Nothing {
        System.err.println(message)
        exitProcess(1)
    }

    private fun launcher() {
        Application.launch(HextantApp::class.java)
    }

    private fun open(project: File) {
        val txt = project.resolve("project.hxt").readText()
        val (plugins) = Json.decodeFromString<Project>(txt)
        val cl = HextantClassLoader(plugins)
        cl.executeInNewThread("hextant.main.ProjectOpener", project)
    }
}