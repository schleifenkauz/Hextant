/**
 *@author Nikolaus Knop
 */

package hextant.launcher

import bundles.set
import hextant.context.Context
import hextant.context.Properties.classLoader
import hextant.context.Properties.defaultContext
import hextant.launcher.GlobalDirectory.Companion.PROJECT_ROOT
import hextant.launcher.HextantPlatform.projectContext
import hextant.launcher.plugins.PluginManager
import hextant.plugin.PluginBuilder.Phase.Enable
import hextant.plugins.ProjectInfo
import hextant.serial.saveSnapshotAsJson
import hextant.serial.writeJson
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
        dest.resolve(GlobalDirectory.PROJECT_INFO).writeJson(info)
        val context = defaultContext(projectContext(globalContext))
        context[PluginManager] = manager
        context.setProjectRoot(dest)
        addPlugins(enabledPlugins, context, Enable, project = null)
        val instance = getProjectTypeInstance(context[classLoader], projectType.clazz)
        instance.initializeContext(context)
        val root = instance.createProject(context)
        root.saveSnapshotAsJson(dest.resolve(PROJECT_ROOT))
        val lock = dest.resolve(GlobalDirectory.LOCK)
        lock.createNewFile()
        ProjectOpener(dest, globalContext, info).run()
    }
}