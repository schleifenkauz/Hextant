/**
 *@author Nikolaus Knop
 */

package hextant.plugin

import hextant.HextantPlatform
import hextant.bundle.CorePermissions.Internal
import hextant.bundle.CorePermissions.Public
import hextant.bundle.Property
import hextant.impl.myLogger
import hextant.plugin.dsl.PluginBuilder
import hextant.plugin.impl.CompoundClassLoader
import hextant.plugin.impl.JsonPluginLoader
import java.net.URLClassLoader
import java.nio.file.*
import java.util.jar.JarFile
import javax.json.Json

class PluginRegistry(private val platform: HextantPlatform, private val pluginsFile: Path) {
    private val plugins = mutableMapOf<String, Plugin>()

    val compoundClassLoader: ClassLoader get() = _compoundClassLoader

    private val _compoundClassLoader = CompoundClassLoader().apply {
        add(javaClass.classLoader)
    }

    init {
        loadPlugins()
        loadCore()
    }

    private fun loadCore() {
        loadPlugin(javaClass.classLoader, "Core$1")
    }

    private fun loadPlugins() {
        if (!Files.exists(pluginsFile)) {
            Files.createFile(pluginsFile)
            logger.config { "Plugin file didn't exist, creating it" }
        }
        logger.config { "Reading plugin list from $pluginsFile" }
        val reader = Files.newBufferedReader(pluginsFile)
        val paths = reader.lineSequence()
        for (plugin in paths) {
            val path = Paths.get(plugin)
            logger.config { "Loading plugin from $path" }
            loadPlugin(path)
        }
    }

    private fun loadPlugin(path: Path) {
        val jar = JarFile(path.toFile())
        val classLoader = URLClassLoader(arrayOf(path.toUri().toURL()))
        _compoundClassLoader.add(classLoader)
        loadJsonPlugin(jar)
        loadPlugin(classLoader, "Plugin$1")
    }

    private fun loadPlugin(classLoader: ClassLoader, builderClsName: String) {
        val builderBlock = try {
            classLoader.loadClass(builderClsName)
        } catch (e: ClassNotFoundException) {
            logger.severe { "Class $builderClsName not found" }
            return
        }
        val invoke = builderBlock.getDeclaredMethod("invoke", PluginBuilder::class.java)
        val builder = PluginBuilder(platform)
        val instance = builderBlock.getDeclaredField("INSTANCE").also {
            it.isAccessible = true
        }.get(null)
        invoke.isAccessible = true
        invoke.invoke(instance, builder)
    }

    private fun loadJsonPlugin(jar: JarFile) {
        val config = jar.getJarEntry(JSON_CONFIG_FILE) ?: return
        logger.config { "Found json config at $config" }
        val input = jar.getInputStream(config)
        val parser = Json.createParser(input)
        val plugin = JsonPluginLoader.loadPlugin(parser, platform, compoundClassLoader)
        plugins[plugin.name] = plugin
    }

    /**
     * Returns the [Plugin] instance for the plugin with the specified [name]
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

    /**
     * Loads the plugin from the classpath by executing the kotlin script
     * named "plugin.kts" directly under the resources root
     */
    fun loadPluginFromClasspath(classLoader: ClassLoader) {
        loadPlugin(classLoader, "Plugin$1")
    }

    companion object : Property<PluginRegistry, Public, Internal>("plugin registry") {
        private const val JSON_CONFIG_FILE = "plugin.json"

        val logger by myLogger()
    }
}