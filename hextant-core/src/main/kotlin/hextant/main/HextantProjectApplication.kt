/**
 *@author Nikolaus Knop
 */

package hextant.main

import hextant.context.*
import hextant.core.Editor
import hextant.core.view.EditorControl
import hextant.core.view.ListEditorControl
import hextant.fx.*
import hextant.serial.SerialProperties
import hextant.undo.UndoManager
import javafx.application.Application
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.Stage
import reaktive.value.now
import validated.force
import java.nio.file.Files
import java.nio.file.Path
import kotlin.reflect.KClass
import kotlin.reflect.full.allSupertypes
import kotlin.reflect.full.cast

/**
 * Abstract base class for applications that use the Hextant project management mechanism.
 */
abstract class HextantProjectApplication<R : Editor<*>> : Application() {
    private val rootType = extractRootEditorType(this::class)

    private val context = HextantPlatform.rootContext()
    private val toplevelContext = Context.newInstance(context) {
        defaultConfig()
        set(Internal, PathChooser, FXPathChooser())
    }

    private val projects = PathListEditor(toplevelContext)

    init {
        context[Internal, ProjectType] = this.projectType()
        val path = projectsPath()
        if (Files.exists(path)) {
            val input = context.createInput(path)
            input.readInplace(projects)
            input.close()
        }
    }

    /**
     * Saves the project.
     */
    override fun stop() {
        val output = context.createOutput(projectsPath())
        output.writeUntyped(projects)
        output.close()
    }

    private fun projectsPath() = configurationDirectory().resolve("recent.ks")

    /**
     * The directory where all the user configuration is stored.
     */
    protected abstract fun configurationDirectory(): Path

    /**
     * The [projectType]
     */
    protected abstract fun projectType(): ProjectType<R>

    private fun createContext(): Context = Context.newInstance {
        defaultConfig()
        configure()
    }

    /**
     * Can be overridden to configure additional properties in the base context.
     */
    protected open fun Context.configure() {}

    /**
     * Create a view for the given [editor].
     */
    protected abstract fun createView(editor: R): Parent

    /**
     * Can be overridden to configure the menu bar.
     */
    protected open fun MenuBarBuilder.configureMenuBar(editor: R) {}

    /**
     * Start the application.
     */
    override fun start(primaryStage: Stage) {
        context[Internal, HextantApplication.stage] = primaryStage
        primaryStage.isResizable = false
        showStartView()
    }

    private fun Context.defaultConfig() {
        set(UndoManager, UndoManager.newInstance())
        set(SelectionDistributor, SelectionDistributor.newInstance())
        set(EditorControlGroup, EditorControlGroup())
    }

    private fun showStartView() {
        val context = toplevelContext
        val logo = Image(
            HextantProjectApplication::class.java.getResource("preloader-image.jpg").toExternalForm(),
            400.0,
            400.0,
            true, true
        )
        val v = context.createView(projects) {
            set(ListEditorControl.ORIENTATION, ListEditorControl.Orientation.Vertical)
            set(ListEditorControl.EMPTY_DISPLAY) { Button("Open or create new project") }
            set(ListEditorControl.CELL_FACTORY, ::Cell)
        }
        val root = HBox(v, ImageView(logo))
        val stage = this.context[Internal, HextantApplication.stage]
        stage.scene = Scene(root)
        stage.scene.initHextantScene(context)
        stage.show()
    }

    private inner class Cell : ListEditorControl.Cell<EditorControl<*>>() {
        override fun updateItem(item: EditorControl<*>) {
            root = item
            check(item is PathEditorControl)
            item.root.onAction {
                val editor = item.editor
                val path = editor.result.now.force()
                showProject(path)
            }
        }
    }

    private fun showProject(path: Path) {
        val input = toplevelContext.createInput(path.resolve(PROJECT_FILE))
        val ctx = createContext()
        ctx[SerialProperties.projectRoot] = path
        val project = rootType.cast(input.readObject())
        showProject(project)
    }

    private fun showProject(root: R) {
        val view = createView(root)
        val menu = createMenuBar(root)
        val stage = context[Internal, HextantApplication.stage]
        stage.scene.root = VBox(menu, view)
    }

    private fun createMenuBar(root: R) = menuBar {
        menu("File") {
            item("Close project") {
                saveProject(root)
                showStartView()
            }
        }
        configureMenuBar(root)
    }

    private fun saveProject(editor: R) {
        val path = editor.context[SerialProperties.projectRoot].resolve(PROJECT_FILE)
        val output = editor.context.createOutput(path)
        output.writeObject(editor)
    }

    companion object {
        private const val PROJECT_FILE = "project.hxt"

        @Suppress("UNCHECKED_CAST")
        private fun <R : Editor<*>> extractRootEditorType(
            cls: KClass<out HextantProjectApplication<R>>
        ): KClass<R> {
            val supertype = cls.allSupertypes.find {
                it.classifier == HextantProjectApplication::class
            } ?: throw AssertionError()
            val r = supertype.arguments[0].type?.classifier
            if (r !is KClass<*>) throw AssertionError()
            return r as KClass<R>
        }
    }
}
