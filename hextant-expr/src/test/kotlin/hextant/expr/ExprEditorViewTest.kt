/**
 * @author Nikolaus Knop
 */

package hextant.expr

import hextant.*
import hextant.bundle.Bundle
import hextant.bundle.CorePermissions.Public
import hextant.bundle.CoreProperties
import hextant.command.Commands
import hextant.command.line.CommandLine
import hextant.command.line.FXCommandLineView
import hextant.command.register
import hextant.expr.edited.*
import hextant.expr.edited.Operator.Plus
import hextant.expr.editor.*
import hextant.fx.applyInputMethod
import hextant.fx.registerShortcuts
import hextant.impl.SelectionDistributor
import hextant.inspect.Inspections
import hextant.inspect.Severity.Error
import hextant.inspect.Severity.Warning
import hextant.inspect.of
import hextant.main.HextantApplication
import hextant.main.InputMethod
import hextant.main.InputMethod.REGULAR
import hextant.main.InputMethod.VIM
import hextant.serial.HextantSerialContext
import hextant.undo.UndoManager
import javafx.application.Platform
import javafx.geometry.Orientation.VERTICAL
import javafx.scene.Parent
import javafx.scene.control.*
import javafx.scene.input.KeyCode.*
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.stage.FileChooser
import kserial.*
import reaktive.Observer
import reaktive.value.binding.and
import reaktive.value.binding.map
import reaktive.value.now
import java.util.logging.Level

class ExprEditorViewTest : HextantApplication() {
    private lateinit var serialContext: SerialContext

    override fun createContext(platform: HextantPlatform): Context = Context.newInstance(platform) {
        set(InputMethod, InputMethod.VIM)
    }

    override fun createView(context: Context): Parent {
        serialContext = HextantSerialContext(context.platform, ExprEditorViewTest::class.java.classLoader)
        registerCommandsAndInspections(context)
        val expander = ExprExpander(context)
        val expanderView = context.createView(expander)
        val clContext = Context.newInstance(context) {
            set(Public, SelectionDistributor, SelectionDistributor.newInstance())
        }
        val sd = context[SelectionDistributor]
        val cl = CommandLine.forSelectedEditors(sd, clContext)
        val clView = FXCommandLineView(cl, clContext, Bundle.newInstance())
        val (evaluationDisplay, obs) = evaluateOnExprChange(expander)
        val menuBar = createMenuBar(expander, context)
        val split = SplitPane(menuBar, expanderView, clView)
        context[CoreProperties.logger].level = Level.INFO
        split.orientation = VERTICAL
        return BorderPane(
            HBox(10.0, expanderView, Label("->"), evaluationDisplay),
            menuBar,
            null,
            clView,
            null
        ).apply {
            userData = obs
            registerShortcuts {
                on("Ctrl+P") {
                    context[InputMethod] = if (context[InputMethod] == VIM) REGULAR else VIM
                    applyInputMethod(context[InputMethod])
                }
            }
        }
    }

    private fun evaluateOnExprChange(expandable: ExprExpander): Pair<Label, Observer> {
        val evaluationDisplay = Label("Invalid expression")
        val obs = expandable.result.observe { _, _, new ->
            Platform.runLater {
                if (new !is Ok) {
                    evaluationDisplay.text = "Invalid expression"
                } else {
                    val v = new.value.value
                    evaluationDisplay.text = "$v"
                }
            }
        }
        return Pair(evaluationDisplay, obs)
    }

