/**
 *@author Nikolaus Knop
 */

package hextant.main

import bundles.SimpleProperty
import hextant.command.meta.CommandParameter
import hextant.command.meta.ProvideCommand
import hextant.context.Context
import hextant.fx.getUserInput
import hextant.main.GlobalDirectory.Companion.PROJECTS
import hextant.main.HextantPlatform.marketplace
import hextant.main.editor.*
import hextant.main.plugins.PluginManager
import hextant.plugins.LocatedProjectType
import hextant.plugins.PluginInfo.Type.Global
import hextant.plugins.PluginInfo.Type.Local
import hextant.plugins.ProjectType
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import validated.ifInvalid

internal class ProjectManager(private val globalContext: Context) {
    @ProvideCommand("Create New Project", "create")
    fun createNewProject(
        @CommandParameter("project type") projectType: LocatedProjectType,
        @CommandParameter("project name", editWith = ProjectNameEditor::class) projectName: String
    ) {
        val required = listOf(projectType.pluginId)
        val marketplace = globalContext[marketplace]
        val manager = PluginManager(marketplace, required)
        manager.enableAll(required)
        manager.enableAll(globalContext[PluginManager].enabledPlugins().map { it.id })
        val editor = PluginsEditor(globalContext, manager, setOf(Local, Global))
        val plugins = getUserInput(editor).ifInvalid { return }.map { it.id }
        val dest = globalContext[GlobalDirectory][PROJECTS].resolve(projectName)
        val cl = HextantClassLoader(globalContext, plugins)
        cl.executeInNewThread(
            "hextant.main.ProjectCreator",
            ProjectType(projectType.name, projectType.clazz),
            dest,
            required,
            plugins,
            globalContext,
            manager
        )
    }

    @ProvideCommand("Open Project", "open")
    fun openProject(
        @CommandParameter(
            description = "The opened project",
            editWith = ProjectLocationEditor::class
        ) name: String
    ) {
        val file = globalContext[GlobalDirectory].getProject(name)
        val desc = file.resolve(GlobalDirectory.PROJECT_INFO).readText()
        val info = Json.decodeFromString<ProjectInfo>(desc)
        val cl = HextantClassLoader(globalContext, info.enabledPlugins)
        cl.executeInNewThread("hextant.main.ProjectOpener", file, globalContext, info)
    }

    companion object : SimpleProperty<ProjectManager>("project manager")
}