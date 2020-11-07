/**
 *@author Nikolaus Knop
 */

package hextant.launcher

import hextant.command.Commands
import hextant.command.line.*
import hextant.context.Context
import hextant.context.ControlFactory
import hextant.context.Properties.classLoader
import hextant.context.Properties.defaultContext
import hextant.core.view.EditorControl
import hextant.fx.*
import hextant.launcher.HextantPlatform.projectContext
import hextant.launcher.HextantPlatform.stage
import hextant.launcher.editor.*
import hextant.launcher.view.*
import hextant.plugin.Aspects
import hextant.plugins.ProjectInfo
import javafx.stage.Stage
import java.io.File

internal class ProjectOpener(
    private val project: File,
    private val globalContext: Context,
    private val info: ProjectInfo
) : Runnable {
    override fun run() {
        val context = defaultContext(projectContext(globalContext))
        context.setProjectRoot(project)
        context[Commands].registerGlobalCommands(context)
        context[Aspects].registerDefaultImplementations()
        val type = getProjectTypeInstance(context[classLoader], info.projectType.clazz)
        type.initializeContext(context)
        val project = Project.open(project, info, context)
        val src = SingleCommandSource(context, context)
        val cl = CommandLine(context, src)
        val stage = globalContext[stage]
        val popup = CommandLinePopup(context, cl)
        registerShortcuts(project.view, context, popup, stage)
        stage.setScene(project.view, context)
        stage.setSize(context[WindowSize])
        project.view.receiveFocusLater()
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

    private fun Aspects.registerDefaultImplementations() {
        implement(ControlFactory::class, DisabledPluginInfoEditor::class, DisabledPluginInfoEditorControlFactory)
        implement(ControlFactory::class, EnabledPluginInfoEditor::class, EnabledPluginInfoEditorControlFactory)
        implement(ControlFactory::class, PluginsEditor::class, PluginsEditorControlFactory)
    }
}