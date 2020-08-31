/**
 * @author Nikolaus Knop
 */

package hextant.main

import javafx.application.Application
import java.io.IOException
import kotlin.system.exitProcess

internal object Main {
    @JvmStatic fun main(args: Array<String>) {
        System.err.println(args.joinToString(" "))
        try {
            Application.launch(HextantApp::class.java, *args)
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

    internal fun fail(message: String): Nothing {
        System.err.println(message)
        exitProcess(1)
    }
}