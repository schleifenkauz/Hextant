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
import hextant.main.HextantPlatform.stage
import hextant.main.editor.*
import hextant.main.plugins.PluginManager
import hextant.plugins.LocatedProjectType
import hextant.plugins.PluginInfo.Type.Global
import hextant.plugins.PluginInfo.Type.Local
import hextant.plugins.ProjectType
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import validated.ifInvalid
import java.nio.file.Files

internal class ProjectManager(private val globalContext: Context) {
    @ProvideCommand("Create Project", shortName = "create", description = "Create a new project")
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

    @ProvideCommand("Open Project", shortName = "open", description = "Opens a project")
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

    @ProvideCommand("Delete Project", shortName = "delete", description = "Deletes the specified Project from the disk")
    fun deleteProject(
        @CommandParameter(
            description = "The deleted project",
            editWith = ProjectLocationEditor::class
        ) project: String
    ) {
        val file = globalContext[GlobalDirectory].getProject(project)
        file.deleteRecursively()
    }

    @ProvideCommand(
        "Rename Project",
        shortName = "rename",
        description = "Renames the given project to the specified name"
    )
    fun renameProject(
        @CommandParameter(
            description = "The project that is renamed",
            editWith = ProjectLocationEditor::class
        ) project: String,
        @CommandParameter(
            description = "The new name for the project",
            editWith = ProjectNameEditor::class
        ) newName: String
    ) {
        val oldLocation = globalContext[GlobalDirectory].getProject(project).toPath()
        val newLocation = oldLocation.resolveSibling(newName)
        Files.move(oldLocation, newLocation)
    }

    @ProvideCommand("Quit", shortName = "quit", description = "Quits the Hextant launcher")
    fun quit() {
        globalContext[stage].close()
    }

    companion object : SimpleProperty<ProjectManager>("project manager")
}