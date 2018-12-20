/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.plugin

import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.bundle.Property
import org.nikok.hextant.core.CorePermissions.Internal
import org.nikok.hextant.core.CorePermissions.Public
import org.nikok.hextant.impl.Settings
import java.net.URLClassLoader
import java.nio.file.*
import java.util.jar.JarFile
import javax.json.Json
import javax.json.stream.JsonParser

class PluginRegistry(private val platform: HextantPlatform) {
    private val plugins = mutableMapOf<String, Plugin>()

    init {
        loadPlugins()
    }

    private fun loadPlugins() {
        if (!Files.exists(Settings.plugins)) {
            Files.createFile(Settings.plugins)
        }
        val reader = Files.newBufferedReader(Settings.plugins)
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
        val plugins = Settings.plugins
        val writer = Files.newBufferedWriter(plugins, StandardOpenOption.APPEND)
        writer.appendln(jar.toString())
    }

    companion object : Property<PluginRegistry, Public, Internal>("plugin registry") {
        private const val CONFIG_FILE_NAME = "plugin.json"
    }
}