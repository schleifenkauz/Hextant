/**
 *@author Nikolaus Knop
 */

package hextant.main

import hextant.context.Context
import hextant.context.createOutput
import hextant.core.Editor
import hextant.main.Main.fail
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
    private val plugins: List<String>,
    private val globalContext: Context
) : Runnable {
    override fun run() {
        dest.mkdir()
        val project = Project(plugins)
        val str = Json.encodeToString(project)
        dest.resolve("project.hxt").writeText(str)
        val context = HextantPlatform.projectContext(globalContext)
        loadPlugins(plugins, context, Enable, project = null)
        val cls = Thread.currentThread().contextClassLoader.loadClass(projectType).kotlin
        val obj = cls.objectInstance
        val companion = cls.companionObjectInstance
        val root = when {
            obj is ProjectType       -> obj.createProject(context)
            companion is ProjectType -> companion.createProject(context)
            else                     -> cls.createInstance() as? Editor<*> ?: fail("Invalid project type $projectType")
        }
        context.createOutput(dest.resolve("root.bin").toPath()).use { out ->
            out.writeObject(root)
        }
    }
}