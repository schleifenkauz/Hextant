/**
 * @author Nikolaus Knop
 */

package hextant.expr

import bundles.createBundle
import hextant.*
import hextant.command.*
import hextant.command.line.CommandLine
import hextant.command.line.CommandLineControl
import hextant.expr.Operator.Plus
import hextant.expr.editor.*
import hextant.fx.*
import hextant.inspect.Inspections
import hextant.inspect.Severity.Error
import hextant.inspect.Severity.Warning
import hextant.inspect.registerInspection
import hextant.main.HextantApplication
import hextant.main.InputMethod
import hextant.main.InputMethod.REGULAR
import hextant.main.InputMethod.VIM
import hextant.serial.SerialProperties
import hextant.serial.makeRoot
import hextant.undo.compoundEdit
import javafx.scene.Parent
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import kserial.*
import reaktive.value.binding.and
import reaktive.value.binding.map
import reaktive.value.now
import validated.*

class ExprEditorViewTest : HextantApplication() {
    private lateinit var serialContext: SerialContext

    override fun createContext(root: Context): Context = HextantPlatform.defaultContext(root)

    override fun createView(context: Context): Parent {
        serialContext = context[SerialProperties.serialContext]
        registerCommandsAndInspections(context)
        val editor = ExprExpander(context)
        editor.makeRoot()
        val view = context.createView(editor)
        val clView = CommandLineControl(context[CommandLine.forEditors], createBundle())
        val menuBar = createMenuBar(editor, context, view)
        return VBox(50.0, menuBar, view, clView)
    }

    private fun registerCommandsAndInspections(context: Context) {
        val commands = context[Commands]
        commands.registerCommand<ExprEditor<*>, Int> {
            name = "Evaluate Expression"
            shortName = "eval"
            applicableIf { exprEditor -> exprEditor.result.now.isValid }
            description = "Evaluates the selected expression and prints it to the console"
            executing { editor, _ ->
                val e = editor.result.now.force()
                val v = e.value
                v
            }
        }
        commands.registerCommand<OperatorEditor, Unit> {
            name = "Flip operands"
            shortName = "flip_op"
            description = "Flips the both operands in this operator application"
            type = Command.Type.SingleReceiver
            applicableIf { oe ->
                val oae = oe.parent as? OperatorApplicationEditor ?: return@applicableIf false
                oae.operator.result.now.map { it.isCommutative }.ifInvalid { false }
            }
            executingCompoundEdit { oe, _ ->
                val oae = oe.parent as OperatorApplicationEditor
                val expander1 = oae.operand1
                val editableOp1 = expander1.editor.now
                val expander2 = oae.operand2
                val editableOp2 = expander2.editor.now
                if (editableOp2 != null) expander1.setEditor(editableOp2)
                if (editableOp1 != null) expander2.setEditor(editableOp1)
            }
        }
        commands.registerCommand<OperatorApplicationEditor, Unit> {
            name = "Collapse expression"
            shortName = "collapse"
            description = "Partially evaluate the selected expression"
            applicableIf { oae ->
                oae.result.now.isValid && oae.expander != null
            }
            executingCompoundEdit { oae, _ ->
                val ex = oae.expander as ExprExpander
                val res = oae.result.now.force().value
                val editable = IntLiteralEditor(context, res.toString())
                ex.setEditor(editable)
            }
        }
        commands.registerCommand<ExprEditor<*>, Unit> {
            name = "Unwrap expression"
            shortName = "unwrap"
            description = "Unwrap an expression by replacing its outer application with itself"
            applicableIf {
                it.parent is OperatorApplicationEditor && it.parent!!.expander is ExprExpander
            }
            executingCompoundEdit { editor, _ ->
                val parentExpander = editor.parent!!.expander as ExprExpander
                parentExpander.setEditor(editor)
            }
        }
        val inspections = context[Inspections]
        inspections.registerInspection<OperatorApplicationEditor> {
            description = "Prevent identical operations"
            severity(Error)
            preventingThat {
                val operandIsZero = inspected.operand2.result.map { it.orNull() is IntLiteral && it.force().value == 0 }
                val operatorIsPlus = inspected.operator.result.map { it.orNull() == Plus }
                operatorIsPlus.and(operandIsZero)
            }
            message { "Operation doesn't change the result" }
            addFix {
                description = "Shorten expression"
                applicableIf {
                    inspected.expander is ExprExpander
                }
                fixingBy {
                    val expander = inspected.expander as ExprExpander
                    expander.setEditor(inspected.operand1)
                }

            }
        }
        inspections.registerInspection<IntLiteralEditor> {
            description = "Prevent '0' Literals"
            message { "Literal is '0'" }
            severity(Warning)
            preventingThat { inspected.result.map { it.orNull()?.value == 0 } }
            addFix {
                description = "Set to '1'"
                fixingBy {
                    inspected.setText("1")
                }
            }
            addFix {
                description = "Set to '2'"
                fixingBy {
                    inspected.setText("2")
                }
            }
        }
        commands.registerCommand<ExprEditor<*>, Unit> {
            description =
                "Wraps the current expression in an operator expression with the current expression being the left operand"
            name = "Wrap in operator expression"
            shortName = "wrap_op"
            addParameter {
                ofType<Operator>()
                description = "The operator being applied"
                name = "operator"
            }
            applicableIf { it.expander is ExprExpander }
            executing { editor, (operator) ->
                editor.context.compoundEdit("Wrap with $operator") {
                    operator as Operator
                    val expander = editor.expander as ExprExpander
                    val app = OperatorApplicationEditor(context)
                    expander.setEditor(app)
                    app.operator.setText(operator.toString())
                    app.operand1.setEditor(editor)
                }
            }
        }
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
