/**
 * @author Nikolaus Knop
 */

package hextant.expr

import bundles.createBundle
import hextant.*
import hextant.command.line.CommandLine
import hextant.command.line.CommandLineControl
import hextant.expr.editor.ExprEditor
import hextant.expr.editor.ExprExpander
import hextant.fx.*
import hextant.main.HextantApplication
import hextant.main.InputMethod
import hextant.main.InputMethod.REGULAR
import hextant.main.InputMethod.VIM
import hextant.serial.SerialProperties
import hextant.serial.makeRoot
import javafx.scene.Parent
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import kserial.*
import reaktive.value.now

class ExprEditorViewTest : HextantApplication() {
    private lateinit var serialContext: SerialContext

    override fun createContext(root: Context): Context = HextantPlatform.defaultContext(root)

    override fun createView(context: Context): Parent {
        serialContext = context[SerialProperties.serialContext]
        val editor = ExprExpander(context)
        editor.makeRoot()
        val view = context.createView(editor)
        val clView = CommandLineControl(context[CommandLine.forEditors], createBundle())
        val globalCLView = CommandLineControl(context[CommandLine.global], createBundle())
        val menuBar = createMenuBar(editor, context, view)
        return VBox(50.0, menuBar, view, globalCLView, clView)
    }

    private fun createMenuBar(
        parent: ExprExpander,
        context: Context,
        view: EditorControl<*>
    ) = menuBar {
        menu("File") {
            item("Save", "Ctrl + S") {
                save(parent)
            }
            item("Open", "Ctrl + O") {
                open(parent)
            }
        }
        menu("Edit") {
            item("Toggle Vim Mode") {
                context[InputMethod] = if (context[InputMethod] == VIM) REGULAR else VIM
                view.applyInputMethod(context[InputMethod])
            }
        }
    }

    private fun open(parent: ExprExpander) {
        val chooser = FileChooser()
        val file = chooser.showOpenDialog(stage) ?: return
        val input = serial.createInput(file, serialContext)
        val editable = input.readTyped<ExprEditor<Expr>>()
        parent.setEditor(editable)
    }

    private fun save(parent: ExprExpander) {
        val chooser = FileChooser()
        val file = chooser.showSaveDialog(stage) ?: return
        val out = serial.createOutput(file, serialContext)
        out.writeObject(parent.editor.now)
    }

    companion object {
        private val serial = KSerial.newInstance {}

        @JvmStatic fun main(args: Array<String>) {
            launch<ExprEditorViewTest>()
        }
    }
}
