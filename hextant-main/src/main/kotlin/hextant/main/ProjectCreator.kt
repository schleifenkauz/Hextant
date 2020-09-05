/**
 *@author Nikolaus Knop
 */

package hextant.main

import hextant.context.Context
import hextant.context.Properties.defaultContext
import hextant.context.createOutput
import hextant.core.Editor
import hextant.main.HextantPlatform.projectContext
import hextant.main.Main.fail
import hextant.main.plugins.PluginManager
import hextant.plugin.PluginBuilder.Phase.Enable
import hextant.project.ProjectType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.createInstance

internal class ProjectCreator(
    private val projectType: String,
    private val dest: File,
    private val requiredPlugins: List<String>, private val enabledPlugins: List<String>,
    private val globalContext: Context, private val manager: PluginManager
) : Runnable {
    override fun run() {
        dest.mkdir()
        val project = ProjectInfo(enabledPlugins, requiredPlugins)
        val str = Json.encodeToString(project)
        dest.resolve(GlobalDirectory.PROJECT_INFO).writeText(str)
        val context = defaultContext(projectContext(globalContext))
        context[PluginManager] = manager
        context.setProjectRoot(dest.toPath())
        initializePlugins(enabledPlugins + "core", context, Enable, project = null)
        val cls = context[HextantClassLoader].loadClass(projectType).kotlin
        val obj = cls.objectInstance
        val companion = cls.companionObjectInstance
        val root = when {
            obj is ProjectType       -> obj.createProject(context)
            companion is ProjectType -> companion.createProject(context)
            else                     -> cls.createInstance() as? Editor<*> ?: fail("Invalid project type $projectType")
        }
        context.createOutput(dest.resolve(GlobalDirectory.PROJECT_ROOT).toPath()).use { out ->
            out.writeObject(root)
        }
        ProjectOpener(dest, globalContext).run()
    }
}