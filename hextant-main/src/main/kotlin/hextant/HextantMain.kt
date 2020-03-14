/**
 *@author Nikolaus Knop
 */

package hextant

import javafx.application.Application
import kotlin.system.exitProcess

object HextantMain {
    private fun require(condition: Boolean, message: String) {
        if (!condition) {
            fail(message)
        }
    }

    private fun fail(message: String): Nothing {
        System.err.println(message)
        exitProcess(1)
    }

    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    fun main(args: Array<String>) {
        require(args.size == 1, "No main class supplied")
        val mainCls = try {
            Class.forName(args[0])
        } catch (ex: ClassNotFoundException) {
            fail("Class ${args[0]} not found")
        }
        require(Application::class.java.isAssignableFrom(mainCls), "Main class is not a subclass of Application")
        Application.launch(mainCls as Class<out Application>)
    }
}