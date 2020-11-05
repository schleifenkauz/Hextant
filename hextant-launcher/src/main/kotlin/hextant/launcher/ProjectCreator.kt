/**
 *@author Nikolaus Knop
 */

package hextant.launcher

import hextant.context.Context
import hextant.context.Properties.classLoader
import hextant.context.Properties.defaultContext
import hextant.context.createOutput
import hextant.launcher.HextantPlatform.projectContext
import hextant.launcher.plugins.PluginManager
import hextant.plugin.PluginBuilder.Phase.Enable
import hextant.plugins.ProjectInfo
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

internal class ProjectCreator(
    private val projectType: hextant.plugins.ProjectType,
    private val dest: File,
    private val requiredPlugins: List<String>, private val enabledPlugins: List<String>,
    private val globalContext: Context, private val manager: PluginManager
) : Runnable {
    override fun run() {
        dest.mkdirs()
        val info = ProjectInfo(projectType, enabledPlugins, requiredPlugins)
        val str = Json.encodeToString(info)
        dest.resolve(GlobalDirectory.PROJECT_INFO).writeText(str)
        val context = defaultContext(projectContext(globalContext))
        context[PluginManager] = manager
        context.setProjectRoot(dest.toPath())
        addPlugins(enabledPlugins + "core", context, Enable, project = null)
        val instance = getProjectTypeInstance(context[classLoader], projectType.clazz)
        instance.initializeContext(context)
        val root = instance.createProject(context)
        context.createOutput(dest.resolve(GlobalDirectory.PROJECT_ROOT).toPath()).use { out ->
            out.writeObject(root)
        }
        ProjectOpener(dest, globalContext, info).run()
    }

}