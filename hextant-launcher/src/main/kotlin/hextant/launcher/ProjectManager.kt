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
import hextant.launcher.Files.Companion.PROJECTS
import hextant.launcher.Files.Companion.PROJECT_INFO
import hextant.launcher.HextantPlatform.marketplace
import hextant.launcher.HextantPlatform.stage
import hextant.launcher.editor.*
import hextant.launcher.plugins.PluginManager
import hextant.plugins.*
import hextant.plugins.PluginInfo.Type.Global
import hextant.plugins.PluginInfo.Type.Local
import kotlinx.serialization.json.Json
import java.io.File

internal class ProjectManager(private val globalContext: Context) {
    private val openedProjects = mutableSetOf<File>()

    @ProvideCommand("Create Project", shortName = "create", description = "Create a new project")
    fun createNewProject(
        @CommandParameter("project type") projectType: LocatedProjectType,
        @CommandParameter("project name", editWith = ProjectNameEditor::class) projectName: String
    ): String {
        val dest = globalContext[Files][PROJECTS].resolve(projectName)
        if (dest.isDirectory) return "Cannot create duplicate project"
        val required = listOf(projectType.pluginId)
        val marketplace = globalContext[marketplace]
        val manager = PluginManager(marketplace, required)
        manager.enableAll(required)
        manager.enableAll(globalContext[PluginManager].enabledPlugins().map { it.id })
        val editor = PluginsEditor(globalContext, manager, setOf(Local, Global))
        val plugins = getUserInput(editor, applyStyle = false) ?: return "Project creation canceled"
        val pluginIds = plugins.map { it.id }
        val cl = HextantClassLoader(globalContext, pluginIds)
        cl.executeInNewThread(
            "hextant.launcher.ProjectCreator",
            ProjectType(projectType.name, projectType.clazz),
            dest,
            required,
            pluginIds,
            globalContext,
            manager
        )
        return "Project successfully created"
    }

    fun acquireLock(project: File): Boolean {
        val lock = project.resolve(Files.LOCK)
        val possible = lock.createNewFile()
        if (possible) openedProjects.add(project)
        lock.deleteOnExit()
        return possible
    }

    fun releaseLock(project: File) {
        val lock = project.resolve(Files.LOCK)
        openedProjects.remove(project)
        lock.delete()
    }

    fun isLocked(project: File) = project.resolve(Files.LOCK).exists()

    @ProvideCommand("Open Project", shortName = "open", description = "Opens a project")
    fun openProject(
        @CommandParameter(
            description = "The opened project",
            editWith = ProjectLocationEditor::class
        ) name: String
    ): String {
        val file = globalContext[Files].getProject(name)
        if (!acquireLock(file)) return "Project already opened by another editor"
        val desc = file.resolve(PROJECT_INFO)
        val info = Json.tryParse<ProjectInfo>(PROJECT_INFO) { desc.readText() } ?: return "Project info corrupted"
        val cl = HextantClassLoader(globalContext, info.enabledPlugins)
        cl.executeInNewThread("hextant.launcher.ProjectOpener", file, globalContext, info)
        return "Successfully opened project"
    }

    @ProvideCommand("Delete Project", shortName = "delete", description = "Deletes the specified Project from the disk")
    fun deleteProject(
        @CommandParameter(
            description = "The deleted project",
            editWith = ProjectLocationEditor::class
        ) project: String
    ): String {
        val file = globalContext[Files].getProject(project)
        if (isLocked(file)) return "Project opened by another editor"
        return try {
            file.deleteRecursively()
            "Successfully deleted project"
        } catch (e: Exception) {
            e.printStackTrace()
            "Exception while deleting project: ${e.message}"
        }
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
        val oldLocation = globalContext[Files].getProject(project)
        if (isLocked(oldLocation)) return "Cannot rename project: Already opened by another editor"
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