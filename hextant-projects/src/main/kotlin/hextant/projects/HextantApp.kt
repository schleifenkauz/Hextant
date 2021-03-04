/**
 *@author Nikolaus Knop
 */

package hextant.projects

import bundles.set
import hextant.context.Context
import hextant.context.Properties
import hextant.context.Properties.classLoader
import hextant.context.Properties.marketplace
import hextant.context.Properties.stage
import hextant.plugins.*
import hextant.serial.Files
import hextant.serial.Files.Companion.GLOBAL_PLUGINS
import hextant.serial.writeJson
import javafx.application.Application
import javafx.scene.image.Image
import javafx.stage.Stage
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException

internal class HextantApp : Application() {
    private val globalContext = Properties.globalContext()

    override fun start(primaryStage: Stage) {
        setupContext(primaryStage)
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
        val globalPlugins = readGlobalPlugins()
        val manager = PluginManager(globalContext[marketplace], emptyList())
        globalContext[PluginManager] = manager
        globalContext[ProjectManager] = ProjectManager(globalContext)
        manager.enableAll(globalPlugins)
    }

    private fun processArguments(args: List<String>) {
        val name = when (args.size) {
            0 -> getDefaultLauncher()
            1 -> args[0]
            else -> OpenProject.fail("Too many arguments")
        }
        val project = tryGetFile(name)
        if (!project.exists()) OpenProject.fail("File with name $project does not exist")
        val path = globalContext[Files].getProject(name)
        globalContext[ProjectManager].openProject(path)
    }

    private fun getDefaultLauncher(): String {
        TODO("Not yet implemented")
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

    private fun tryGetFile(s: String): File = try {
        val f = File(s)
        f.canonicalPath
        f
    } catch (ex: IOException) {
        OpenProject.fail("Invalid path $s")
    }
}