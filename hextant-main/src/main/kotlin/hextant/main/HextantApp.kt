/**
 *@author Nikolaus Knop
 */

package hextant.main

import bundles.set
import hextant.context.Internal
import hextant.context.Properties
import hextant.context.Properties.classLoader
import hextant.context.Properties.marketplace
import hextant.context.Properties.stage
import hextant.plugins.PluginManager
import hextant.plugins.addPlugin
import hextant.plugins.tryParse
import hextant.serial.Files
import hextant.serial.Files.Companion.GLOBAL_PLUGINS
import hextant.serial.writeJson
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.stage.Stage
import kotlinx.serialization.json.Json
import java.util.prefs.Preferences
import kotlin.reflect.full.companionObjectInstance

internal class HextantApp : Application() {
    private val globalContext = Properties.globalContext()

    override fun start(primaryStage: Stage) {
//        val loader = HextantClassLoader.instance()
//        loader.context = globalContext
        val internal = Internal::class.companionObjectInstance as Internal
        globalContext[internal, classLoader] = javaClass.classLoader
        javaClass.classLoader.addPlugin("launcher", globalContext)
        setupContext(primaryStage)
        setupStage(primaryStage)
        processArguments(parameters.raw)
    }

    private fun setupStage(primaryStage: Stage) {
        primaryStage.scene = Scene(Label("Hello World"))
        val icon = javaClass.getResource("icon.png")
        primaryStage.icons.add(Image(icon.toExternalForm()))
        primaryStage.show()
    }

    private fun setupContext(primaryStage: Stage) {
        globalContext[stage] = primaryStage
        val globalPlugins = readGlobalPlugins()
        val manager = PluginManager(globalContext[marketplace], emptyList())
        globalContext[PluginManager] = manager
        globalContext[Properties.globalContext] = globalContext
        globalContext[ProjectManager] = ProjectManager(globalContext)
        manager.enableAll(globalPlugins)
    }

    private fun processArguments(args: List<String>) {
        val name = args.firstOrNull()
            ?: Preferences.userRoot().get("launcher-path", null)
            ?: Main.fail("No default launcher configured")
        val path = globalContext[Files].getProject(name)
        globalContext[ProjectManager].openProject(path)
    }

    override fun stop() {
        saveGlobalPlugins()
    }

    private fun readGlobalPlugins(): List<String> {
        val file = globalContext[Files][GLOBAL_PLUGINS]
        if (!file.exists()) return emptyList()
        return Json.tryParse(GLOBAL_PLUGINS) { file.readText() } ?: emptyList()
    }

    private fun saveGlobalPlugins() {
        val enabled = globalContext[PluginManager].enabledPlugins().map { it.id }
        val file = globalContext[Files][GLOBAL_PLUGINS]
        file.writeJson(enabled)
    }
}