package hextant.main

import bundles.SimpleProperty
import hextant.context.Context
import hextant.context.createOutput
import hextant.core.Editor
import hextant.main.plugins.PluginManager
import hextant.plugins.ProjectType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path

internal data class Project(val type: ProjectType, val root: Editor<*>, val context: Context, val location: Path) {
    fun save() {
        val output = context.createOutput(location.resolve(GlobalDirectory.PROJECT_ROOT))
        output.writeObject(root)
        val manager = context[PluginManager]
        val info = ProjectInfo(type, manager.enabledIds().toList(), manager.requiredPlugins)
        val txt = Json.encodeToString(info)
        Files.newBufferedWriter(location.resolve(GlobalDirectory.PROJECT_INFO)).use { w ->
            w.write(txt)
        }
    }

    companion object : SimpleProperty<Project>("project")
}