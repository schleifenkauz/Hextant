package hextant.main

import bundles.*
import hextant.cli.HextantDirectory
import hextant.cli.HextantDirectory.DISPLAY
import hextant.cli.HextantDirectory.PROJECT_INFO
import hextant.cli.HextantDirectory.PROJECT_ROOT
import hextant.cli.fail
import hextant.context.Context
import hextant.context.Internal
import hextant.context.Properties.classLoader
import hextant.context.Properties.marketplace
import hextant.context.createControl
import hextant.core.Editor
import hextant.core.view.EditorControl
import hextant.fx.getUserInput
import hextant.plugins.*
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
        location.resolve(PROJECT_INFO).writeJson(info)
        val plugins = context[PluginManager].enabledPlugins()
        applyPhase(Close, plugins, context, root)
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

        private fun getProjectType(
            context: Context,
            projectTypeName: String
        ) = (runBlocking { context[marketplace].getProjectType(projectTypeName) }
            ?: fail("No project type named '$projectTypeName'"))

        fun create(context: Context, projectTypeName: String, dest: File): Project {
            if (dest.isDirectory) fail("Cannot create duplicate project")
            dest.mkdir()
            HextantDirectory.acquireLock(dest)
            setProjectRoot(context, dest)
            val projectType = getProjectType(context, projectTypeName)
            val infos = if (PluginSource.dynamic()) {
                val required = listOf(projectType.pluginId)
                createPluginManager(context, required, required)
                val pluginTypes = setOf(PluginInfo.Type.Local, PluginInfo.Type.Global)
                val editor = PluginsEditor(context, context[PluginManager], pluginTypes)
                val enabled = getUserInput("Project plugins", editor, applyStyle = false) ?: fail("Aborted")
                val pluginIds = enabled.map { it.id }
                context[classLoader].addPluginsToClasspath(pluginIds)
                registerImplementations(pluginIds, context)
                runBlocking { enabled.map { it.info.await() } }
            } else {
                registerImplementationsFromClasspath(context)
                loadPluginInfosFromClasspath(context)
            }
            applyPhase(Initialize, infos, context, project = null)
            applyPhase(Enable, infos, context, project = null)
            val instance = getProjectTypeInstance(context[classLoader], projectType.clazz)
            instance.initializeContext(context)
            val root = instance.createProject(context)
            applyPhase(Enable, infos, context, root)
            applyPhase(Initialize, infos, context, root)
            val view = context.createControl(root)
            return Project(projectTypeName, root, view, context, dest)
        }

        private fun deserializeProjectRoot(path: File, info: ProjectInfo, context: Context): Editor<Any?> {
            val projectType = getProjectType(context, info.projectType)
            val type = getProjectTypeInstance(context[classLoader], projectType.clazz)
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
                val snap = Snapshot.decodeFromJson<EditorControl<*>>(json)
                view.root
                snap.reconstructObject(view)
            }
            return view
        }

        fun open(context: Context, path: File): Project {
            check(HextantDirectory.acquireLock(path)) { "Project already opened in another Hextant window" }
            setProjectRoot(context, path)
            val info = path.resolve(PROJECT_INFO).readJson<ProjectInfo>()
            val plugins = if (PluginSource.dynamic()) {
                createPluginManager(context, info.enabledPlugins, info.requiredPlugins)
                context[classLoader].addPluginsToClasspath(info.enabledPlugins)
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