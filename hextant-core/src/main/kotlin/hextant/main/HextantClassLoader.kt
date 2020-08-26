/**
 * @author Nikolaus Knop
 */

package hextant.main

import hextant.context.Context
import hextant.main.GlobalDirectory.Companion.PLUGIN_CACHE
import java.net.URLClassLoader

internal class HextantClassLoader(private val context: Context, plugins: Collection<String>) :
    URLClassLoader(systemCL.urLs) {
    init {
        for (id in plugins) addPlugin(id)
    }

    fun addPlugin(id: String) {
        val file = context[GlobalDirectory][PLUGIN_CACHE].resolve(id)
        val url = file.toURI().toURL()
        addURL(url)
    }

    fun executeInNewThread(runnable: String, vararg arguments: Any): Thread {
        val cls = findClass(runnable)
        val cstr = cls.constructors.first()
        val inst = cstr.newInstance(*arguments) as Runnable
        val t = Thread(inst)
        t.contextClassLoader = this
        t.start()
        return t
    }

    companion object {
        private val systemCL = ClassLoader.getSystemClassLoader() as URLClassLoader
    }
}
