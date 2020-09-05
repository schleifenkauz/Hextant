/**
 *@author Nikolaus Knop
 */

package hextant.main

import hextant.context.Properties.defaultContext
import hextant.core.Core
import hextant.fx.initHextantScene
import hextant.main.HextantPlatform.projectContext
import hextant.main.HextantPlatform.stage
import hextant.plugin.PluginBuilder.Phase
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.stage.Stage
import java.io.File
import java.io.IOException

internal class HextantApp : Application() {
    private val globalContext = HextantPlatform.globalContext()
    private val launcherContext = defaultContext(projectContext(globalContext))

    override fun start(primaryStage: Stage) {
        globalContext[stage] = primaryStage
        globalContext[ProjectManager] = ProjectManager(launcherContext)
        launcherContext.setProjectRoot(launcherContext[GlobalDirectory].root.toPath())
        initializePluginsFromClasspath(launcherContext)
        val sc = Scene(Label())
        sc.initHextantScene(launcherContext)
        primaryStage.scene = sc
        val args = parameters.raw
        when (args.size) {
            0 -> {
                val cl = HextantClassLoader(globalContext, plugins = emptyList())
                cl.executeInNewThread("hextant.main.HextantLauncher", globalContext, launcherContext)
            }
            1 -> {
                val name = args[0]
                val project = tryGetFile(name)
                if (!project.exists()) Main.fail("File with name $project does not exist")
                globalContext[ProjectManager].openProject(name)
            }
            2 -> Main.fail("Too many arguments")
        }
        primaryStage.show()
    }

    override fun stop() {
        for (initializer in listOf(Core, HextantMain)) {
            initializer.apply(launcherContext, Phase.Close, project = null)
        }
    }

    private fun tryGetFile(s: String): File = try {
        val f = File(s)
        f.canonicalPath
        f
    } catch (ex: IOException) {
        Main.fail("Invalid path $s")
    }
}