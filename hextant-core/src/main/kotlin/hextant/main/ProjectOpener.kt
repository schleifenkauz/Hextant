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
import hextant.serial.PhysicalFile
import hextant.serial.SerialProperties.deserializationContext
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
        val root = project.resolve("root.bin").toPath()
        val input = context.createInput(root)
        input.bundle[deserializationContext] = context
        val editor = input.readObject() as Editor<*>
        editor.setFile(PhysicalFile(editor, root, context))
        loadPlugins(plugins, context, Initialize, editor)
        val view = context.createControl(editor)
        val src = SingleCommandSource(context, context)
        val cl = CommandLine(context, src)
        val clView = CommandLineControl(cl, createBundle())
        clView.prefWidth = 300.0
        val stage = globalContext[HextantApp.stage]
        val cmdPopup = Popup().apply {
            scene.root = clView
            scene.initHextantScene(context)
            isHideOnEscape = true
            isAutoHide = true
        }
        view.registerShortcuts {
            on("Ctrl+Q") {
                val output = context.createOutput(root)
                output.writeObject(editor)
                val loader = HextantClassLoader(Context.newInstance(), plugins = emptyList())
                loader.executeInNewThread("hextant.main.HextantLauncher", Main.localContext)
            }
            on("Ctrl+A") {
                cmdPopup.show(stage)
                clView.receiveFocus()
            }
        }
        val sc = stage.scene
        sc.root = view
    }
}