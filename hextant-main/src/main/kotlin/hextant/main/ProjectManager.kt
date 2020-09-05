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
import hextant.plugins.PluginInfo.Type.Language
import hextant.plugins.PluginInfo.Type.Local
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import validated.ifInvalid

internal class ProjectManager(private val context: Context) {
    @ProvideCommand("Create New Project", "create")
    fun createNewProject(
        @CommandParameter("project type") projectType: LocatedProjectType,
        @CommandParameter("project name", editWith = ProjectNameEditor::class) projectName: String
    ) {
        val required = listOf(projectType.pluginId)
        val marketplace = context[marketplace]
        val manager = PluginManager(marketplace, required)
        manager.enableAll(required)
        val editor = PluginsEditor(context, manager, setOf(Local, Language))
        val plugins = getUserInput(editor).ifInvalid { return }.map { it.id }
        val dest = context[GlobalDirectory][PROJECTS].resolve(projectName)
        val cl = HextantClassLoader(context, plugins)
        cl.executeInNewThread(
            "hextant.main.ProjectCreator",
            projectType.clazz,
            dest,
            required,
            plugins,
            context,
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
        val file = context[GlobalDirectory].getProject(name)
        val desc = file.resolve(GlobalDirectory.PROJECT_INFO).bufferedReader().use { r -> r.readText() }
        val (plugins) = Json.decodeFromString<ProjectInfo>(desc)
        val cl = HextantClassLoader(context, plugins)
        cl.executeInNewThread("hextant.main.ProjectOpener", file, context)
    }

    companion object : SimpleProperty<ProjectManager>("project manager")
}