    private fun registerCommandsAndInspections(context: Context) {
        val commands = context[Commands]
        commands.of<ExprEditor<*>>().register<ExprEditor<*>, Int> {
            name = "Evaluate Expression"
            shortName = "eval"
            applicableIf { exprEditor -> exprEditor.result.now.isOk }
            description = "Evaluates the selected expression and prints it to the console"
            executing { editor, _ ->
                val e = editor.result.now.force()
                val v = e.value
                v
            }
        }
        commands.of<OperatorEditor>().register<OperatorEditor, Unit> {
            name = "Flip operands"
            shortName = "flip_op"
            description = "Flips the both operands in this operator application"
            applicableIf { oe ->
                val oae = oe.parent as? OperatorApplicationEditor ?: return@applicableIf false
                oae.operator.result.now.map { it.isCommutative }.ifErr { false }
            }
            executing { oe, _ ->
                val oae = oe.parent as OperatorApplicationEditor
                val expander1 = oae.operand1
                val editableOp1 = expander1.editor.now
                val expander2 = oae.operand2
                val editableOp2 = expander2.editor.now
                if (editableOp2 != null) expander1.setEditor(editableOp2)
                if (editableOp1 != null) expander2.setEditor(editableOp1)
            }
        }
        commands.of<OperatorApplicationEditor>().register<OperatorApplicationEditor, Unit> {
            name = "Collapse expression"
            shortName = "collapse"
            description = "Partially evaluate the selected expression"
            applicableIf { oae ->
                oae.result.now.isOk && oae.expander != null
            }
            executing { oae, _ ->
                val ex = oae.expander as ExprExpander
                val res = oae.result.now.force().value
                val editable = IntLiteralEditor(context, res.toString())
                ex.setEditor(editable)
            }
        }
        commands.of<ExprEditor<*>>().register<ExprEditor<*>, Unit> {
            name = "Unwrap expression"
            shortName = "unwrap"
            description = "Unwrap an expression by replacing its outer application with itself"
            applicableIf {
                it.parent is OperatorApplicationEditor && it.parent!!.expander is ExprExpander
            }
            executing { editor, _ ->
                val parentExpander = editor.parent!!.expander as ExprExpander
                parentExpander.setEditor(editor)
            }
        }
        val inspections = context[Inspections]
        inspections.of<OperatorApplicationEditor>().registerInspection {
            description = "Prevent identical operations"
            severity(Error)
            val isPlus = inspected.operator.result.map { it.orNull() == Plus }
            val operandIs0 =
                inspected.operand2.result.map { it.orNull() is IntLiteral && it.force().value == 0 }
            preventingThat(isPlus.and(operandIs0))
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
        inspections.of<IntLiteralEditor>().registerInspection {
            description = "Prevent '0' Literals"
            message { "Literal is '0'" }
            severity(Warning)
            preventingThat(inspected.result.map { it.orNull()?.value == 0 })
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
        commands.of<ExprEditor<*>>().register<ExprEditor<*>, Unit> {
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
                operator as Operator
                val expander = editor.expander as ExprExpander
                val app = OperatorApplicationEditor(context)
                app.operator.setText(operator.toString())
                app.operand1.setEditor(editor)
                expander.setEditor(app)
            }
        }
    }

    private fun createMenuBar(
        parent: ExprExpander,
        context: Context
    ): MenuBar {
        val save = createOpenBtn(parent)
        val open = createSaveBtn(parent)
        val file = Menu("File", null, save, open)
        val undo = context[UndoManager]
        val edit = Menu("Edit", null, undoBtn(undo), redoBtn(undo))
        return MenuBar(file, edit)
    }

    private fun redoBtn(undo: UndoManager): MenuItem = MenuItem("Undo").apply {
        setOnAction {
            if (undo.canUndo) {
                undo.undo()
            }
        }
        accelerator = KeyCodeCombination(Z, KeyCombination.SHORTCUT_DOWN)
    }

    private fun undoBtn(undo: UndoManager): MenuItem = MenuItem("Redo").apply {
        setOnAction {
            if (undo.canRedo) {
                undo.redo()
            }
        }
        accelerator = KeyCodeCombination(Z, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN)
    }

    private fun createSaveBtn(parent: ExprExpander) = MenuItem("Save").apply {
        setOnAction {
            val chooser = FileChooser()
            val file = chooser.showSaveDialog(stage) ?: return@setOnAction
            val out = serial.createOutput(file, serialContext)
            out.writeObject(parent.editor.now)
        }
        accelerator = KeyCodeCombination(S, KeyCombination.SHORTCUT_DOWN)
    }

    private fun createOpenBtn(parent: ExprExpander) = MenuItem("Open").apply {
        setOnAction {
            val chooser = FileChooser()
            val file = chooser.showOpenDialog(stage) ?: return@setOnAction
            val input = serial.createInput(file, serialContext)
            val editable = input.readTyped<ExprEditor<Expr>>()
            parent.setEditor(editable)
        }
        accelerator = KeyCodeCombination(O, KeyCombination.SHORTCUT_DOWN)
    }

    companion object {
        private val serial = KSerial.newInstance {}

        @JvmStatic fun main(args: Array<String>) {
            launch(ExprEditorViewTest::class.java, *args)
        }
    }

}
