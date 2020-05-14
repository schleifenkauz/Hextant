/**
 *@author Nikolaus Knop
 */

package hextant.gui

import bundles.createBundle
import hextant.*
import hextant.core.editor.ExpanderConfig
import hextant.core.editor.TokenEditor
import hextant.core.view.TokenEditorView
import hextant.fx.ModifierValue.DOWN
import hextant.fx.menuBar
import hextant.fx.shortcut
import hextant.main.HextantApplication
import hextant.project.editor.DirectoryEditor
import hextant.project.editor.ProjectItemEditor
import hextant.project.view.*
import hextant.serial.FileManager
import hextant.serial.PhysicalFileManager
import hextant.serial.SerialProperties.projectRoot
import javafx.scene.Parent
import javafx.scene.control.SplitPane
import javafx.scene.input.KeyCode.O
import javafx.scene.input.KeyCode.S
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import java.nio.file.Paths

class DirectoryViewTest : HextantApplication() {
    private val pane = SimpleEditorPane()

    override fun createContext(root: Context): Context = root.extend {
        set(FileManager, PhysicalFileManager(this))
        set(ProjectItemEditor.expanderConfig(), ExpanderConfig<IntEditor>().apply {
            registerInterceptor { text, context ->
                text.toIntOrNull()?.let {
                    IntEditor(
                        context,
                        text
                    )
                }
            }
        })
        set(projectRoot, Paths.get("D:", "dev", "hextant", "sample-project"))
        set(EditorPane, pane)
    }

    override fun createView(context: Context): Parent {
        val e = DirectoryEditor<Int>(context)
        e.itemName.setText("project")
        val explorer = ProjectEditorControl(e, createBundle())
        val menu = menuBar {
            menu("File") {
                item("Save", shortcut(S) { control(DOWN) }) {
                    val path = context[projectRoot].resolve(".project")
                    val output = context.createOutput(path)
                    output.writeUntyped(e)
                    output.close()
                }
                item("Open", shortcut(O) { control(DOWN) }) {
                    val path = context[projectRoot].resolve(".project")
                    val input = context.createInput(path)
                    input.readInplace(e)
                    input.close()
                }
            }
        }
        return VBox(menu, SplitPane(explorer, Pane(pane)))
    }

    class IntEditor(context: Context, text: String = "") :
        TokenEditor<Int, TokenEditorView>(context, text) {
        override fun compile(token: String): CompileResult<Int> =
            token.toIntOrNull().okOrErr { "Invalid literal $token" }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(DirectoryViewTest::class.java)
        }
    }
}