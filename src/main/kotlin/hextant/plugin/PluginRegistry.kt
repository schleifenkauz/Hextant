/**
 *@author Nikolaus Knop
 */

package hextant.plugin

import hextant.HextantPlatform
import hextant.bundle.CorePermissions.Internal
import hextant.bundle.CorePermissions.Public
import hextant.bundle.Property
import hextant.plugin.dsl.DslPluginLoader
import hextant.plugin.impl.CompoundClassLoader
import hextant.plugin.impl.JsonPluginLoader
import java.io.InputStreamReader
import java.io.Reader
import java.net.URLClassLoader
import java.nio.file.*
import java.util.jar.JarFile
import javax.json.Json
import javax.json.stream.JsonParser

class PluginRegistry(private val platform: HextantPlatform, private val pluginsFile: Path) {
    private val plugins = mutableMapOf<String, Plugin>()

    val compoundClassLoader: ClassLoader get() = _compoundClassLoader

    private val _compoundClassLoader = CompoundClassLoader()

    private val dslPluginLoader = DslPluginLoader(platform, compoundClassLoader)

    init {
        _compoundClassLoader.add(javaClass.classLoader)
        loadPlugins()
        loadCore()
    }

    private fun loadCore() {
        val core = javaClass.getResource(CORE_PLUGIN_CONFIG)
        val input = core.openStream()
        val json = Json.createParser(input)
        loadPlugin(json, javaClass.classLoader)
    }

    private fun loadPlugins() {
        if (!Files.exists(pluginsFile)) {
            Files.createFile(pluginsFile)
        }
        val reader = Files.newBufferedReader(pluginsFile)
        val paths = reader.lineSequence()
        for (plugin in paths) {
            val path = Paths.get(plugin)
            val jar = JarFile(path.toFile())
            val jsonConfig = getJsonConfiguration(jar)
            val dslConfig = getDslConfiguration(jar)
            val classLoader = URLClassLoader(arrayOf(path.toUri().toURL()))
            _compoundClassLoader.add(classLoader)
            if (jsonConfig != null) loadPlugin(jsonConfig, classLoader)
            if (dslConfig != null) loadPlugin(dslConfig)
        }
    }

    private fun getDslConfiguration(root: JarFile): Reader? {
        val config = root.getJarEntry(DSL_CONFIG_FILE) ?: return null
        val input = root.getInputStream(config)
        return InputStreamReader(input)
    }

    private fun getJsonConfiguration(jar: JarFile): JsonParser? {
        val config = jar.getJarEntry(JSON_CONFIG_FILE) ?: return null
        val input = jar.getInputStream(config)
        return Json.createParser(input)
    }

    /**
     * Loads a plugin from the specified [json]-reader
     * @throws PluginException if an exception occurs during loading
     */
    private fun loadPlugin(json: JsonParser, classLoader: ClassLoader) {
        val plugin = JsonPluginLoader.loadPlugin(json, platform, classLoader)
        plugins[plugin.name] = plugin
    }

    /**
     * Loads a plugin from the specified [dsl]-reader
     */
    private fun loadPlugin(dsl: Reader) {
        val plugin = dslPluginLoader.loadPlugin(dsl)
        plugins[plugin.name] = plugin
    }

    /**
     * Returns the [Plugin] instance for the plugin with the specified [name] previously load by [loadPlugin]
     */
    fun getPlugin(name: String): Plugin =
        plugins[name] ?: throw NoSuchElementException("No plugin with name $name loaded")


    /**
     * Adds the plugin contained in the specified [jar]
     * * To activate the plugin hextant must be restarted
     */
    fun addPlugin(jar: Path) {
        val plugins = pluginsFile
        val writer = Files.newBufferedWriter(plugins, StandardOpenOption.APPEND)
        writer.appendln(jar.toString())
    }

    companion object : Property<PluginRegistry, Public, Internal>("plugin registry") {
        private const val JSON_CONFIG_FILE = "plugin.json"
        private const val DSL_CONFIG_FILE = "plugin.kts"
        private const val CORE_PLUGIN_CONFIG = "core.json"
    }
}