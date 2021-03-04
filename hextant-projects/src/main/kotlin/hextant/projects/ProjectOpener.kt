/**
 *@author Nikolaus Knop
 */

package hextant.projects

import hextant.command.line.CommandLine
import hextant.command.line.CommandLinePopup
import hextant.command.line.SingleCommandSource
import hextant.context.Context
import hextant.context.Properties.classLoader
import hextant.context.Properties.defaultContext
import hextant.context.Properties.projectContext
import hextant.context.Properties.stage
import hextant.fx.WindowSize
import hextant.fx.handleCommands
import hextant.fx.registerShortcuts
import hextant.plugins.setProjectRoot
import hextant.plugins.Project
import hextant.plugins.ProjectInfo
import hextant.plugins.getProjectTypeInstance
import java.io.File

internal class ProjectOpener(
    private val path: File,
    private val globalContext: Context,
    private val info: ProjectInfo
) : Runnable {
    override fun run() {
        val context = defaultContext(projectContext(globalContext))
        context.setProjectRoot(path)
        val type = getProjectTypeInstance(context[classLoader], info.projectType.clazz)
        type.initializeContext(context)
        val project = Project.open(path, info, context)
        val src = SingleCommandSource(context, context)
        val cl = CommandLine(context, src)
        val stage = globalContext[stage]
        val popup = CommandLinePopup(context, cl)
        project.view.registerShortcuts {
            handleCommands(context, context)
            on("Ctrl?+G") {
                popup.show(stage)
            }
        }
        stage.setScene(project.view, context)
        stage.setSize(context[WindowSize])
        setTitleAndFocus(stage, "Hextant - ${project.name}", project.view)
    }
}