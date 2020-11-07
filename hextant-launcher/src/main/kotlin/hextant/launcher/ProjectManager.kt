/**
 *@author Nikolaus Knop
 */

package hextant.launcher

import bundles.PublicProperty
import bundles.property
import hextant.command.meta.CommandParameter
import hextant.command.meta.ProvideCommand
import hextant.context.Context
import hextant.fx.getUserInput
import hextant.launcher.GlobalDirectory.Companion.PROJECTS
import hextant.launcher.GlobalDirectory.Companion.PROJECT_INFO
import hextant.launcher.HextantPlatform.marketplace
import hextant.launcher.HextantPlatform.stage
import hextant.launcher.editor.*
import hextant.launcher.plugins.PluginManager
import hextant.plugins.*
import hextant.plugins.PluginInfo.Type.Global
import hextant.plugins.PluginInfo.Type.Local
import kotlinx.serialization.json.Json
import validated.ifInvalid
import java.io.File

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
        val plugins = getUserInput(editor, applyStyle = false).ifInvalid { return }.map { it.id }
        val dest = globalContext[GlobalDirectory][PROJECTS].resolve(projectName)
        val cl = HextantClassLoader(globalContext, plugins)
        cl.executeInNewThread(
            "hextant.launcher.ProjectCreator",
            ProjectType(projectType.name, projectType.clazz),
            dest,
            required,
            plugins,
            globalContext,
            manager
        )
    }

    private fun acquireLock(project: File): Boolean {
        val lock = project.resolve(GlobalDirectory.LOCK)
        return lock.createNewFile()
    }

    @ProvideCommand("Open Project", shortName = "open", description = "Opens a project")
    fun openProject(
        @CommandParameter(
            description = "The opened project",
            editWith = ProjectLocationEditor::class
        ) name: String
    ) {
        val file = globalContext[GlobalDirectory].getProject(name)
        if (!acquireLock(file)) return
        val desc = file.resolve(PROJECT_INFO)
        val info = Json.tryParse<ProjectInfo>(PROJECT_INFO) { desc.readText() } ?: return
        val cl = HextantClassLoader(globalContext, info.enabledPlugins)
        cl.executeInNewThread("hextant.launcher.ProjectOpener", file, globalContext, info)
    }

    @ProvideCommand("Delete Project", shortName = "delete", description = "Deletes the specified Project from the disk")
    fun deleteProject(
        @CommandParameter(
            description = "The deleted project",
            editWith = ProjectLocationEditor::class
        ) project: String
    ) {
        val file = globalContext[GlobalDirectory].getProject(project)
        if (!acquireLock(file)) return
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
    ): String {
        val oldLocation = globalContext[GlobalDirectory].getProject(project)
        if (!acquireLock(oldLocation)) return "Cannot rename project: Already opened by another editor"
        val newLocation = oldLocation.resolveSibling(newName)
        oldLocation.renameTo(newLocation)
        return "Successfully renamed project"
    }

    @ProvideCommand("Quit", shortName = "quit", description = "Quits the Hextant launcher")
    fun quit() {
        globalContext[stage].close()
    }

    companion object : PublicProperty<ProjectManager> by property("project manager")
}