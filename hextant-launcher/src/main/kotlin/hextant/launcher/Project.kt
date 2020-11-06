package hextant.launcher

import bundles.SimpleProperty
import hextant.context.Context
import hextant.core.Editor
import hextant.launcher.GlobalDirectory.Companion.PROJECT_ROOT
import hextant.launcher.plugins.PluginManager
import hextant.plugins.ProjectInfo
import hextant.plugins.ProjectType
import hextant.serial.saveAsJson
import hextant.serial.writeJson
import java.io.File

internal data class Project(val type: ProjectType, val root: Editor<*>, val context: Context, val location: File) {
    fun save() {
        root.saveAsJson(location.resolve(PROJECT_ROOT))
        val manager = context[PluginManager]
        val info = ProjectInfo(type, manager.enabledIds().toList(), manager.requiredPlugins)
        location.resolve(GlobalDirectory.PROJECT_INFO).writeJson(info)
    }

    companion object : SimpleProperty<Project>("project")
}