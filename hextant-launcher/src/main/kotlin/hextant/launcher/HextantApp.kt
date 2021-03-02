/**
 *@author Nikolaus Knop
 */

package hextant.launcher

import bundles.set
import hextant.context.Properties.classLoader
import hextant.context.Properties.defaultContext
import hextant.core.Core
import hextant.launcher.Files.Companion.GLOBAL_PLUGINS
import hextant.launcher.HextantPlatform.marketplace
import hextant.launcher.HextantPlatform.projectContext
import hextant.launcher.HextantPlatform.stage
import hextant.launcher.plugins.PluginManager
import hextant.plugin.PluginBuilder.Phase
import hextant.plugin.initializePluginsFromClasspath
import hextant.serial.writeJson
import javafx.application.Application
import javafx.scene.image.Image
import javafx.stage.Stage
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException

internal class HextantApp : Application() {
    private val globalContext = HextantPlatform.globalContext()
    private val launcherContext = defaultContext(projectContext(globalContext))

    override fun start(primaryStage: Stage) {
        setupContext(primaryStage)
        initializePluginsFromClasspath(launcherContext, launcherContext[classLoader])
        processArguments(parameters.raw)
        setupStage(primaryStage)
    }

    private fun setupStage(primaryStage: Stage) {
        primaryStage.sceneProperty().addListener { sc ->
            if (sc != null) primaryStage.show()
        }
        val icon = javaClass.getResource("icon.jpg")
        primaryStage.icons.add(Image(icon.toExternalForm()))
    }

    private fun setupContext(primaryStage: Stage) {
        globalContext[stage] = primaryStage
        globalContext[ProjectManager] = ProjectManager(launcherContext)
        val globalPlugins = readGlobalPlugins()
        val manager = PluginManager(globalContext[marketplace], emptyList())
        globalContext[PluginManager] = manager
        manager.enableAll(globalPlugins)
        launcherContext.setProjectRoot(launcherContext[Files].root)
    }

    private fun processArguments(args: List<String>) {
        when (args.size) {
            0 -> {
                val cl = HextantClassLoader(globalContext, plugins = emptyList())
                Thread.currentThread().contextClassLoader = cl
                cl.executeInNewThread("hextant.launcher.HextantLauncher", globalContext, launcherContext)
            }
            1 -> {
                val name = args[0]
                val project = tryGetFile(name)
                if (!project.exists()) Main.fail("File with name $project does not exist")
                val path = globalContext[Files].getProject(name)
                globalContext[ProjectManager].openProject(path)
            }
            2 -> Main.fail("Too many arguments")
        }
    }

    override fun stop() {
        Core.apply(launcherContext, Phase.Close, project = null)
        HextantMain.apply(launcherContext, Phase.Close, project = null)
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

    private fun tryGetFile(s: String): File = try {
        val f = File(s)
        f.canonicalPath
        f
    } catch (ex: IOException) {
        Main.fail("Invalid path $s")
    }
}