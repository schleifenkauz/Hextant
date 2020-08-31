/**
 *@author Nikolaus Knop
 */

package hextant.main

import hextant.command.line.*
import hextant.context.*
import hextant.core.Editor
import hextant.core.view.EditorControl
import hextant.fx.*
import hextant.main.HextantPlatform.defaultContext
import hextant.main.HextantPlatform.marketplace
import hextant.main.HextantPlatform.projectContext
import hextant.main.HextantPlatform.stage
import hextant.main.plugins.PluginManager
import hextant.plugin.PluginBuilder.Phase.Initialize
import hextant.serial.PhysicalFile
import hextant.serial.SerialProperties.deserializationContext
import javafx.stage.Stage
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import reaktive.Observer
import java.io.File

internal class ProjectOpener(private val project: File, private val globalContext: Context) : Runnable {
    override fun run() {
        val info = readProjectInfo()
        val context = defaultContext(projectContext(globalContext))
        val project = loadProject(info, context)
        val view = context.createControl(project.root)
        val src = SingleCommandSource(context, context)
        val cl = CommandLine(context, src)
        val stage = globalContext[stage]
        val popup = CommandLinePopup(context, cl)
        registerShortcuts(view, context, popup, stage)
        stage.setSize(context[WindowSize])
        stage.setScene(view, context)
        view.receiveFocusLater()
    }

    private fun registerShortcuts(
        view: EditorControl<*>,
        context: Context,
        popup: CommandLinePopup,
        stage: Stage
    ) {
        view.registerShortcuts {
            handleCommands(context, context)
            on("Ctrl?+G") {
                popup.show(stage)
            }
        }
    }

    private fun loadProject(info: ProjectInfo, context: Context): Project {
        val plugins = info.enabledPlugins + "core"
        loadPlugins(plugins, context, Initialize, project = null)
        val root = project.resolve(GlobalDirectory.PROJECT_ROOT).toPath()
        val input = context.createInput(root)
        input.bundle[deserializationContext] = context
        val editor = input.readObject() as Editor<*>
        @Suppress("DEPRECATION")
        editor.setFile(PhysicalFile(editor, root, context))
        loadPlugins(plugins, context, Initialize, editor)
        val project = Project(editor, context, project.toPath())
        context[Project] = project
        val manager = PluginManager(globalContext[marketplace], info.requiredPlugins)
        manager.enableAll(info.enabledPlugins)
        val obs = manager.autoLoadAndUnloadPluginsOnChange(context, editor)
        observers.add(obs)
        context[PluginManager] = manager
        return project
    }

    private fun readProjectInfo(): ProjectInfo {
        val desc = project.resolve(GlobalDirectory.PROJECT_INFO).readText()
        return Json.decodeFromString(desc)
    }

    companion object {
        private val observers = mutableListOf<Observer>()
    }
}