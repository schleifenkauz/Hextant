/**
 *@author Nikolaus Knop
 */

package hextant.main

import bundles.SimpleProperty
import hextant.core.Core
import hextant.fx.initHextantScene
import hextant.main.HextantPlatform.globalContext
import hextant.main.HextantPlatform.launcherContext
import hextant.plugin.Aspects
import hextant.plugin.PluginBuilder.Phase.Initialize
import hextant.plugins.Implementation
import hextant.plugins.Marketplace
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.stage.Stage
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException

internal class HextantApp : Application() {
    override fun start(primaryStage: Stage) {
        globalContext[stage] = primaryStage
        initCore()
        val sc = Scene(Label())
        sc.initHextantScene(launcherContext)
        primaryStage.scene = sc
        val args = parameters.raw
        when (args.size) {
            0 -> {
                val cl = HextantClassLoader(globalContext, plugins = emptyList())
                cl.executeInNewThread("hextant.main.HextantLauncher")
            }
            1 -> {
                val project = tryGetFile(args[0])
                if (!project.exists()) Main.fail("File with name $project does not exist")
                globalContext[ProjectManager].openProject(project)
            }
            2 -> Main.fail("Too many arguments")
        }
        primaryStage.show()
    }

    private fun tryGetFile(s: String): File = try {
        val f = File(s)
        f.canonicalPath
        f
    } catch (ex: IOException) {
        Main.fail("Invalid path $s")
    }

    private fun initCore() {
        val url = javaClass.classLoader.getResource("implementations.json")!!
        val desc = url.openStream().bufferedReader().readText()
        val impls = Json.decodeFromString<List<Implementation>>(desc)
        for (impl in impls) {
            launcherContext[Aspects].addImplementation(impl)
        }
        Core.apply(launcherContext, Initialize, project = null)
    }

    companion object {
        val marketplace = SimpleProperty<Marketplace>("marketplace")

        val stage = SimpleProperty<Stage>("stage")
    }
}