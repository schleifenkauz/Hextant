/**
 *@author Nikolaus Knop
 */

package hextant.main

import hextant.command.meta.CommandParameter
import hextant.command.meta.ProvideCommand
import hextant.context.Context
import hextant.fx.getUserInput
import hextant.main.editor.*
import hextant.main.plugins.PluginManager
import hextant.plugins.LocatedProjectType
import hextant.plugins.Plugin.Type.Language
import hextant.plugins.Plugin.Type.Local
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import validated.ifInvalid
import java.io.File

internal class ProjectManager(private val globalContext: Context) {
    @ProvideCommand("Create New Project", "create")
    fun createNewProject(
        @CommandParameter("project type") projectType: LocatedProjectType,
        @CommandParameter("project name", editWith = ProjectNameEditor::class) projectName: String
    ) {
        val required = setOf(projectType.pluginId)
        val marketplace = globalContext[HextantApp.marketplace]
        val manager = PluginManager(marketplace, required)
        val editor = PluginsEditor(globalContext, manager, marketplace, setOf(Local, Language))
        val plugins = getUserInput(editor).ifInvalid { return }
        val dest = HextantApp.projects.resolve(projectName)
        val cl = HextantClassLoader(plugins)
        cl.executeInNewThread("hextant.main.ProjectCreator", projectType.clazz, dest, plugins, globalContext)
    }

    @ProvideCommand("Open Project", "open")
    fun openProject(
        @CommandParameter(
            description = "The opened project",
            editWith = ProjectLocationEditor::class
        ) project: File
    ) {
        val desc = project.resolve("plugin.hxt").bufferedReader().use { r -> r.readText() }
        val (plugins) = Json.decodeFromString<Project>(desc)
        val cl = HextantClassLoader(plugins)
        cl.executeInNewThread("hextant.main.ProjectOpener", project, globalContext)
    }
}