/**
 *@author Nikolaus Knop
 */

package hextant.main

import bundles.createBundle
import hextant.command.line.*
import hextant.context.*
import hextant.core.Editor
import hextant.fx.initHextantScene
import hextant.fx.registerShortcuts
import hextant.plugin.PluginBuilder.Phase.Initialize
import javafx.stage.Popup
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File

internal class ProjectOpener(
    private val project: File,
    private val globalContext: Context
) : Runnable {
    override fun run() {
        val desc = project.resolve("project.hxt").readText()
        val (plugins) = Json.decodeFromString<Project>(desc)
        val context = HextantPlatform.projectContext(globalContext)
        loadPlugins(plugins, context, Initialize, project = null)
        val input = context.createInput(project.resolve("root.bin").toPath())
        val root = input.readObject() as Editor<*>
        loadPlugins(plugins, context, Initialize, root)
        val view = context.createControl(root)
        val src = SingleCommandSource(context, context)
        val cl = CommandLine(context, src)
        val clView = CommandLineControl(cl, createBundle())
        clView.prefWidth = 300.0
        val stage = globalContext[HextantApp.stage]
        val cmdPopup = Popup().apply {
            scene.root = clView
            scene.initHextantScene(context)
        }
        view.registerShortcuts {
            on("Ctrl+Q") {
                globalContext[HextantLauncher].launch()
            }
            on("Ctrl+A") {
                cmdPopup.show(stage)
                clView.receiveFocus()
            }
        }
        stage.scene.root = view
    }
}