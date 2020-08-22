/**
 *@author Nikolaus Knop
 */

package hextant.main

import hextant.context.*
import hextant.core.Editor
import hextant.plugin.PluginBuilder.Phase.Initialize
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File

class ProjectOpener(private val project: File, private val globalContext: Context) : Runnable {
    override fun run() {
        val desc = project.resolve("project.hxt").readText()
        val (plugins) = Json.decodeFromString<Project>(desc)
        val context = HextantPlatform.projectContext(globalContext)
        loadPlugins(plugins, context, Initialize, project = null)
        val input = context.createInput(project.resolve("root.bin").toPath())
        val root = input.readObject() as Editor<*>
        loadPlugins(plugins, context, Initialize, root)
    }
}