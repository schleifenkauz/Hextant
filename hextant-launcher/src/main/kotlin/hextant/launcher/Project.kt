package hextant.launcher

import bundles.*
import hextant.context.Context
import hextant.context.createControl
import hextant.core.Editor
import hextant.core.view.EditorControl
import hextant.launcher.Files.Companion.DISPLAY
import hextant.launcher.Files.Companion.PROJECT_ROOT
import hextant.launcher.HextantPlatform.marketplace
import hextant.launcher.plugins.PluginManager
import hextant.plugin.PluginBuilder.Phase.Initialize
import hextant.plugins.ProjectInfo
import hextant.plugins.ProjectType
import hextant.serial.*
import kotlinx.serialization.json.Json
import reaktive.Observer
import java.io.File

internal data class Project(
    val type: ProjectType,
    val root: Editor<*>,
    val view: EditorControl<*>,
    val context: Context,
    val location: File
) {
    fun save() {
        root.saveSnapshotAsJson(location.resolve(PROJECT_ROOT))
        view.saveSnapshotAsJson(location.resolve(DISPLAY))
        val manager = context[PluginManager]
        val info = ProjectInfo(type, manager.enabledIds().toList(), manager.requiredPlugins)
        location.resolve(Files.PROJECT_INFO).writeJson(info)
    }

    fun quit() {
        save()
        observers.remove(location)!!.kill()
    }

    companion object : PublicProperty<Project> by property("project") {
        fun open(location: File, info: ProjectInfo, context: Context): Project {
            val manager = PluginManager(context[marketplace], info.requiredPlugins)
            manager.enableAll(info.enabledPlugins)
            context[PluginManager] = manager
            val plugins = info.enabledPlugins
            addPlugins(plugins, context, Initialize, project = null)
            val root = location.resolve(PROJECT_ROOT)
            val editor = reconstructEditorFromJSONSnapshot(root, context)
            @Suppress("DEPRECATION")
            editor.setFile(PhysicalFile(editor, root, context))
            addPlugins(plugins, context, Initialize, editor)
            val view = context.createControl(editor)
            val display = location.resolve(DISPLAY)
            if (display.exists()) {
                val json = Json.parseToJsonElement(display.readText())
                val snap = Snapshot.decode<EditorControl<*>>(json)
                view.root
                snap.reconstruct(view)
            }
            val project = Project(info.projectType, editor, view, context, location)
            context[Project] = project
            val obs = manager.autoLoadAndUnloadPluginsOnChange(context, editor)
            observers[location] = obs
            return project
        }

        private val observers = mutableMapOf<File, Observer>()
    }
}