/**
 * @author Nikolaus Knop
 */

package hextant.main

import java.net.URLClassLoader

class HextantClassLoader(plugins: Collection<String>) : URLClassLoader(systemCL.urLs) {
    init {
        for (id in plugins) addPlugin(id)
    }

    fun addPlugin(id: String) {
        val file = HextantApp.pluginCache.resolve(id)
        val url = file.toURI().toURL()
        addURL(url)
    }

    fun executeInNewThread(runnable: String, vararg arguments: Any): Thread {
        val cls = findClass(runnable)
        val cstr = cls.getDeclaredConstructor(*arguments.map { it.javaClass }.toTypedArray())
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
