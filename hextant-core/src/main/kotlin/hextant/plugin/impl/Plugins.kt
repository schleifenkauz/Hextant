package hextant.plugin.impl

import Core
import bundles.SimpleProperty
import hextant.context.Context
import hextant.plugin.Plugin
import hextant.plugin.PluginException
import hextant.plugin.dsl.PluginInitializer
import java.util.jar.Attributes.Name
import java.util.jar.Manifest
import kotlin.reflect.full.isSubclassOf

internal class Plugins(context: Context, classLoader: ClassLoader = Plugins::class.java.classLoader) {
    val allPlugins = initializePlugins(context, classLoader)

    private fun initializePlugins(context: Context, classLoader: ClassLoader): List<Plugin> {
        Core.apply(context)
        val all = mutableListOf<Plugin>()
        val urls = classLoader.getResources("META-INF/MANIFEST.MF")
        for (url in urls) {
            val input = url.openConnection().getInputStream()
            val m = Manifest(input)
            input.close()
            val attributes = m.mainAttributes
            val className = attributes[PLUGIN_INITIALIZER]
            if (className !is String) continue
            val cls = try {
                classLoader.loadClass(className).kotlin
            } catch (e: ClassNotFoundException) {
                throw PluginException("Plugin initializer class $className not found", e)
            }
            if (!cls.isSubclassOf(PluginInitializer::class))
                throw PluginException("$cls referenced in plugin manifest is not a subclass of Plugin")
            val instance = cls.objectInstance
                ?: throw PluginException("$cls referenced in plugin manifest has no object instance")
            instance as PluginInitializer
            val plugin = try {
                instance.apply(context)
            } catch (e: Throwable) {
                throw PluginException("Error in plugin initialization", e)
            }
            all.add(plugin)
        }
        return all
    }

    companion object : SimpleProperty<Plugins>("plugins") {
        private val PLUGIN_INITIALIZER = Name("Plugin-Initializer")
    }
}