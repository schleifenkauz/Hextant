/**
 *@author Nikolaus Knop
 */

package hextant.main

import hextant.command.Commands
import hextant.command.line.*
import hextant.context.*
import hextant.context.Properties.defaultContext
import hextant.core.Editor
import hextant.core.view.EditorControl
import hextant.fx.*
import hextant.main.HextantPlatform.marketplace
import hextant.main.HextantPlatform.projectContext
import hextant.main.HextantPlatform.stage
import hextant.main.plugins.PluginManager
import hextant.plugin.Aspects
import hextant.plugin.PluginBuilder.Phase.Initialize
import hextant.plugins.ProjectInfo
import hextant.serial.PhysicalFile
import hextant.serial.SerialProperties.deserializationContext
import javafx.stage.Stage
import reaktive.Observer
import java.io.File

internal class ProjectOpener(
    private val project: File,
    private val globalContext: Context,
    private val info: ProjectInfo
) : Runnable {
    override fun run() {
        val context = defaultContext(projectContext(globalContext))
        context.setProjectRoot(project.toPath())
        context[Commands].registerGlobalCommands(context)
        context[Aspects].registerDefaultImplementations()
        val type = getProjectTypeInstance(context[HextantClassLoader], info.projectType.clazz)
        type.initializeContext(context)
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
        val manager = PluginManager(globalContext[marketplace], info.requiredPlugins)
        manager.enableAll(info.enabledPlugins)
        context[PluginManager] = manager
        val plugins = info.enabledPlugins
        addPlugins(plugins, context, Initialize, project = null)
        val root = project.resolve(GlobalDirectory.PROJECT_ROOT).toPath()
        val input = context.createInput(root)
        input.bundle[deserializationContext] = context
        val editor = context.withoutUndo { input.readObject() as Editor<*> }
        @Suppress("DEPRECATION")
        editor.setFile(PhysicalFile(editor, root, context))
        addPlugins(plugins, context, Initialize, editor)
        val project = Project(info.projectType, editor, context, project.toPath())
        context[Project] = project
        val obs = manager.autoLoadAndUnloadPluginsOnChange(context, editor)
        observers.add(obs)
        return project
    }

    companion object {
        private val observers = mutableListOf<Observer>()
    }
}