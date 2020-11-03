/**
 * @author Nikolaus Knop
 */

package hextant.main

import bundles.SimpleProperty
import hextant.context.Context
import hextant.main.GlobalDirectory.Companion.PLUGIN_CACHE
import java.net.URLClassLoader

internal class HextantClassLoader(
    private val context: Context,
    plugins: Collection<String>,
    parent: ClassLoader = systemCL
) : URLClassLoader(getURLs(parent), parent) {
    init {
        for (id in plugins) addPlugin(id)
    }

    fun addPlugin(id: String) {
        if (id == "main" || id == "core") return
        val file = context[GlobalDirectory][PLUGIN_CACHE].resolve("$id.jar")
        val url = file.toURI().toURL()
        addURL(url)
    }

    fun executeInNewThread(runnable: String, vararg arguments: Any): Thread {
        val cls = findLoadedClass(runnable) ?: findClass(runnable)
        val cstr = cls.constructors.first()
        val inst = cstr.newInstance(*arguments) as Runnable
        val t = Thread(inst)
        t.contextClassLoader = this
        t.start()
        return t
    }

    companion object : SimpleProperty<HextantClassLoader>("hextant class loader") {
        private val systemCL = ClassLoader.getSystemClassLoader()
    }
}
