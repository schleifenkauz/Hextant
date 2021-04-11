package hextant.plugins

import bundles.*
import hextant.context.Context
import hextant.context.Internal
import hextant.context.Properties.classLoader
import hextant.context.Properties.marketplace
import hextant.context.createControl
import hextant.core.Editor
import hextant.core.view.EditorControl
import hextant.fx.getUserInput
import hextant.install.fail
import hextant.main.HextantDirectory
import hextant.main.HextantDirectory.Companion.DISPLAY
import hextant.main.HextantDirectory.Companion.PROJECT_ROOT
import hextant.plugins.PluginBuilder.Phase.*
import hextant.plugins.editor.PluginsEditor
import hextant.serial.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.reflect.full.companionObjectInstance

class Project private constructor(
    val projectType: String,
    val root: Editor<*>,
    val view: EditorControl<*>,
    val context: Context,
    val location: File
) {
    val name: String get() = location.name
    val info
        get() = ProjectInfo(
            projectType,
            context[PluginManager].enabledIds().toList(),
            context[PluginManager].requiredPlugins
        )

    fun save() {
        root.saveSnapshotAsJson(location.resolve(PROJECT_ROOT))
        view.saveSnapshotAsJson(location.resolve(DISPLAY))
        location.resolve(HextantDirectory.PROJECT_INFO).writeJson(info)
        val plugins = context[PluginManager].enabledPlugins()
        val project = context[Project].root
        applyPhase(Close, plugins, context, project)
    }

    fun setRootFile() {
        @Suppress("DEPRECATION")
        root.setFile(PhysicalFile(root, location.resolve(PROJECT_ROOT), context))
    }

    companion object : PublicProperty<Project> by property("project") {
        private fun getProjectTypeInstance(cl: ClassLoader, clazz: String): hextant.project.ProjectType {
            val cls = cl.loadClass(clazz).kotlin
            val obj = cls.objectInstance
            val companion = cls.companionObjectInstance
            return obj as? hextant.project.ProjectType ?: companion as? hextant.project.ProjectType
            ?: error("Invalid project type $clazz")
        }

        private fun setProjectRoot(context: Context, dest: File) {
            val perm = Internal::class.companionObjectInstance as Internal
            context[perm, SerialProperties.projectRoot] = dest
        }

        private fun createPluginManager(context: Context, required: List<String>, enabled: List<String>) {
            val manager = PluginManager(context[marketplace], required)
            manager.enableAll(enabled)
            context[PluginManager] = manager
        }

        fun create(context: Context, projectTypeClass: String, dest: File): Project {
            if (dest.isDirectory) fail("Cannot create duplicate project")
            setProjectRoot(context, dest)
            val infos = if (PluginSource.dynamic()) {
                val projectTypes = runBlocking { context[marketplace].availableProjectTypes() }
                val projectType = projectTypes.find { it.clazz == projectTypeClass }
                    ?: fail("No project type named '$projectTypeClass'")
                val required = listOf(projectType.pluginId)
                createPluginManager(context, required, emptyList())
                val pluginTypes = setOf(PluginInfo.Type.Local, PluginInfo.Type.Global)
                val editor = PluginsEditor(context, context[PluginManager], pluginTypes)
                val enabled = getUserInput("Project plugins", editor, applyStyle = false) ?: fail("Aborted")
                val pluginIds = enabled.map { it.id }
                addPluginsToClasspath(pluginIds, context)
                registerImplementations(pluginIds, context)
                runBlocking { enabled.map { it.info.await() } }
            } else {
                registerImplementationsFromClasspath(context)
                loadPluginInfosFromClasspath(context)
            }
            applyPhase(Initialize, infos, context, project = null)
            applyPhase(Enable, infos, context, project = null)
            val instance = getProjectTypeInstance(context[classLoader], projectTypeClass)
            instance.initializeContext(context)
            val root = instance.createProject(context)
            applyPhase(Enable, infos, context, root)
            applyPhase(Initialize, infos, context, root)
            val view = context.createControl(root)
            return Project(projectTypeClass, root, view, context, dest)
        }

        private fun deserializeProjectRoot(path: File, info: ProjectInfo, context: Context): Editor<Any?> {
            val type = getProjectTypeInstance(context[classLoader], info.projectType)
            type.initializeContext(context)
            val root = path.resolve(PROJECT_ROOT)
            return if (root.exists()) {
                reconstructEditorFromJSONSnapshot(root, context)
            } else {
                type.createProject(context)
            }
        }

        private fun createView(context: Context, root: Editor<Any?>, path: File): EditorControl<*> {
            val view = context.createControl(root)
            val display = path.resolve(DISPLAY)
            if (display.exists()) {
                val json = Json.parseToJsonElement(display.readText())
                val snap = Snapshot.decode<EditorControl<*>>(json)
                view.root
                snap.reconstruct(view)
            }
            return view
        }

        fun open(context: Context, path: File): Project {
            setProjectRoot(context, path)
            val info = path.resolve(HextantDirectory.PROJECT_INFO).readJson<ProjectInfo>()
            val plugins = if (PluginSource.dynamic()) {
                createPluginManager(context, info.enabledPlugins, info.requiredPlugins)
                addPluginsToClasspath(info.enabledPlugins, context)
                registerImplementations(info.enabledPlugins, context)
                runBlocking {
                    info.enabledPlugins.mapNotNull { id ->
                        context[marketplace].get(PluginProperty.info, id)
                    }
                }
            } else {
                registerImplementationsFromClasspath(context)
                loadPluginInfosFromClasspath(context)
            }
            applyPhase(Initialize, plugins, context, project = null)
            val root = deserializeProjectRoot(path, info, context)
            applyPhase(Initialize, plugins, context, root)
            val view = createView(context, root, path)
            return Project(info.projectType, root, view, context, path)
        }
    }
}