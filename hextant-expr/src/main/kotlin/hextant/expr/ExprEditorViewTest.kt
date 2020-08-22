/**
 * @author Nikolaus Knop
 */

package hextant.expr

import bundles.createBundle
import hextant.command.line.CommandLine
import hextant.command.line.CommandLineControl
import hextant.context.*
import hextant.core.InputMethod
import hextant.core.InputMethod.REGULAR
import hextant.core.InputMethod.VIM
import hextant.core.view.EditorControl
import hextant.core.view.ViewSnapshot
import hextant.expr.editor.ExprExpander
import hextant.fx.*
import hextant.main.HextantApplication
import hextant.serial.makeRoot
import javafx.scene.Parent
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import kserial.readTyped

class ExprEditorViewTest : HextantApplication() {
    private val handler = VisualLoggerHandler()

    override fun createView(context: Context): Parent {
        context[HextantPlatform.logger].addHandler(handler)
        val editor = ExprExpander(context)
        editor.makeRoot()
        val view = context.createView(editor)
        val clView = CommandLineControl(context[CommandLine], createBundle())
        val menuBar = createMenuBar(editor, view)
        return VBox(menuBar, view, clView)
    }

    private fun createMenuBar(editor: ExprExpander, view: EditorControl<*>) = menuBar {
        menu("File") {
            item("Save", "Ctrl + S") {
                save(editor, view)
            }
            item("Open", "Ctrl + O") {
                open(editor, view)
            }
            item("Log Message") {
                editor.context[HextantPlatform.logger].info("Information")
            }
            item("Show Log") {
                handler.stage.show()
            }
        }
        menu("Edit") {
            item("Toggle Vim Mode") {
                editor.context[InputMethod] = if (editor.context[InputMethod] == VIM) REGULAR else VIM
                view.applyInputMethod(editor.context[InputMethod])
            }
        }
    }

    private fun open(editor: ExprExpander, view: EditorControl<*>) {
        val chooser = FileChooser()
        val file = chooser.showOpenDialog(stage) ?: return
        val input = editor.context.createInput(file.toPath())
        input.readInplace(editor)
        val snapshot = input.readTyped<ViewSnapshot<EditorControl<*>>>()
        snapshot.reconstruct(view)
    }

    private fun save(editor: ExprExpander, view: EditorControl<*>) {
        val chooser = FileChooser()
        val file = chooser.showSaveDialog(stage) ?: return
        val out = editor.context.createOutput(file.toPath())
        out.writeUntyped(editor)
        out.writeObject(view.createSnapshot())
    }

    companion object {
        @JvmStatic fun main(args: Array<String>) {
            launch<ExprEditorViewTest>()
        }
    }
}
