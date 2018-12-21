/**
 *@author Nikolaus Knop
 */

package hextant.plugin

import hextant.HextantPlatform
import hextant.bundle.Property
import hextant.core.CorePermissions.Internal
import hextant.core.CorePermissions.Public
import hextant.plugin.impl.CompoundClassLoader
import java.net.URLClassLoader
import java.nio.file.*
import java.util.jar.JarFile
import javax.json.Json
import javax.json.stream.JsonParser

class PluginRegistry(private val platform: HextantPlatform, private val pluginsFile: Path) {
    private val plugins = mutableMapOf<String, Plugin>()

    val compoundClassLoader: ClassLoader get() = _compoundClassLoader

    private val _compoundClassLoader = CompoundClassLoader()

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
            val config = getConfiguration(path)
            val classLoader = URLClassLoader(arrayOf(path.toUri().toURL()))
            loadPlugin(config, classLoader)
        }
    }

    private fun getConfiguration(plugin: Path): JsonParser {
        val jar = JarFile(plugin.toFile())
        val config = jar.getJarEntry(CONFIG_FILE_NAME)
        val input = jar.getInputStream(config)
        return Json.createParser(input)
    }

    /**
     * Loads a plugin from the specified [json]-reader
     * @throws org.nikok.hextant.plugin.PluginException if an exception occurs during loading
     */
    private fun loadPlugin(json: JsonParser, classLoader: ClassLoader) {
        val plugin = JsonPluginLoader.loadPlugin(json, platform, classLoader)
        plugins[plugin.name] = plugin
        _compoundClassLoader.add(classLoader)
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
        private const val CONFIG_FILE_NAME = "plugin.json"
        private const val CORE_PLUGIN_CONFIG = "core.json"
    }
}