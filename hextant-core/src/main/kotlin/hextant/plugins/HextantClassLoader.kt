package hextant.plugins

import bundles.PublicProperty
import bundles.property
import hextant.context.Context
import hextant.serial.Files
import java.lang.reflect.InaccessibleObjectException
import java.net.URL
import java.net.URLClassLoader

class HextantClassLoader(
    private val context: Context,
    plugins: Collection<String>,
    parent: ClassLoader = systemCL
) : URLClassLoader(getURLs(parent), parent) {
    init {
        for (id in plugins) addPlugin(id)
    }

    fun addPlugin(id: String) {
        if (id == "main" || id == "core") return
        val file = context[Files][Files.PLUGIN_CACHE].resolve("$id.jar")
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

    companion object : PublicProperty<HextantClassLoader> by property("hextant class loader") {
        private fun getURLClassPath(classLoader: ClassLoader): Any {
            val field = classLoader.javaClass.getDeclaredField("ucp")
            field.isAccessible = true
            return field.get(classLoader)
        }

        @Suppress("UNCHECKED_CAST")
        internal fun getURLs(classLoader: ClassLoader): Array<out URL> =
            try {
                val ucp = getURLClassPath(classLoader)
                val method = ucp.javaClass.getMethod("getURLs")
                method.invoke(ucp) as Array<out URL>
            } catch (e: ReflectiveOperationException) {
                throw AssertionError("Could not extract urls from system class loader", e)
            } catch (e: InaccessibleObjectException) {
                throw AssertionError("Could not extract urls from system class loader", e)
            } catch (e: ClassCastException) {
                throw AssertionError("Could not extract urls from system class loader", e)
            }


        private val systemCL = ClassLoader.getSystemClassLoader()
    }
}