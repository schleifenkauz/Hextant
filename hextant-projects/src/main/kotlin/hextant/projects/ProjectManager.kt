package hextant.projects

import bundles.PublicProperty
import bundles.publicProperty
import hextant.command.meta.ProvideCommand
import hextant.context.Context
import hextant.context.Properties
import hextant.fx.getUserInput
import hextant.plugins.*
import hextant.plugins.editor.PluginsEditor
import hextant.serial.Files
import kotlinx.serialization.json.Json
import java.io.File

class ProjectManager(private val context: Context) {
    private val globalContext = context[Properties.globalContext]

    fun createNewProject(projectType: LocatedProjectType, dest: File): String {
        if (dest.isDirectory) return "Cannot create duplicate project"
        val required = listOf(projectType.pluginId)
        val marketplace = globalContext[Properties.marketplace]
        val manager = PluginManager(marketplace, required)
        manager.enableAll(required)
        manager.enableAll(globalContext[PluginManager].enabledPlugins().map { it.id })
        val editor = PluginsEditor(globalContext, manager, setOf(PluginInfo.Type.Local, PluginInfo.Type.Global))
        val plugins = getUserInput("Project plugins", editor, applyStyle = false) ?: return "Project creation canceled"
        val pluginIds = plugins.map { it.id }
        val cl = HextantClassLoader(globalContext, pluginIds)
        Thread.currentThread().contextClassLoader = cl
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

    fun openProject(project: File): String {
        if (!context[Files].acquireLock(project)) return "Project already opened by another editor"
        val desc = project.resolve(Files.PROJECT_INFO)
        val info = Json.tryParse<ProjectInfo>(Files.PROJECT_INFO) { desc.readText() } ?: return "Project info corrupted"
        val cl = HextantClassLoader(globalContext, info.enabledPlugins)
        Thread.currentThread().contextClassLoader = cl
        cl.executeInNewThread("hextant.launcher.ProjectOpener", project, globalContext, info)
        return "Successfully opened project"
    }

    fun deleteProject(project: File): String {
        if (context[Files].isLocked(project)) return "Project opened by another editor"
        return try {
            project.deleteRecursively()
            "Successfully deleted project"
        } catch (e: Exception) {
            e.printStackTrace()
            "Exception while deleting project: ${e.message}"
        }
    }

    fun renameProject(project: File, newLocation: File): String {
        if (context[Files].isLocked(project)) return "Cannot rename project: Already opened by another editor"
        project.renameTo(newLocation)
        return "Successfully renamed project"
    }

    @ProvideCommand("Quit", shortName = "quit", description = "Quits the Hextant launcher")
    fun quit() {
        globalContext[Properties.stage].close()
    }

    companion object: PublicProperty<ProjectManager> by publicProperty("project manager")
}